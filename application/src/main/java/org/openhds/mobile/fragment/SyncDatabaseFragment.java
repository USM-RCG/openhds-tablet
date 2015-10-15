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

import org.apache.http.HttpStatus;
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
import org.openhds.mobile.task.parsing.entities.EntityParser;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.MembershipParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.task.parsing.entities.VisitParser;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.SyncUtils.hashFilename;
import static org.openhds.mobile.utilities.SyncUtils.loadHash;
import static org.openhds.mobile.utilities.SyncUtils.storeHash;

/**
 * Allow user to sync tables with the server.
 *
 * Shows a table with sync status and progress for each entity/table.
 * The user may sync one table at a time or queue up all tables at once.
 *
 * BSH
 */
public class SyncDatabaseFragment extends Fragment {

    public enum SyncEntity {

        FIELD_WORKER(R.string.sync_database_label_field_workers,
                R.string.sync_field_workers_path,
                new FieldWorkerParser(),
                GatewayRegistry.getFieldWorkerGateway()),
        VISIT(R.string.sync_database_label_visits,
                R.string.sync_visits_path,
                new VisitParser(),
                GatewayRegistry.getVisitGateway()),
        INDIVIDUAL(R.string.sync_database_label_individuals,
                R.string.sync_individuals_path,
                new IndividualParser(),
                GatewayRegistry.getIndividualGateway()),
        RELATIONSHIP(R.string.sync_database_label_relationships,
                R.string.sync_relationships_path,
                new RelationshipParser(),
                GatewayRegistry.getRelationshipGateway()),
        MEMBERSHIP(R.string.sync_database_label_memberships,
                R.string.sync_memberships_path,
                new MembershipParser(),
                GatewayRegistry.getMembershipGateway()),
        SOCIAL_GROUP(R.string.sync_database_label_social_groups,
                R.string.sync_social_groups_path,
                new SocialGroupParser(),
                GatewayRegistry.getSocialGroupGateway()),
        LOCATION_HIERARCHY(R.string.sync_database_label_location_hierarchies,
                R.string.sync_location_hierarchies_path,
                new LocationHierarchyParser(),
                GatewayRegistry.getLocationHierarchyGateway()),
        LOCATION(R.string.sync_database_label_locations,
                R.string.sync_locations_path,
                new LocationParser(),
                GatewayRegistry.getLocationGateway());

        Integer labelId, pathId;
        EntityParser<?> parser;
        Gateway gateway;
        ParseEntityTaskRequest taskRequest; // mutated, see usage below

        SyncEntity(Integer labelId, Integer pathId, EntityParser<?> parser, Gateway<?> gateway) {
            this.labelId = labelId;
            this.pathId = pathId;
            this.parser = parser;
            this.gateway = gateway;
            this.taskRequest = new ParseEntityTaskRequest(labelId, parser, gateway);
        }
    }

    // placeholder for integer value to ignore
    private static final int IGNORE = -1;
    private static final int UNKNOWN = -2;
    private static final String UNKNOWN_TEXT = "-";

    private HttpTask httpTask;
    private ParseEntityTask parseEntityTask;
    private Queue<SyncEntity> queuedEntityIds;
    private SyncEntity currentEntity;
    private Map<SyncEntity, Integer> allErrorCounts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queuedEntityIds = new ArrayDeque<>();
        allErrorCounts = new HashMap<>();
        currentEntity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sync_database_fragment, container, false);
        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.sync_summary_table);
        View.OnClickListener actionButtonListener = new ActionButtonListener();

        for (SyncEntity entity : SyncEntity.values()) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.sync_database_row, container, false);
            tableRow.setTag(entity);
            tableLayout.addView(tableRow);

            Button actionButton = (Button) tableRow.findViewById(R.id.action_column);
            actionButton.setOnClickListener(actionButtonListener);
            actionButton.setTag(entity);
        }

        Button syncAllButton = (Button) view.findViewById(R.id.sync_all_button);
        syncAllButton.setOnClickListener(new SyncAllButtonListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (SyncEntity entity : SyncEntity.values()) {
            resetTableRow(entity);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        terminateSync(true);
    }

    // Refresh a table with stored data and ready to sync.
    private void resetTableRow(SyncEntity entity) {
        int errors = allErrorCounts.containsKey(entity) ? allErrorCounts.get(entity) : UNKNOWN;
        updateTableRow(entity, queryRecordCount(entity), errors, R.string.sync_database_button_sync);
    }

    // Query the database for entity record counts.
    private int queryRecordCount(SyncEntity entity) {
        return entity.gateway.countAll(getActivity().getContentResolver());
    }

    // Add an entity to the queue to be synced.
    private void enqueueEntity(SyncEntity entity) {

        if (currentEntity == entity || queuedEntityIds.contains(entity)) {
            return;
        }

        // mark the table row for this entity as "waiting"
        updateTableRow(entity, UNKNOWN, UNKNOWN, R.string.sync_database_button_waiting);

        // add this entity to the queue and run it if ready
        queuedEntityIds.add(entity);
        startNextEntity();
    }

    // Take the next entity off the queue and start the sync process.
    private void startNextEntity() {

        if (currentEntity != null || queuedEntityIds.isEmpty()) {
            return;
        }

        currentEntity = queuedEntityIds.remove(); // next entity to sync

        // reset the table row
        allErrorCounts.put(currentEntity, 0);
        updateTableRow(currentEntity, UNKNOWN, 0, R.string.sync_database_button_cancel);

        // start the http task
        httpTask = new HttpTask(new HttpResponseHandler());
        HttpTaskRequest httpTaskRequest = buildHttpTaskRequest(currentEntity);
        httpTask.execute(httpTaskRequest);
    }

    // Pass http data stream to the entity parser.
    private void httpResultToParser(HttpTaskResponse httpTaskResponse) {

        parseEntityTask = new ParseEntityTask(getActivity().getContentResolver());
        parseEntityTask.setProgressListener(new ParseProgressListener());

        ParseEntityTaskRequest parseEntityTaskRequest = currentEntity.taskRequest;
        parseEntityTaskRequest.setInputStream(httpTaskResponse.getInputStream());

        storeContentHash(currentEntity, httpTaskResponse.getETag());
        parseEntityTaskRequest.getGateway().deleteAll(getActivity().getContentResolver());
        parseEntityTask.execute(parseEntityTaskRequest);
    }

    // Clean up after the entity parser is all done.
    private void finishEntity() {
        int records = queryRecordCount(currentEntity);
        updateTableRow(currentEntity, records, allErrorCounts.get(currentEntity), R.string.sync_database_button_sync);
        showProgressMessage(currentEntity, Integer.toString(records));
        terminateSync(false);
    }

    // Clean up tasks.  If a isError is true, counts as an error for the running task.
    private void terminateSync(boolean isError) {

        if (currentEntity != null) {

            int errorCount = allErrorCounts.get(currentEntity);

            if (isError) {
                errorCount++;
                allErrorCounts.put(currentEntity, errorCount);
            }
            updateTableRow(currentEntity, IGNORE, errorCount, R.string.sync_database_button_sync);

            // unhook the parse entity task request from the http input stream
            ParseEntityTaskRequest parseEntityTaskRequest = currentEntity.taskRequest;
            parseEntityTaskRequest.setInputStream(null);
        }

        currentEntity = null;

        if (httpTask != null) {
            httpTask.cancel(true);
            httpTask = null;
        }

        if (parseEntityTask != null) {
            parseEntityTask.cancel(true);
            parseEntityTask = null;
        }

        // proceed to the next entity if any
        startNextEntity();
    }

    // Show an error by logging, and toasting.
    private void showError(SyncEntity entity, int errorCode, String errorMessage) {
        String entityName = getResourceString(getActivity(), entity.labelId);
        String message = "Error syncing " + entityName + " (" + Integer.toString(errorCode) + "):" + errorMessage;
        Log.e(entityName, message);
        showLongToast(getActivity(), message);
    }

    // Show progress by toasting.
    private void showProgressMessage(SyncEntity entity, String progressMessage) {
        String entityName = getResourceString(getActivity(), entity.labelId);
        String message = entityName + ": " + progressMessage;
        showLongToast(getActivity(), message);
    }

    // Update column values and button status.
    private void updateTableRow(SyncEntity entity, int records, int errors, int actionId) {

        View view = getView();
        if (null == view) {
            return;
        }

        TableRow tableRow = (TableRow) view.findViewWithTag(entity);
        TextView entityText = (TextView) tableRow.findViewById(R.id.entity_column);
        TextView recordsText = (TextView) tableRow.findViewById(R.id.records_column);
        TextView errorsText = (TextView) tableRow.findViewById(R.id.errors_column);
        Button actionButton = (Button) tableRow.findViewById(R.id.action_column);

        entityText.setText(entity.labelId);

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
            actionButton.setTag(entity);
        }
    }

    // Create an http task request for fetching data from the server.
    private HttpTaskRequest buildHttpTaskRequest(SyncEntity entity) {

        Bundle extras = getActivity().getIntent().getExtras();
        String userName = (String) extras.get(OpeningActivity.USERNAME_KEY);
        String password = (String) extras.get(OpeningActivity.PASSWORD_KEY);

        String openHdsBaseUrl = getPreferenceString(getActivity(), R.string.openhds_server_url_key, "");
        String path = getResourceString(getActivity(), entity.pathId);
        String url = openHdsBaseUrl + path;

        return new HttpTaskRequest(entity.labelId, url, "application/xml", userName, password, loadContentHash(entity));
    }

    private File getAppFile(String file) {
        return new File(getActivity().getFilesDir(), file);
    }

    private String loadContentHash(SyncEntity entity) {
        return loadHash(getAppFile(hashFilename(entity.name())));
    }

    private void storeContentHash(SyncEntity entity, String hash) {
        storeHash(getAppFile(hashFilename(entity.name())), hash);
    }

    // Respond to "sync all" button.
    private class SyncAllButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            for (SyncEntity entity : SyncEntity.values()) {
                enqueueEntity(entity);
            }
        }
    }

    // Respond to individual entity "sync" buttons.
    private class ActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // which button is this?
            SyncEntity entity = (SyncEntity) view.getTag();
            if (entity == null) {
                return;
            }

            if (entity == currentEntity) {
                // button should change from "cancel" to "sync"
                terminateSync(true);
                showProgressMessage(entity, getResourceString(getActivity(), R.string.sync_database_canceled));

            } else if (queuedEntityIds.contains(entity)) {
                // button should change "waiting" to "sync"
                queuedEntityIds.remove(entity);
                resetTableRow(entity);

            } else {
                // button should change from "sync" to "waiting"
                enqueueEntity(entity);
            }
        }
    }

    // Receive http response from server, or error data.
    private class HttpResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (httpTaskResponse.isSuccess()) {
                httpResultToParser(httpTaskResponse);
            } else if (httpTaskResponse.getHttpStatus() == HttpStatus.SC_NOT_MODIFIED) {
                showProgressMessage(currentEntity, httpTaskResponse.getMessage());
                terminateSync(false);
            } else {
                showError(currentEntity, httpTaskResponse.getHttpStatus(), httpTaskResponse.getMessage());
                terminateSync(true);
            }
        }
    }

    // Receive progress reports from parser, or error data.
    private class ParseProgressListener implements ParseEntityTask.ProgressListener {
        @Override
        public void onProgressReport(int progress) {
            updateTableRow(currentEntity, progress, IGNORE, IGNORE);
        }

        @Override
        public void onError(DataPage dataPage, Exception e) {
            int errorCount = allErrorCounts.get(currentEntity);
            errorCount++;
            allErrorCounts.put(currentEntity, errorCount);
            updateTableRow(currentEntity, IGNORE, errorCount, IGNORE);
            showError(currentEntity, 0, e.getMessage());
            storeContentHash(currentEntity, null);
        }

        @Override
        public void onComplete(int progress) {
            finishEntity();
        }
    }
}
