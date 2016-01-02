package org.openhds.mobile.tests.parsing;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.ProviderTestCase2;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;
import org.openhds.mobile.task.parsing.ParseRequest;
import org.openhds.mobile.task.parsing.ParseTask;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.MembershipParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.task.parsing.entities.VisitParser;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openhds.mobile.tests.util.TestUtil.getTestContext;

/**
 * Feed XML documents to a ParseTask and verify that entities were created in the database.
 *
 * BSH
 */
public class ParseTaskTest extends ProviderTestCase2<OpenHDSProvider> {

    private static final String TEST_PASSWORD = "";
    private static final int TASK_TIMEOUT = 10;

    private OpenHDSProvider provider;
    private ContentResolver contentResolver;
    private ParseTask parseTask;

    private class ConstantPasswordHelper implements PasswordHelper {
        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

    public ParseTaskTest() {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.provider = getProvider();
        this.contentResolver = getMockContentResolver();

        // inject a password helper that uses a known password
        // and doesn't use shared preferences (which are not enabled under ProviderTestCase2)
        provider.setPasswordHelper(new ConstantPasswordHelper());

        // make sure we have a fresh database for each test
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        databaseHelper.onUpgrade(db, 0, 0);

        // test the task against the mock content resolver with no progress dialog
        parseTask = new ParseTask(contentResolver);
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.close();
    }

    public void testProcessFieldWorkerXml() throws Exception {
        String fileName = "testXml/field-workers.xml";
        FieldWorkerGateway gateway = GatewayRegistry.getFieldWorkerGateway();
        ParseRequest<FieldWorker> parseRequest = new ParseRequest<>(
                0,
                new FieldWorkerParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<FieldWorker> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(7, entities.size());
    }

    public void testProcessIndividualXml() throws Exception {
        String fileName = "testXml/individuals.xml";
        IndividualGateway gateway = GatewayRegistry.getIndividualGateway();
        ParseRequest<Individual> parseRequest = new ParseRequest<>(
                0,
                new IndividualParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Individual> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessLocationHierarchyXml() throws Exception {
        String fileName = "testXml/location-hierarchies.xml";
        LocationHierarchyGateway gateway = GatewayRegistry.getLocationHierarchyGateway();
        ParseRequest<LocationHierarchy> parseRequest = new ParseRequest<>(
                0,
                new LocationHierarchyParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<LocationHierarchy> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessLocationXml() throws Exception {
        String fileName = "testXml/locations.xml";
        LocationGateway gateway = GatewayRegistry.getLocationGateway();
        ParseRequest<Location> parseRequest = new ParseRequest<>(
                0,
                new LocationParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Location> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessRelationshipXml() throws Exception {
        String fileName = "testXml/relationships.xml";
        RelationshipGateway gateway = GatewayRegistry.getRelationshipGateway();
        ParseRequest<Relationship> parseRequest = new ParseRequest<>(
                0,
                new RelationshipParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Relationship> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessSocialGroupXml() throws Exception {
        String fileName = "testXml/social-groups.xml";
        SocialGroupGateway gateway = GatewayRegistry.getSocialGroupGateway();
        ParseRequest<SocialGroup> parseRequest = new ParseRequest<>(
                0,
                new SocialGroupParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<SocialGroup> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessVisitXml() throws Exception {
        String fileName = "testXml/visits.xml";
        VisitGateway gateway = GatewayRegistry.getVisitGateway();
        ParseRequest<Visit> parseRequest = new ParseRequest<>(
                0,
                new VisitParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Visit> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessMembershipXml() throws Exception {
        String fileName = "testXml/memberships.xml";
        MembershipGateway gateway = GatewayRegistry.getMembershipGateway();
        ParseRequest<Membership> parseRequest = new ParseRequest<>(
                0,
                new MembershipParser(),
                gateway);
        parseRequest.setInputStream(getTestContext(this).getAssets().open(fileName));

        // run the task and wait for it to finish
        parseTask.execute(parseRequest);
        parseTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Membership> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }
}
