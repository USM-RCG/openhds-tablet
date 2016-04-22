package org.openhds.mobile.model.form;

import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.forms.builders.FormPayloadBuilder;
import org.openhds.mobile.navconfig.forms.consumers.DefaultConsumer;
import org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer;
import org.openhds.mobile.navconfig.forms.filters.FormFilter;
import org.openhds.mobile.navconfig.forms.filters.NullFilter;
import org.openhds.mobile.repository.search.EntityFieldSearch;

import java.util.ArrayList;

public class FormBehavior {

    private String formName;
    private String labelKey;
    private FormFilter filter;
    private FormPayloadBuilder builder;
    private FormPayloadConsumer consumer;

    // ArrayList, not just List, because of user with Android Parcelable interface.
    private ArrayList<EntityFieldSearch> searchPluginModules;

    public FormBehavior(String formName, String labelKey, FormFilter filter, FormPayloadBuilder builder,
                        FormPayloadConsumer consumer) {
        this(formName, labelKey, filter, builder, consumer, null);
    }

    public FormBehavior(String formName, String labelKey, FormFilter filter, FormPayloadBuilder builder, FormPayloadConsumer consumer,
                        ArrayList<EntityFieldSearch> searchPluginModules) {
        this.formName = formName;
        this.labelKey = labelKey;
        this.filter = filter;
        this.builder = builder;
        this.consumer = consumer;
        this.searchPluginModules = searchPluginModules;
    }

    public String getFormName() {
        return formName;
    }

    public String getLabel() {
        return NavigatorConfig.getInstance().getString(labelKey);
    }

    public FormFilter getFilter() {
        return filter != null ? filter : NullFilter.INSTANCE;
    }

    public FormPayloadBuilder getBuilder() {
        return builder;
    }

    public FormPayloadConsumer getConsumer() {
        return consumer != null ? consumer : DefaultConsumer.INSTANCE;
    }

    public ArrayList<EntityFieldSearch> getSearchPluginModules() {
        return searchPluginModules;
    }

    public boolean getNeedsFormFieldSearch() {
        return searchPluginModules != null && searchPluginModules.size() > 0;
    }
}