package org.openhds.mobile.tests.gateway;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.ProviderTestCase2;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.gateway.Gateway;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class GatewayTest<T> extends ProviderTestCase2<OpenHDSProvider> {

    protected OpenHDSProvider provider;
    protected ContentResolver contentResolver;
    protected Gateway<T> gateway;

    // subclass constructor must provide specific gateway implementation
    public GatewayTest(Gateway<T> gateway) {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
        this.gateway = gateway;
    }

    protected abstract T makeTestEntity(String id, String name);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.provider = getProvider();
        this.contentResolver = getMockContentResolver();

        // make sure we have a fresh database for each test
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper(getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        databaseHelper.onUpgrade(db, 0, 0);
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper(getContext());
        databaseHelper.close();
    }

    public void testSafeToFindWhenEmpty() {
        List<T> allEntities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(0, allEntities.size());

        T entity = gateway.getFirst(contentResolver, gateway.findById("INVALID"));
        assertNull(entity);

        DataWrapper result = gateway.getFirstQueryResult(contentResolver, gateway.findById("INVALID"), "test");
        assertNull(result);
    }

    public void testAdd() {
        String id = "TEST";
        T entity = makeTestEntity(id, "mr. test");

        boolean wasInserted = gateway.insertOrUpdate(contentResolver, entity);
        assertEquals(true, wasInserted);

        T savedEntity = gateway.getFirst(contentResolver, gateway.findById(id));
        assertNotNull(savedEntity);
        String savedId = gateway.getConverter().getId(savedEntity);
        assertEquals(id, savedId);

        DataWrapper savedDataWrapper = gateway.getFirstQueryResult(contentResolver, gateway.findById(id), "test");
        assertNotNull(savedDataWrapper);
        assertEquals(id, savedDataWrapper.getUuid());

        wasInserted = gateway.insertOrUpdate(contentResolver, entity);
        assertEquals(false, wasInserted);

        savedEntity = gateway.getFirst(contentResolver, gateway.findById(id));
        assertNotNull(savedEntity);
        savedId = gateway.getConverter().getId(savedEntity);
        assertEquals(id, savedId);

        List<T> allEntities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(1, allEntities.size());
    }

    public void testFindAll() {
        T entity1 = makeTestEntity("TEST1", "first person");
        T entity2 = makeTestEntity("TEST2", "second person");
        gateway.insertOrUpdate(contentResolver, entity1);
        gateway.insertOrUpdate(contentResolver, entity2);

        List<T> allEntities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, allEntities.size());

        String id1 = gateway.getConverter().getId(allEntities.get(0));
        String id2 = gateway.getConverter().getId(allEntities.get(1));
        assertNotSame(id1, id2);

        List<DataWrapper> allDataWrappers = gateway.getQueryResultList(contentResolver, gateway.findAll(), "test");
        assertEquals(2, allDataWrappers.size());
        assertNotSame(allDataWrappers.get(0).getUuid(), allDataWrappers.get(1).getUuid());
    }
}
