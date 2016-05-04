package org.openhds.mobile.navconfig.forms;

import org.openhds.mobile.navconfig.forms.builders.FormPayloadBuilder;
import org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer;
import org.openhds.mobile.repository.search.EntityFieldSearch;

import java.util.List;

public interface Binding {

    String getName();

    String getForm();

    String getLabel();

    FormPayloadBuilder getBuilder();

    FormPayloadConsumer getConsumer();

    boolean requiresSearch();

    List<EntityFieldSearch> getSearches();

}
