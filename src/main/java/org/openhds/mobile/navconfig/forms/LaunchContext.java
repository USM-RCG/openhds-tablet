package org.openhds.mobile.navconfig.forms;

import android.content.ContentResolver;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.navconfig.forms.consumers.ConsumerResults;
import org.openhds.mobile.repository.DataWrapper;

import java.util.List;
import java.util.Map;

/**
 * This is the formal contract currently required to build a payload and launch a form. Currently,
 * {@link HierarchyNavigatorActivity} implements this contract directly. However, this ensures that
 * that dependency can be easily identified and, if necessary, decoupled.
 */
public interface LaunchContext {

    ContentResolver getContentResolver();

    FieldWorker getCurrentFieldWorker();

    DataWrapper getCurrentSelection();

    Map<String, DataWrapper> getHierarchyPath();

    List<String> getStateSequence();

    void startVisit(Visit v);

    Visit getCurrentVisit();

    void finishVisit();

    ConsumerResults getConsumerResult();

}
