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
import org.openhds.mobile.task.parsing.DataPage;
import org.openhds.mobile.task.parsing.ParseTask;
import org.openhds.mobile.task.parsing.ParseRequest;
import org.openhds.mobile.task.parsing.entities.EntityParser;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.MembershipParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.task.parsing.entities.VisitParser;
import org.openhds.mobile.task.sync.SyncRequest;
import org.openhds.mobile.task.sync.SyncResult;
import org.openhds.mobile.task.sync.SyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.SyncUtils.XML_MIME_TYPE;
import static org.openhds.mobile.utilities.SyncUtils.entityFilename;
import static org.openhds.mobile.utilities.SyncUtils.hashFilename;
import static org.openhds.mobile.utilities.SyncUtils.loadHash;
import static org.openhds.mobile.utilities.SyncUtils.storeHash;
import static org.openhds.mobile.utilities.SyncUtils.tempFilename;

/**
 * Allow user to sync tables with the server.
 * <p/>
 * Shows a table with sync status and progress for each entity/table.
 * The user may sync one table at a time or queue up all tables at once.
 * <p/>
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
        ParseRequest taskRequest; // mutated, see usage below

        SyncEntity(Integer labelId, Integer pathId, EntityParser<?> parser, Gateway<?> gateway) {
            this.labelId = labelId;
            this.pathId = pathId;
            this.parser = parser;
            this.gateway = gateway;
            this.taskRequest = new ParseRequest(labelId, parser, gateway);
        }
    }

    // placeholder for integer value to ignore
    private static final int IGNORE = -1;
    private static final int UNKNOWN = -2;
    private static final String UNKNOWN_TEXT = "-";

    private SyncTask syncTask;
    private ParseTask parseTask;
    private Queue<SyncEntity> syncQueue;
    private SyncEntity syncEntity;
    private Map<SyncEntity, Integer> errorCounts;
    private String contentHash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        syncQueue = new ArrayDeque<>();
        errorCounts = new HashMap<>();
        syncEntity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sync_database_fragment, container, false);
        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.sync_summary_table);
        View.OnClickListener actionButtonListener = new ActionButtonListener();

        for (SyncEntity entity : SyncEntity.values()) {

            // Add a row for sychronizable entity
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.sync_database_row, container, false);
            tableRow.setTag(entity);
            tableRow.setPadding(0, 0, 0, 5);
            tableLayout.addView(tableRow);

            // Hookup sync/cancel button
            Button actionButton = (Button) tableRow.findViewById(R.id.action_column);
            actionButton.setOnClickListener(actionButtonListener);
            actionButton.setTag(entity);
        }

        Button syncAllButton = (Button) view.findViewById(R.id.sync_all_button);
        syncAllButton.setOnClickListener(new SyncAllButtonListener());

        Button clearCacheButton = (Button) view.findViewById(R.id.clear_cache_button);
        clearCacheButton.setOnClickListener(new ClearCacheButtonListener());

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
        int errors = errorCounts.containsKey(entity) ? errorCounts.get(entity) : UNKNOWN;
        updateTableRow(entity, UNKNOWN, errors, R.string.sync_database_button_sync);
    }

    // Query the database for entity record counts.
    private int queryRecordCount(SyncEntity entity) {
        return entity.gateway.countAll(getActivity().getContentResolver());
    }

    // Add an entity to the queue to be synced.
    private void queueSync(SyncEntity entity) {

        if (syncEntity == entity || syncQueue.contains(entity)) {
            return;
        }

        // mark the table row for this entity as "waiting"
        updateTableRow(entity, UNKNOWN, UNKNOWN, R.string.sync_database_button_waiting);

        // add this entity to the queue and run it if ready
        syncQueue.add(entity);
        startNextEntity();
    }

    // Take the next entity off the queue and start the sync process.
    private void startNextEntity() {

        if (syncEntity != null || syncQueue.isEmpty()) {
            return;
        }

        syncEntity = syncQueue.remove(); // next entity to sync

        // reset the table row
        errorCounts.put(syncEntity, 0);
        updateTableRow(syncEntity, UNKNOWN, 0, R.string.sync_database_button_cancel);

        // start the http task
        syncTask = new SyncTask(new SyncListener());
        try {
            SyncRequest syncRequest = buildSyncRequest(syncEntity);
            syncTask.execute(syncRequest);
        } catch (MalformedURLException e) {
            String urlError = getResourceString(
                    getActivity(), R.string.url_error) + ": " + e.getMessage();
            showShortToast(getActivity(), urlError);
            terminateSync(true);
        }
    }

    private class SyncListener implements SyncTask.Listener {

        @Override
        public void handleResult(SyncResult result) {
            switch (result.getType()) {
                case FULL:
                case INCREMENTAL:
                    try {
                        contentHash = result.getETag(); // Used after parse finishes
                        flipFile();
                        startParse(new FileInputStream(getBasisFile(syncEntity)));
                    } catch (FileNotFoundException e) {
                        showShortToast(getActivity(), e.getMessage());
                        terminateSync(true);
                    }
                    break;
                case NO_UPDATE:
                    setEntityStatus("No update");
                    showProgressMessage(syncEntity, result.toString());
                    terminateSync(false);
                    break;
                default:
                    showError(syncEntity, result.toString());
                    terminateSync(true);
            }
        }

        @Override
        public void handleProgress(String status) {
            setEntityStatus(status);
        }

        /**
         * Replaces the basis file with the one constructed by the sync process.
         * This guarantees that the temp storage is cleaned up and that the parse
         * can only happen on complete files.
         * @return true if the 'flip' succeeds
         */
        private boolean flipFile() {
            return getTargetFile(syncEntity).renameTo(getBasisFile(syncEntity));
        }

        private void startParse(InputStream input) {
            parseTask = new ParseTask(getActivity().getContentResolver());
            parseTask.setListener(new ParseListener());
            ParseRequest parseRequest = syncEntity.taskRequest;
            parseRequest.setInputStream(input);
            setEntityStatus("Parsing");
            parseTask.execute(parseRequest);
        }
    }

    // Clean up after the entity parser is all done.
    private void entityComplete(int records) {
        setEntityStatus("Complete");
        updateTableRow(syncEntity, IGNORE, errorCounts.get(syncEntity), R.string.sync_database_button_sync);
        showProgressMessage(syncEntity, Integer.toString(records));
        storeContentHash(syncEntity, contentHash);  // Next call changes entity
        terminateSync(false);
    }

    private void entityError(Exception e) {
        int errorCount = errorCounts.get(syncEntity) + 1;
        errorCounts.put(syncEntity, errorCount);
        updateTableRow(syncEntity, IGNORE, errorCount, IGNORE);
        showError(syncEntity, e.getMessage());
        storeContentHash(syncEntity, null);
    }

    // Clean up tasks.  If a isError is true, counts as an error for the running task.
    private void terminateSync(boolean isError) {

        if (syncEntity != null) {

            int errorCount = errorCounts.get(syncEntity);

            if (isError) {
                errorCount++;
                errorCounts.put(syncEntity, errorCount);
            }
            updateTableRow(syncEntity, IGNORE, errorCount, R.string.sync_database_button_sync);

            // unhook the parse entity task request from the http input stream
            ParseRequest parseRequest = syncEntity.taskRequest;
            parseRequest.setInputStream(null);
        }

        syncEntity = null;

        if (syncTask != null) {
            syncTask.cancel(true);
            syncTask = null;
        }

        if (parseTask != null) {
            parseTask.cancel(true);
            parseTask = null;
        }

        // proceed to the next entity if any
        startNextEntity();
    }

    // Show an error by logging, and toasting.
    private void showError(SyncEntity entity, String errorMessage) {
        String entityName = getResourceString(getActivity(), entity.labelId);
        String message = "Error syncing " + entityName + ":" + errorMessage;
        Log.e(entityName, message);
        showShortToast(getActivity(), message);
    }

    // Show progress by toasting.
    private void showProgressMessage(SyncEntity entity, String progressMessage) {
        String entityName = getResourceString(getActivity(), entity.labelId);
        String message = entityName + ": " + progressMessage;
        showShortToast(getActivity(), message);
    }

    private void setEntityStatus(String status) {
        View view = getView();
        if (null == view) {
            return;
        }
        TableRow tableRow = (TableRow) view.findViewWithTag(syncEntity);
        TextView recordsText = (TextView) tableRow.findViewById(R.id.records_column);
        recordsText.setText(status);
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

    /**
     * Builds a sync request for efficiently updating local database content.
     */
    private SyncRequest buildSyncRequest(SyncEntity entity) throws MalformedURLException {
        Bundle extras = getActivity().getIntent().getExtras();
        String username = (String) extras.get(OpeningActivity.USERNAME_KEY);
        String password = (String) extras.get(OpeningActivity.PASSWORD_KEY);
        File basis = getBasisFile(entity), target = getTargetFile(entity);
        return new SyncRequest(getEndpoint(entity), username, password, basis,
                target, XML_MIME_TYPE, loadContentHash(entity));
    }

    private URL getEndpoint(SyncEntity entity) throws MalformedURLException {
        String openHdsBaseUrl = getPreferenceString(getActivity(), R.string.openhds_server_url_key, "");
        String path = getResourceString(getActivity(), entity.pathId);
        return new URL(openHdsBaseUrl + path);
    }

    private File getAppFile(String file) {
        return new File(getActivity().getFilesDir(), file);
    }

    private File getBasisFile(SyncEntity entity) {
        return getAppFile(entityFilename(entity.name()));
    }

    private File getTargetFile(SyncEntity entity) {
        return getAppFile(tempFilename(entity.name()));
    }

    private String loadContentHash(SyncEntity entity) {
        return loadHash(getAppFile(hashFilename(entity.name())));
    }

    private void storeContentHash(SyncEntity entity, String hash) {
        storeHash(getAppFile(hashFilename(entity.name())), hash);
    }

    private void clearContentHashes() {
        for (SyncEntity entity : SyncEntity.values()) {
            storeHash(getAppFile(hashFilename(entity.name())), null);
        }
    }

    // Respond to "sync all" button.
    private class SyncAllButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            for (SyncEntity entity : SyncEntity.values()) {
                queueSync(entity);
            }
        }
    }

    private class ClearCacheButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            clearContentHashes();
            showShortToast(getActivity(), R.string.sync_database_cache_cleared);
        }
    }

    /**
     * Handles click of an individual entity "sync" button.
     */
    private class ActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            SyncEntity entity = (SyncEntity) view.getTag();
            if (entity != null) {
                if (isSyncing(entity)) {
                    cancelCurrentSync();
                } else if (isQueued(entity)) {
                    cancelQueuedSync(entity);
                } else {
                    queueSync(entity);
                }
            }
        }

        private boolean isSyncing(SyncEntity entity) {
            return entity == syncEntity;
        }

        private void cancelCurrentSync() {
            setEntityStatus("Canceled");
            showProgressMessage(syncEntity, getResourceString(getActivity(), R.string.sync_database_canceled));
            terminateSync(true);
        }

        private boolean isQueued(SyncEntity entity) {
            return syncQueue.contains(entity);
        }

        private void cancelQueuedSync(SyncEntity entity) {
            syncQueue.remove(entity);
            resetTableRow(entity);
        }
    }

    /**
     * Handles updates from parse task and updates UI/model state accordingly.
     */
    private class ParseListener implements ParseTask.Listener {
        @Override
        public void onProgress(int progress) {
            updateTableRow(syncEntity, progress, IGNORE, IGNORE);
        }

        @Override
        public void onError(DataPage dataPage, Exception e) {
            entityError(e);
        }

        @Override
        public void onComplete(int progress) {
            entityComplete(progress);
        }
    }
}
