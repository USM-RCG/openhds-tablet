package org.openhds.mobile.navconfig.forms;

import android.content.ContentResolver;
import android.content.Context;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.navconfig.forms.consumers.ConsumerResult;
import org.openhds.mobile.repository.DataWrapper;

/**
 * This is the formal contract currently required to build a payload and launch a form. Currently,
 * {@link HierarchyNavigatorActivity} implements this contract directly. However, this ensures that
 * that dependency can be easily identified and, if necessary, decoupled.
 */
public interface LaunchContext {

    Context getApplicationContext();

    ContentResolver getContentResolver();

    FieldWorker getCurrentFieldWorker();

    DataWrapper getCurrentSelection();

    HierarchyPath getHierarchyPath();

    void startVisit(Visit v);

    Visit getCurrentVisit();

    void finishVisit();

    ConsumerResult getConsumerResult();

}
