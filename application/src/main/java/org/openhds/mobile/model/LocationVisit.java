package org.openhds.mobile.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.database.Cursor;

/**
 * A LocationVisit represents a single visit to a specific location. This class
 * is built incrementally, meaning first the user constructs the location
 * hierarchy selection (region, sub-region, village, and round), one item at a
 * time. Then they must either set or create a location. After a location is
 * set, they can proceed to create a visit. Once a visit is created, the
 * LocationVisit cannot be changed, that is, a visit is considered started. Only
 * new events can be added to the location visit at this point. Once the user
 * completes a visit, a new LocationVisit is returned that contains pre-filled
 * location hierarchy selection (region, sub-region, village and round) assuming
 * the field worker will work in the same area.
 */
public class LocationVisit implements Serializable {

    private static final long serialVersionUID = -36602612353821830L;

    private FieldWorker fieldWorker;
    private LocationHierarchy hierarchy1;
    private LocationHierarchy hierarchy2;
    private LocationHierarchy hierarchy3;
    private LocationHierarchy hierarchy4;
    private LocationHierarchy hierarchy5;
    private Round round;

    private Location location;
    private Visit visit;

    private Individual selectedIndividual;

    public LocationVisit completeVisit() {
        LocationVisit visit = new LocationVisit();
        visit.fieldWorker = fieldWorker;
        visit.hierarchy1 = hierarchy1;
        visit.hierarchy2 = hierarchy2;
        visit.hierarchy3 = hierarchy3;
        visit.hierarchy4 = hierarchy4;
        visit.hierarchy5 = hierarchy5;
        visit.round = round;

        return visit;
    }

    public FieldWorker getFieldWorker() {
        return fieldWorker;
    }

    public void setFieldWorker(FieldWorker fieldWorker) {
        this.fieldWorker = fieldWorker;
    }

    public LocationHierarchy getHierarchy1() {
        return hierarchy1;
    }

    public LocationHierarchy getHierarchy2() {
        return hierarchy2;
    }
    
    public LocationHierarchy getHierarchy3() {
        return hierarchy3;
    }

    public LocationHierarchy getHierarchy4() {
        return hierarchy4;
    }
    
    public LocationHierarchy getHierarchy5() {
        return hierarchy5;
    }

    public Round getRound() {
        return round;
    }

    public Individual getSelectedIndividual() {
        return selectedIndividual;
    }

    public void setSelectedIndividual(Individual selectedIndividual) {
        this.selectedIndividual = selectedIndividual;
    }

    public void setHierarchy1(LocationHierarchy region) {
        this.hierarchy1 = region;
        clearLevelsBelow(1);
    }

    public void clearLevelsBelow(int i) {
        switch (i) {
        case 0:
            hierarchy1 = null;
        case 1:
            hierarchy2 = null;
        case 2:
            hierarchy3 = null;
        case 3:
            hierarchy4 = null;
        case 4:
            hierarchy5 = null;
        case 5:
            round = null;
        case 6:
            location = null;
        case 7:
            selectedIndividual = null;
        }
    }

    public void setHierarchy2(LocationHierarchy subRegion) {
        this.hierarchy2 = subRegion;
        clearLevelsBelow(2);
    }
    
    public void setHierarchy3(LocationHierarchy hierarchy3) {
        this.hierarchy3 = hierarchy3;
        clearLevelsBelow(3);
    }

    public void setHierarchy4(LocationHierarchy village) {
        this.hierarchy4 = village;
        clearLevelsBelow(4);
    }
    
    public void setHierarchy5(LocationHierarchy floor) {
        this.hierarchy5 = floor;
        clearLevelsBelow(5);
    }

    public void setRound(Round round) {
        this.round = round;
        clearLevelsBelow(6);
    }

    public Location getLocation() {
        return location;
    }

    public int getLevelOfHierarchySelected() {
        if (hierarchy1 == null) {
            return 0;
        }

        if (hierarchy2 == null) {
            return 1;
        }
        
        if (hierarchy3 == null) {
            return 2;
        }

        if (hierarchy4 == null) {
            return 3;
        }

        if (round == null) {
            return 4;
        }

        return 5;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void createLocation(ContentResolver resolver) {
        String locationId = generateLocationId(resolver);

        location = new Location();
        location.setExtId(locationId);
        //location.setHierarchy(hierarchy5.getExtId());
    }

    private String generateLocationId(ContentResolver resolver) {
        Cursor cursor = resolver.query(OpenHDS.Locations.CONTENT_ID_URI_BASE,
                new String[] { OpenHDS.Locations.COLUMN_LOCATION_EXTID }, OpenHDS.Locations.COLUMN_LOCATION_EXTID
                        + " LIKE ?", new String[] { hierarchy5.getExtId() + "%" }, OpenHDS.Locations.COLUMN_LOCATION_EXTID
                        + " DESC");

        String generatedId = null;
        if (cursor.moveToFirst()) {
            generatedId = generateLocationIdFrom(cursor.getString(0));
        } else {
            generatedId = hierarchy5.getExtId() + "000001";
        }

        cursor.close();
        return generatedId;
    }

    private String generateLocationIdFrom(String lastGeneratedId) {
        try {
            int increment = Integer.parseInt(lastGeneratedId.substring(3, 9));
            int nextIncrement = increment + 1;
            return String.format(hierarchy4.getExtId() + "%06d", nextIncrement);
        } catch (NumberFormatException e) {
            return hierarchy4.getExtId() + "000001";
        }
    }

    // this logic is specific for Cross River
    public void createVisit(ContentResolver resolver) {
        //String visitPrefix = location.getExtId() + round.getRoundNumber();
        
        String suffix= round.getRoundNumber();
    	if(suffix.length() < 3)
    		suffix="0"+suffix;
    	    		
        String generatedId = location.getExtId() + suffix ;

        /*Cursor cursor = resolver.query(OpenHDS.Visits.CONTENT_ID_URI_BASE,
                new String[] { OpenHDS.Visits.COLUMN_VISIT_EXTID }, OpenHDS.Visits.COLUMN_VISIT_EXTID + " LIKE ?",
                new String[] { visitPrefix + "%" }, OpenHDS.Visits.COLUMN_VISIT_EXTID + " DESC");
        String visitGeneratedId;
        if (cursor.moveToFirst()) {
            try {
                int lastVisitCount = Integer.parseInt(cursor.getString(0).substring(10, 12));
                int nextVisitCount = lastVisitCount + 1;
                visitGeneratedId = visitPrefix + nextVisitCount;
            } catch (NumberFormatException e) {
                visitGeneratedId = visitPrefix + "1";
            }
        } else {
            visitGeneratedId = visitPrefix + "1";
        }

        cursor.close();*/

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(new Date());

        visit = new Visit();
//        visit.setExtId(generatedId);
//        visit.setDate(date);
    }

    public PregnancyOutcome createPregnancyOutcome(ContentResolver resolver, int liveBirthCount) {
        PregnancyOutcome outcome = new PregnancyOutcome();
        outcome.setMother(selectedIndividual);

        if (liveBirthCount > 0) {
            String[] ids = generateIndividualIds(resolver, liveBirthCount);
            for (String id : ids) {
                outcome.addChildId(id);
            }
        }

        return outcome;
    }

    private String[] generateIndividualIds(ContentResolver resolver, int liveBirthCount) {
    	
        Cursor cursor = getCursor(resolver);
        int lastIndividualCount = 0;
        if (cursor.moveToFirst()) {
            try {
                lastIndividualCount = Integer.parseInt(cursor.getString(0).substring(9, 12));
            } catch (NumberFormatException e) {
            }
        }

        int nextIndividualCount = lastIndividualCount + 1;
        String[] ids = new String[liveBirthCount];
        for (int i = 0; i < liveBirthCount; i++) {
            ids[i] = location.getExtId() + String.format("%03d", nextIndividualCount);
            nextIndividualCount += 1;
        }

        cursor.close();

        return ids;
    }

    // an option to create a new social group rather than to reference an
    // existing one
    public SocialGroup createSocialGroup(ContentResolver resolver) {
        SocialGroup sg = new SocialGroup();

        String socialGroupPrefix = hierarchy4.getExtId() + location.getExtId().substring(3, 9);

        Cursor cursor = resolver.query(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE,
                new String[] { OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID },
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID + " LIKE ?", new String[] { socialGroupPrefix + "%" },
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID + " DESC");

        if (cursor.moveToNext()) {
 //           int lastIncrement = Integer.parseInt(cursor.getString(0).substring(9, 11));
  //          int nextIncrement = lastIncrement + 1;
    //        sg.setExtId(socialGroupPrefix + String.format("%02d", nextIncrement));
        	return null;
        } else {
            sg.setExtId(socialGroupPrefix + "00");
        }

        cursor.close();

        return sg;
    }

    public Individual determinePregnancyOutcomeFather(ContentResolver resolver) {
        Cursor cursor = Queries.getRelationshipByIndividualA(resolver, selectedIndividual.getExtId());
        List<Relationship> rels = Converter.toRelationshipList(cursor);
        // the selected individual will always be the 'individualA' in the
        // relationship
        cursor = Queries.getRelationshipByIndividualB(resolver, selectedIndividual.getExtId());
        rels.addAll(Converter.toRelationshipListSwapped(cursor));

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        Relationship currentHusband = null;
        // must find the most current relationship
        for (Relationship rel : rels) {

            if (currentHusband == null) {
                currentHusband = rel;
            } else {
                try {
                    Date currentDate = formatter.parse(currentHusband.getStartDate());
                    Date relDate = formatter.parse(rel.getStartDate());
                    if (currentDate.before(relDate))
                        currentHusband = rel;

                } catch (ParseException e) {
                    return null;
                }
            }
        }

        if (currentHusband == null) {
            return null;
        } else {
            String fatherId = currentHusband.getIndividualB();
            cursor = Queries.getIndividualByExtId(resolver, fatherId);
            return Converter.toIndividual(cursor,true);
        }
    }

    public Visit getVisit() {
        return visit;
    }

    public boolean isVisitStarted() {
        return visit != null;
    }
    
    private Cursor getCursor(ContentResolver resolver){
    	Cursor cursor = resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                new String[] { OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID },
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " LIKE ?", new String[] { location.getExtId() + "%" },
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " DESC");
    	return cursor;
    }

    public String generateIndividualId(ContentResolver resolver) {   	
    	
    	Cursor cursor=getCursor(resolver);
    	
        String id = null;
        if (cursor.moveToNext()) {
        	
            int lastIncrement = Integer.parseInt(cursor.getString(0).substring(9, 12));
            int nextIncrement = lastIncrement + 1;
            id = location.getExtId() + String.format("%03d", nextIncrement);
        } else {
            id = location.getExtId() + "001";
        }

        cursor.close();

        return id;
    }
}
