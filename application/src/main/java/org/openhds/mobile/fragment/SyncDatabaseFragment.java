package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;
import org.openhds.mobile.task.parsing.DataPage;
import org.openhds.mobile.task.parsing.ParseEntityTask;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.MembershipParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.task.parsing.entities.VisitParser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

/**
 * Allow user to sync tables with the server.
 *
 * Shows a table with sync status and progress for each entity/table.
 * The user may sync one table at a time or queue up all tables at once.
 *
 * BSH
 */
public class SyncDatabaseFragment extends Fragment {

    // placeholder for integer value to ignore
    private static final int IGNORE = -1;
    private static final int UNKNOWN = -2;
    private static final String UNKNOWN_TEXT = "-";

    // list of entities for "sync all"
    private static final List<Integer> allEntityIds;

    // http paths associated with entities
    private static final Map<Integer, Integer> allResourcePaths;

    // parsing requests associated entities
    private static final Map<Integer, ParseEntityTaskRequest> allParseTaskRequests;

    // one-time wiring up of labels, http paths, and parser stuff
    static {
        allEntityIds = new ArrayList<>();
        allEntityIds.add(R.string.sync_database_label_field_workers);
        allEntityIds.add(R.string.sync_database_label_visits);
        allEntityIds.add(R.string.sync_database_label_individuals);
        allEntityIds.add(R.string.sync_database_label_relationships);
        allEntityIds.add(R.string.sync_database_label_memberships);
        allEntityIds.add(R.string.sync_database_label_social_groups);
        allEntityIds.add(R.string.sync_database_label_location_hierarchies);
        allEntityIds.add(R.string.sync_database_label_locations);

        allResourcePaths = new HashMap<>();
        allResourcePaths.put(R.string.sync_database_label_field_workers, R.string.sync_field_workers_path);
        allResourcePaths.put(R.string.sync_database_label_individuals, R.string.sync_individuals_path);
        allResourcePaths.put(R.string.sync_database_label_locations, R.string.sync_locations_path);
        allResourcePaths.put(R.string.sync_database_label_location_hierarchies, R.string.sync_location_hierarchies_path);
        allResourcePaths.put(R.string.sync_database_label_memberships, R.string.sync_memberships_path);
        allResourcePaths.put(R.string.sync_database_label_relationships, R.string.sync_relationships_path);
        allResourcePaths.put(R.string.sync_database_label_social_groups, R.string.sync_social_groups_path);
        allResourcePaths.put(R.string.sync_database_label_visits, R.string.sync_visits_path);

        allParseTaskRequests = new HashMap<>();
        allParseTaskRequests.put(R.string.sync_database_label_field_workers, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_field_workers,
                null,
                new FieldWorkerParser(),
                GatewayRegistry.getFieldWorkerGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_individuals, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_individuals,
                null,
                new IndividualParser(),
                GatewayRegistry.getIndividualGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_locations, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_locations,
                null,
                new LocationParser(),
                GatewayRegistry.getLocationGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_location_hierarchies, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_location_hierarchies,
                null,
                new LocationHierarchyParser(),
                GatewayRegistry.getLocationHierarchyGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_memberships, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_memberships,
                null,
                new MembershipParser(),
                GatewayRegistry.getMembershipGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_relationships, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_relationships,
                null,
                new RelationshipParser(),
                GatewayRegistry.getRelationshipGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_social_groups, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_social_groups,
                null,
                new SocialGroupParser(),
                GatewayRegistry.getSocialGroupGateway()));
        allParseTaskRequests.put(R.string.sync_database_label_visits, new ParseEntityTaskRequest<>(
                R.string.sync_database_label_visits,
                null,
                new VisitParser(),
                GatewayRegistry.getVisitGateway()));
    }

    private HttpTask httpTask;
    private ParseEntityTask parseEntityTask;
    private Queue<Integer> queuedEntityIds;
    private int currentEntityId;
    private Map<Integer, Integer> allErrorCounts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queuedEntityIds = new ArrayDeque<>();
        allErrorCounts = new HashMap<>();
        currentEntityId = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sync_database_fragment, container, false);

        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.sync_summary_table);
        View.OnClickListener actionButtonListener = new ActionButtonListener();
        for (int entityId : allEntityIds) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.sync_database_row, container, false);
            tableRow.setTag(entityId);
            tableLayout.addView(tableRow);

            Button actionButton = (Button) tableRow.findViewById(R.id.action_column);
            actionButton.setOnClickListener(actionButtonListener);
            actionButton.setTag(entityId);
        }

        Button syncAllButton = (Button) view.findViewById(R.id.sync_all_button);
        syncAllButton.setOnClickListener(new SyncAllButtonListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update entity record counts directly from the database
        for (int entityId : allEntityIds) {
            int records = queryRecordCount(entityId);
            int errors = allErrorCounts.containsKey(entityId) ? allErrorCounts.get(entityId) : 0;
            updateTableRow(entityId, records, errors, IGNORE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelSync();
    }

    // Add an entity to the queue to be synced.
    private void enqueueEntity(int entityId) {
        if (currentEntityId == entityId || queuedEntityIds.contains(entityId)) {
            return;
        }

        // mark the table row for this entity as "waiting"
        updateTableRow(entityId, IGNORE, IGNORE, R.string.sync_database_button_waiting);

        // add this entity to the queue and run it if ready
        queuedEntityIds.add(entityId);
        syncNextEntity();
    }

    // Take the next entity off the queue and start the sync process.
    private void syncNextEntity() {
        if (0 < currentEntityId || queuedEntityIds.isEmpty()) {
            return;
        }

        // choose the next entity to sync
        currentEntityId = queuedEntityIds.remove();

        // reset the table row for this entity
        int errorCount = 0;
        allErrorCounts.put(currentEntityId, errorCount);
        updateTableRow(currentEntityId, UNKNOWN, errorCount, R.string.sync_database_button_cancel);

        // start an http task for this entity
        httpTask = new HttpTask(new HttpResponseHandler());
        HttpTaskRequest httpTaskRequest = buildHttpTaskRequest(currentEntityId);
        httpTask.execute(httpTaskRequest);
    }

    // Pass http data stream to the entity parser.
    private void httpResultToParser(HttpTaskResponse httpTaskResponse) {
        parseEntityTask = new ParseEntityTask(getActivity().getContentResolver());
        parseEntityTask.setProgressListener(new ParseProgressListener());

        ParseEntityTaskRequest parseEntityTaskRequest = allParseTaskRequests.get(currentEntityId);
        parseEntityTaskRequest.setInputStream(httpTaskResponse.getInputStream());

        parseEntityTaskRequest.getGateway().deleteAll(getActivity().getContentResolver());
        parseEntityTask.execute(parseEntityTaskRequest);
    }

    // Clean up after the entity parser is all done.
    private void finishEntity() {
        int records = queryRecordCount(currentEntityId);
        updateTableRow(currentEntityId, records, allErrorCounts.get(currentEntityId), R.string.sync_database_button_sync);
        showProgressMessage(currentEntityId, Integer.toString(records));
        cancelSync();
    }

    // Clean up after parsing is canceled by user or finishes naturally.
    private void cancelSync() {
        if (0 < currentEntityId) {
            updateTableRow(currentEntityId, IGNORE, allErrorCounts.get(currentEntityId), R.string.sync_database_button_sync);
            ParseEntityTaskRequest parseEntityTaskRequest = allParseTaskRequests.get(currentEntityId);
            parseEntityTaskRequest.setInputStream(null);
        }
        currentEntityId = 0;

        if (null != httpTask) {
            httpTask.cancel(true);
        }
        httpTask = null;

        if (null != parseEntityTask) {
            parseEntityTask.cancel(true);
        }
        parseEntityTask = null;

        // proceed to the next entity if any
        syncNextEntity();
    }

    // Show an error by logging, and toasting.
    private void showError(int entityId, int errorCode, String errorMessage) {
        String entityName = getResourceString(getActivity(), entityId);
        String message = "Error syncing " + entityName + " (" + Integer.toString(errorCode) + "):" + errorMessage;
        Log.e(entityName, message);
        showLongToast(getActivity(), message);
    }

    private void showProgressMessage(int entityId, String progressMessage) {
        String entityName = getResourceString(getActivity(), entityId);
        String message = entityName + ": " + progressMessage;
        showLongToast(getActivity(), message);
    }

    // Query the database for entity record counts.
    private int queryRecordCount(int entityId) {
        ParseEntityTaskRequest parseEntityTaskRequest = allParseTaskRequests.get(entityId);
        Gateway gateway = parseEntityTaskRequest.getGateway();
        return gateway.countAll(getActivity().getContentResolver());
    }

    // Update column values and button status.
    private void updateTableRow(int entityId, int records, int errors, int actionId) {
        View view = getView();
        if (null == view) {
            return;
        }

        TableRow tableRow = (TableRow) view.findViewWithTag(entityId);
        TextView entityText = (TextView) tableRow.findViewById(R.id.entity_column);
        TextView recordsText = (TextView) tableRow.findViewById(R.id.records_column);
        TextView errorsText = (TextView) tableRow.findViewById(R.id.errors_column);
        Button actionButton = (Button) tableRow.findViewById(R.id.action_column);

        entityText.setText(entityId);

        if (IGNORE != records) {
            if (UNKNOWN == records) {
                recordsText.setText(UNKNOWN_TEXT);
            } else {
                recordsText.setText(Integer.toString(records));
            }
        }

        if (IGNORE != errors) {
            if (UNKNOWN == errors) {
                errorsText.setText(UNKNOWN_TEXT);
            } else {
                errorsText.setText(Integer.toString(errors));
            }
        }

        if (IGNORE != actionId) {
            actionButton.setText(actionId);
            actionButton.setTag(entityId);
        }
    }

    // Create an http task request for fetching data from the server.
    private HttpTaskRequest buildHttpTaskRequest(int entityId) {
        String userName = (String) getActivity().getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getActivity().getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);

        String openHdsBaseUrl = getPreferenceString(getActivity(), R.string.openhds_server_url_key, "");
        String path = getResourceString(getActivity(), allResourcePaths.get(entityId));
        String url = openHdsBaseUrl + path;
        return new HttpTaskRequest(entityId, url, userName, password);
    }

    // Respond to "sync all" button.
    private class SyncAllButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            for (int entityId : allEntityIds) {
                enqueueEntity(entityId);
            }
        }
    }

    // Respond to individual entity "sync" buttons.
    private class ActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // which button is this?
            int entityId = (int) view.getTag();
            if (!allEntityIds.contains(entityId)) {
                return;
            }

            if (entityId == currentEntityId) {
                // button should change from "cancel" to "sync"
                cancelSync();
                showProgressMessage(entityId, getResourceString(getActivity(), R.string.sync_database_canceled));

            } else if (queuedEntityIds.contains(entityId)) {
                // button should change "waiting" to "sync"
                queuedEntityIds.remove(entityId);

            } else {
                // button should change from "sync" to "waiting"
                enqueueEntity(entityId);
            }
        }
    }


    // Receive http response from server, or error data.
    private class HttpResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (!httpTaskResponse.isSuccess()) {
                int errorCount = allErrorCounts.get(currentEntityId) + 1;
                allErrorCounts.put(currentEntityId, errorCount);
                showError(currentEntityId, httpTaskResponse.getHttpStatus(), httpTaskResponse.getMessage());
                cancelSync();
                return;
            }
            httpResultToParser(httpTaskResponse);
        }
    }

    // Receive progress reports from parser, or error data.
    private class ParseProgressListener implements ParseEntityTask.ProgressListener {
        @Override
        public void onProgressReport(int progress) {
            updateTableRow(currentEntityId, progress, IGNORE, IGNORE);
        }

        @Override
        public void onError(DataPage dataPage, Exception e) {
            int errorCount = allErrorCounts.get(currentEntityId) + 1;
            allErrorCounts.put(currentEntityId, errorCount);
            updateTableRow(currentEntityId, IGNORE, errorCount, IGNORE);
            showError(currentEntityId, 0, e.getMessage());
        }

        @Override
        public void onComplete(int progress) {
            finishEntity();
        }
    }
}
