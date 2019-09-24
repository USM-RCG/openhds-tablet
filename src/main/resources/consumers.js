
function consumer(fn) {
    return new FormConsumer({ consume: fn });
}

function map(d) {

    const e = d.rootElement,
        hierGateway = $db.hierarchy,
        localityUuid = e.getChildText('localityUuid'),
        mapName = e.getChildText('mapName'),
        locality = hierGateway.findById(localityUuid).first,
        m = new LocationHierarchy();

    m.uuid = e.getChildText('mapUuid');
    m.extId = mapName + '/' + locality.name;
    m.name = mapName;
    m.parentUuid = localityUuid;
    m.level = 'mapArea';

    hierGateway.insertOrUpdate(m);

    return false;
}

function sector(d) {

    const e = d.rootElement,
        hierGateway = $db.hierarchy,
        mapUuid = e.getChildText('mapUuid'),
        sectorName = e.getChildText('sectorName'),
        map = hierGateway.findById(mapUuid).first,
        locality = hierGateway.findById(map.parentUuid).first,
        s = new LocationHierarchy();

    s.uuid = e.getChildText('sectorUuid');
    s.extId = map.name + sectorName + '/' + locality.name;
    s.name = sectorName;
    s.parentUuid = mapUuid;
    s.level = 'sector';

    hierGateway.insertOrUpdate(s);

    return false;
}

function formToLocation(e) {
    const l = new Location();
    l.uuid = e.getChildText('entityUuid');
    l.extId = e.getChildText('locationExtId');
    l.name = e.getChildText('locationName');
    l.hierarchyUuid = e.getChildText('hierarchyUuid');
    l.sectorName = e.getChildText('sectorName');
    l.mapAreaName = e.getChildText('mapAreaName');
    l.buildingNumber = e.getChildText('locationBuildingNumber');
    l.description = e.getChildText('description');
    l.longitude = e.getChildText('longitude');
    l.latitude = e.getChildText('latitude');
    return l;
}

function formToIndividual(e) {
    const i = new Individual();
    i.uuid = e.getChildText('entityUuid');
    i.extId = e.getChildText('individualExtId');
    i.firstName = e.getChildText('individualFirstName');
    i.lastName = e.getChildText('individualLastName');
    i.dob = e.getChildText('individualDateOfBirth');
    i.gender = e.getChildText('individualGender');
    i.currentResidenceUuid = e.getChildText('householdUuid');
    i.otherId = e.getChildText('individualDip');
    i.otherNames = e.getChildText('individualOtherNames');
    i.phoneNumber = e.getChildText('individualPhoneNumber');
    i.otherPhoneNumber = e.getChildText('individualOtherPhoneNumber');
    i.pointOfContactName = e.getChildText('individualPointOfContactName');
    i.pointOfContactPhoneNumber = e.getChildText('individualPointOfContactPhoneNumber');
    i.languagePreference = e.getChildText('individualLanguagePreference');
    i.status = e.getChildText('individualMemberStatus');
    i.nationality = e.getChildText('individualNationality');
    i.relationshipToHead = e.getChildText('individualRelationshipToHeadOfHousehold') || '1';
    return i;
}

function insertOrUpdateIndividual(e) {
    const i = formToIndividual(e);
    $db.individuals.insertOrUpdate(i);
    return i;
}

function householdMember(d) {
    insertOrUpdateIndividual(d.rootElement);
    return false;
}

function insertOrUpdateLocation(data) {
    const l = formToLocation(data);
    $db.locations.insertOrUpdate(l);
    return l;
}

function location(d) {
    const e = d.rootElement;
    insertOrUpdateLocation(e);
    e.getChild('entityExtId').text = e.getChildText('locationExtId');
    return true;
}

function householdHead(d, ctx) {

    const e = d.rootElement,
        locationGateway = $db.locations,
        individual = insertOrUpdateIndividual(e),
        selectedLocation = ctx.hierarchyPath.get('household'),
        location = locationGateway.findById(selectedLocation.uuid).first,
        locationName = individual.lastName;

    location.name = locationName;
    selectedLocation.name = locationName;
    locationGateway.insertOrUpdate(location);

    return true;
}

function nested(d) {
    const e = d.rootElement, idvs = e.getChildren('individuals');
    for (let idx = 0; idx < idvs.size(); idx++) {
        let i = idvs.get(idx), idb = $db.individuals.findById(i.getChildText('uuid')).first;
        idb.firstName = i.getChildText('firstName');
        idb.lastName = i.getChildText('lastName');
        idb.extId = i.getChildText('extId');
        $db.individuals.insertOrUpdate(idb);
    }
    return false;
}

exports.default = consumer(() => false);
exports.householdHead = consumer(householdHead);
exports.householdMember = consumer(householdMember);
exports.location = consumer(location);
exports.map = consumer(map);
exports.nested = consumer(nested);
exports.sector = consumer(sector);