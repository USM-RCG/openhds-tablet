package org.openhds.mobile.navconfig.forms.builders;

import android.content.ContentResolver;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.update.Visit;
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

    Visit getCurrentVisit();

    Map<String, DataWrapper> getHierarchyPath();

    List<String> getStateSequence();
}
