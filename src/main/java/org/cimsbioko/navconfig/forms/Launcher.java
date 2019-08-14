package org.cimsbioko.navconfig.forms;

public interface Launcher {

    String getLabel();

    boolean relevantFor(LaunchContext ctx);

    Binding getBinding();

}
