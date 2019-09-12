package org.cimsbioko.navconfig.forms;

import org.jdom2.Document;

public interface FormBuilder {
    void build(Document blankDataDoc, LaunchContext ctx);
}

