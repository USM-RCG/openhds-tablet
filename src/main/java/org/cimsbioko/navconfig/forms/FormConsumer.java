package org.cimsbioko.navconfig.forms;

import org.jdom2.Document;

public interface FormConsumer {
    boolean consume(Document dataDoc, LaunchContext ctx);
}
