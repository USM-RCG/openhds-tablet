package org.openhds.mobile.model.form;

import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.forms.builders.FormPayloadBuilder;
import org.openhds.mobile.navconfig.forms.consumers.DefaultConsumer;
import org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer;
import org.openhds.mobile.navconfig.forms.filters.FormFilter;
import org.openhds.mobile.navconfig.forms.filters.NullFilter;
import org.openhds.mobile.repository.search.EntityFieldSearch;

import java.util.ArrayList;

import static java.util.Arrays.asList;

public class FormBehavior {

    private String formName;
    private String labelKey;
    private FormFilter filter;
    private FormPayloadBuilder builder;
    private FormPayloadConsumer consumer;
    private ArrayList<EntityFieldSearch> requiredSearches;  // Using concrete type for convenient use with Parcelable

    public FormBehavior(String formName, String labelKey, FormFilter filter, FormPayloadBuilder builder,
                        FormPayloadConsumer consumer, EntityFieldSearch... requiredSearches) {
        this.formName = formName;
        this.labelKey = labelKey;
        this.filter = filter;
        this.builder = builder;
        this.consumer = consumer;
        this.requiredSearches = new ArrayList<>();
        if (requiredSearches != null) {
            this.requiredSearches.addAll(asList(requiredSearches));
        }
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

    public ArrayList<EntityFieldSearch> getRequiredSearches() {
        return requiredSearches;
    }

    public boolean requiresSearch() {
        return requiredSearches.size() > 0;
    }
}