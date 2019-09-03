package org.cimsbioko.navconfig.forms;

public interface Binding {

    String getName();

    String getForm();

    String getLabel();

    FormPayloadBuilder getBuilder();

    FormPayloadConsumer getConsumer();

}
