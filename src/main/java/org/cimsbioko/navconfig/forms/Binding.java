package org.cimsbioko.navconfig.forms;

public interface Binding {

    String getName();

    String getForm();

    String getLabel();

    FormBuilder getBuilder();

    FormConsumer getConsumer();

}
