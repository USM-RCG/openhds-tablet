package org.cimsbioko.navconfig.forms;

import org.cimsbioko.navconfig.forms.builders.FormPayloadBuilder;
import org.cimsbioko.navconfig.forms.consumers.FormPayloadConsumer;

public interface Binding {

    String getName();

    String getForm();

    String getLabel();

    FormPayloadBuilder getBuilder();

    FormPayloadConsumer getConsumer();

}
