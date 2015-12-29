package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.task.parsing.DataPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert DataPages to an OpenHDS entities.
 *
 * Store the entities in a list to be retrieved in batches.
 *
 * BSH
 */
public abstract class EntityParser <T> {

    private final List<T> entities;

    public EntityParser() {
        entities = new ArrayList<>();
    }

    public List<T> getEntities() {
        return entities;
    }

    public void parsePage(DataPage dataPage) {
        entities.add(toEntity(dataPage));
    }

    protected abstract T toEntity(DataPage dataPage);
}
