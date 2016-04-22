package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.search.SearchModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.utilities.LayoutUtils.makeEditText;

/**
 * Allow user to search for entities using free text criteria.
 *
 * The gateway and database columns to search are supplied by one or more
 * SearchPluginModules.  The search results are passed as a list of
 * QueryResults to a listener.
 */
public class SearchFragment extends Fragment {

    private static final String LIKE_WILD_CARD = "%";
    private static final String DATA_CATEGORY = "searchFragment";
    private static final int RESULTS_PENDING = -1;
    private static final int NO_SEARCH = -2;

    private SearchModule currentModule;
    private ResultsHandler resultsHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        View button = view.findViewById(R.id.search_fragment_button);
        button.setOnClickListener(new ButtonClickHandler());
        return view;
    }

    public void setResultsHandler(ResultsHandler resultsHandler) {
        this.resultsHandler = resultsHandler;
    }

    @SuppressWarnings("unchecked")
    public void setModules(List<? extends SearchModule> modules) {

        Spinner spinner = (Spinner) getView().findViewById(R.id.search_fragment_spinner);

        boolean modulesExist = modules != null && modules.size() > 0;
        boolean multipleExist = modules != null && modules.size() > 1;

        spinner.setVisibility(multipleExist? View.VISIBLE : View.GONE);

        if (modulesExist) {
            if (multipleExist) {
                spinner.setVisibility(View.VISIBLE);
                ArrayAdapter<SearchModule> moduleAdapter = new SpinnerListAdapter(getActivity(),
                        R.layout.generic_dropdown_item, (List<SearchModule>) modules);
                spinner.setAdapter(moduleAdapter);
                spinner.setOnItemSelectedListener(new SpinnerClickHandler());
            }
            selectModule(modules.get(0));
        }
    }

    public void setTitle(int titleId) {
        TextView titleText = (TextView) getView().findViewById(R.id.search_fragment_title);
        titleText.setText(titleId);
    }

    private void updateStatus(int resultCount) {

        TextView statusText = (TextView) getView().findViewById(R.id.search_fragment_status);

        if (resultCount == NO_SEARCH) {
            statusText.setVisibility(View.GONE);
        } else {

            statusText.setVisibility(View.VISIBLE);

            if (resultCount == RESULTS_PENDING) {
                statusText.setText(R.string.search_in_progress_label);
            } else {
                final String resultsStatus = Integer.toString(resultCount)
                        + " "
                        + getActivity().getResources().getString(R.string.search_results_label);
                statusText.setText(resultsStatus);
            }
        }
    }

    // Set up search fields for a selected search plugin module.
    private void selectModule(SearchModule searchModule) {
        currentModule = searchModule;
        setTitle(searchModule.getLabelId());
        updateStatus(NO_SEARCH);
        configureEditTexts();
    }

    private void configureEditTexts() {
        if (currentModule != null) {
            LinearLayout editTextContainer = (LinearLayout) getView().findViewById(R.id.search_fragment_container);
            editTextContainer.removeAllViews();
            for (String columnName : currentModule.getColumnsAndLabels().keySet()) {
                Integer textHintId = currentModule.getColumnsAndLabels().get(columnName);
                EditText editText = makeEditText(getActivity(), textHintId, columnName);
                editTextContainer.addView(editText);
            }
        }
    }

    // Gather column values from the current edit texts, exclude empty text.
    private Map<String, String> gatherColumnValues() {
        Map<String, String> columnValues = new HashMap<>();
        if (currentModule != null) {
            for (String columnName : currentModule.getColumnsAndLabels().keySet()) {
                EditText editText = (EditText) getView().findViewWithTag(columnName);
                String columnValue = editText.getText().toString();
                if (!columnValue.isEmpty()) {
                    columnValues.put(columnName, columnValue);
                }
            }
        }
        return columnValues;
    }

    // Query based on user's text and return result count or code
    @SuppressWarnings("unchecked")
    private int performQuery() {

        int result = NO_SEARCH;

        Map<String, String> columnNamesAndValues = gatherColumnValues();
        int columnCount = columnNamesAndValues.size();
        if (columnCount != 0) {
            // surround the user's text with SQL LIKE wild cards
            List<String> wildCardValues = new ArrayList<>();
            for (String columnValue : columnNamesAndValues.values()) {
                wildCardValues.add(LIKE_WILD_CARD + columnValue + LIKE_WILD_CARD);
            }

            // build a query with those values that the user typed in
            final String[] columnNames = columnNamesAndValues.keySet().toArray(new String[columnCount]);
            final String[] columnValues = wildCardValues.toArray(new String[columnCount]);
            Query query = currentModule.getGateway().findByCriteriaLike(columnNames, columnValues, columnNames[0]);
            List<DataWrapper> dataWrappers = currentModule.getGateway().getQueryResultList(
                    getActivity().getContentResolver(), query, DATA_CATEGORY);

            // report the results to the listener
            if (resultsHandler != null) {
                resultsHandler.handleSearchResults(dataWrappers);
            }

            result = dataWrappers.size();
        }

        return result;
    }

    public interface ResultsHandler {
        void handleSearchResults(List<DataWrapper> dataWrappers);
    }

    // Display a choice of search plugin modules.
    private class SpinnerListAdapter extends ArrayAdapter<SearchModule> {

        public SpinnerListAdapter(Context context, int resource, List<SearchModule> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // make or reuse a text view for this item
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.generic_dropdown_item, null);
            }

            // set the text of this item from the corresponding search module plugin
            final TextView textView = (TextView) convertView;
            SearchModule searchModule = getItem(position);
            textView.setText(searchModule.getLabelId());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    // Set up search fields when the user chooses a plugin module from the spinner.
    private class SpinnerClickHandler implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            SearchModule searchModule = (SearchModule) adapterView.getItemAtPosition(position);
            selectModule(searchModule);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    // Perform the user's search when they click the search button.
    private class ButtonClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // TODO: this RESULTS_PENDING status will never show up.
            // need to mode the searching to an async task to allow
            // the UI to redraw while the search is running
            // Gateway should provide a handy mechanism for this...
            updateStatus(RESULTS_PENDING);
            updateStatus(performQuery());
        }
    }
}
