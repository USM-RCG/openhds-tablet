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

    private static final String TEST_PASSWORD = "";

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

    public void testAddMany() {
        List<T> manyEntities = new ArrayList<>();
        int insertedCount = gateway.insertMany(contentResolver, manyEntities);
        assertEquals(0, insertedCount);

        // many entities to insert
        int nEntities = 10;
        for (int i = 0; i < nEntities; i++) {
            String id = String.format("%05d", i);
            T entity = makeTestEntity(id, "test person");
            manyEntities.add(entity);
        }

        insertedCount = gateway.insertMany(contentResolver, manyEntities);
        assertEquals(nEntities, insertedCount);

        List<T> allEntities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(nEntities, allEntities.size());

        int recordCount = gateway.countAll(contentResolver);
        assertEquals(nEntities, recordCount);
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

    public void testFindAllAsIterator() {
        Iterator<T> allIterator = gateway.getIterator(contentResolver, gateway.findAll());
        assertFalse(allIterator.hasNext());

        Iterator<DataWrapper> allQueryResultsIterator =
                gateway.getQueryResultIterator(contentResolver, gateway.findAll(), "test");
        assertFalse(allQueryResultsIterator.hasNext());

        T entity1 = makeTestEntity("TEST1", "test person");
        T entity2 = makeTestEntity("TEST2", "test person");
        gateway.insertOrUpdate(contentResolver, entity1);
        gateway.insertOrUpdate(contentResolver, entity2);

        // expect both entities to come out, ordered by id
        allIterator = gateway.getIterator(contentResolver, gateway.findAll());
        assertTrue(allIterator.hasNext());
        assertEquals("TEST1", gateway.getConverter().getId(allIterator.next()));
        assertEquals("TEST2", gateway.getConverter().getId(allIterator.next()));
        assertFalse(allIterator.hasNext());

        // expect both query results to come out, ordered by id
        allQueryResultsIterator = gateway.getQueryResultIterator(contentResolver, gateway.findAll(), "test");
        assertTrue(allQueryResultsIterator.hasNext());
        assertEquals("TEST1", allQueryResultsIterator.next().getUuid());
        assertEquals("TEST2", allQueryResultsIterator.next().getUuid());
        assertFalse(allQueryResultsIterator.hasNext());
    }

    public void testFindAllAsIteratorMany() {
        Iterator<T> allIterator = gateway.getIterator(contentResolver, gateway.findAll());
        assertFalse(allIterator.hasNext());

        // insert more entities than the iterator window size
        int windowSize = 10;
        int nEntities = 15;
        for (int i = 0; i < nEntities; i++) {
            String id = String.format("%05d", i);
            T entity = makeTestEntity(id, "test person");
            gateway.insertOrUpdate(contentResolver, entity);
        }

        allIterator = gateway.getIterator(contentResolver, gateway.findAll(), windowSize);

        // expect all entities to come out, ordered by id
        for (int i = 0; i < nEntities; i++) {
            assertTrue(allIterator.hasNext());

            String id = String.format("%05d", i);
            T entity = allIterator.next();
            assertEquals(id, gateway.getConverter().getId(entity));
        }

        assertFalse(allIterator.hasNext());
    }

    public void testDelete() {
        String id = "TEST";
        T entity = makeTestEntity(id, "mr. test");
        gateway.insertOrUpdate(contentResolver, entity);
        T savedEntity = gateway.getFirst(contentResolver, gateway.findById(id));
        assertNotNull(savedEntity);
        String savedId = gateway.getConverter().getId(savedEntity);
        assertEquals(id, savedId);

        boolean wasDeleted = gateway.deleteById(contentResolver, id);
        assertTrue(wasDeleted);

        wasDeleted = gateway.deleteById(contentResolver, id);
        assertFalse(wasDeleted);

        savedEntity = gateway.getFirst(contentResolver, gateway.findById(id));
        assertNull(savedEntity);
    }
}
