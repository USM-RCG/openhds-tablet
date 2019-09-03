const ji = JavaImporter(
    java.lang,
    org.cimsbioko.utilities,
    org.cimsbioko.model.core,
    org.cimsbioko.navconfig.forms
);

function newConsumer(consume, augment) {
    return new ji.FormPayloadConsumer({
        consumeFormPayload: consume,
        augmentInstancePayload: augment || (() => false)
    });
}

function map(data) {

    const hierGateway = db.hierarchy,
        localityUuid = data.get('localityUuid'),
        mapName = data.get('mapName'),
        locality = hierGateway.findById(localityUuid).first,
        m = new ji.LocationHierarchy();

    m.uuid = data.get('mapUuid');
    m.extId = mapName + '/' + locality.name;
    m.name = mapName;
    m.parentUuid = localityUuid;
    m.level = 'mapArea';

    hierGateway.insertOrUpdate(m);

    return new ji.ConsumerResult(false);
}

function sector(data) {

    const hierGateway = db.hierarchy,
        mapUuid = data.get('mapUuid'),
        sectorName = data.get('sectorName'),
        map = hierGateway.findById(mapUuid).first,
        locality = hierGateway.findById(map.parentUuid).first,
        s = new ji.LocationHierarchy();

    s.uuid = data.get('sectorUuid');
    s.extId = map.name + sectorName + '/' + locality.name;
    s.name = sectorName;
    s.parentUuid = mapUuid;
    s.level = 'sector';

    hierGateway.insertOrUpdate(s);

    return new ji.ConsumerResult(false);
}

function formToLocation(data) {
    const l = new ji.Location();
    l.uuid = data.get('entityUuid');
    l.extId = data.get('locationExtId');
    l.name = data.get('locationName');
    l.hierarchyUuid = data.get('hierarchyUuid');
    l.sectorName = data.get('sectorName');
    l.mapAreaName = data.get('mapAreaName');
    l.buildingNumber = ji.Integer.parseInt(data.get('locationBuildingNumber'));
    l.description = data.get('description');
    l.longitude = data.get('longitude');
    l.latitude = data.get('latitude');
    return l;
}

function formToIndividual(data) {
    const i = new ji.Individual();
    i.uuid = data.get('entityUuid');
    i.extId = data.get('individualExtId');
    i.firstName = data.get('individualFirstName');
    i.lastName = data.get('individualLastName');
    i.dob = data.get('individualDateOfBirth');
    i.gender = data.get('individualGender');
    i.currentResidenceUuid = data.get('householdUuid');
    i.otherId = data.get('individualDip');
    i.otherNames = data.get('individualOtherNames');
    i.phoneNumber = data.get('individualPhoneNumber');
    i.otherPhoneNumber = data.get('individualOtherPhoneNumber');
    i.pointOfContactName = data.get('individualPointOfContactName');
    i.pointOfContactPhoneNumber = data.get('individualPointOfContactPhoneNumber');
    i.languagePreference = data.get('individualLanguagePreference');
    i.status = data.get('individualMemberStatus');
    i.nationality = data.get('individualNationality');
    i.relationshipToHead = data.get('individualRelationshipToHeadOfHousehold');
    return i;
}

function insertOrUpdateIndividual(data) {
    const i = formToIndividual(data);
    db.individuals.insertOrUpdate(i);
    return i;
}

function householdMember(data) {
    insertOrUpdateIndividual(data);
    return ji.ConsumerResult(false);
}

function insertOrUpdateLocation(data) {
    const l = formToLocation(data);
    db.locations.insertOrUpdate(l);
    return l;
}

function location(data) {
    insertOrUpdateLocation(data);
    return new ji.ConsumerResult(true);
}

function householdHead(data, ctx) {

    const locationGateway = db.locations,
        individual = insertOrUpdateIndividual(data),
        selectedLocation = ctx.hierarchyPath.get('household'),
        location = locationGateway.findById(selectedLocation.uuid).first,
        locationName = individual.lastName;

    location.name = locationName;
    selectedLocation.name = locationName;
    locationGateway.insertOrUpdate(location);

    return new ji.ConsumerResult(true);
}

exports.default = newConsumer(() => new ji.ConsumerResult(false));
exports.householdHead = newConsumer(householdHead, d => d.put('individualRelationshipToHeadOfHousehold', '1'));
exports.householdMember = newConsumer(householdMember);
exports.location = newConsumer(location, d => d.put('entityExtId', d.get('locationExtId')));
exports.map = newConsumer(map);
exports.sector = newConsumer(sector);