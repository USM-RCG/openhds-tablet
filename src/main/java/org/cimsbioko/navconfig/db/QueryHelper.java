package org.cimsbioko.navconfig.db;

import org.cimsbioko.data.DataWrapper;

import java.util.List;

public interface QueryHelper {

    List<DataWrapper> getAll(String level);

    List<DataWrapper> getChildren(DataWrapper qr, String childLevel);

    DataWrapper get(String level, String uuid);

    DataWrapper getParent(String level, String uuid);

}
