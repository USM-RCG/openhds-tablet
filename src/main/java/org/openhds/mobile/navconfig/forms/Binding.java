package org.openhds.mobile.navconfig.forms;

import org.openhds.mobile.navconfig.forms.builders.FormPayloadBuilder;
import org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer;

public interface Binding {

    String getName();

    String getForm();

    String getLabel();

    FormPayloadBuilder getBuilder();

    FormPayloadConsumer getConsumer();

}
