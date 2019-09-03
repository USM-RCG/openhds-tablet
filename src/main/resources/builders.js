const ji = JavaImporter(
    org.cimsbioko.utilities,
    org.cimsbioko.model.core,
    org.cimsbioko.navconfig.forms
);

function newMap() {
    return new java.util.HashMap();
}

function newBuilder(fn) {
    return new ji.FormPayloadBuilder({ buildPayload: fn });
}

function minData(ctx) {
    const d = newMap();
    ji.PayloadTools.addMinimalFormPayload(d, ctx);
    return d;
}

function newId() {
    return ji.IdHelper.generateEntityUuid();
}

function newLocationData(d, ctx) {
    const hierPath = ctx.hierarchyPath,
        sector = ctx.currentSelection,
        map = hierPath.get('mapArea'),
        nextBuildingNumber = db.locations.nextBuildingNumberInSector(map.name, sector.name);
    d.put('entityUuid', newId());
    d.put('locationBuildingNumber', ji.PayloadTools.formatBuilding(nextBuildingNumber, false));
    d.put('hierarchyExtId', sector.extId);
    d.put('hierarchyUuid', sector.uuid);
    d.put('hierarchyParentUuid', map.uuid);
    d.put('sectorName', sector.name);
    d.put('locationFloorNumber', ji.PayloadTools.formatFloor(1, false));
    d.put('mapAreaName', map.name);
    return d;
}

function newIndividualData(d, ctx) {
    const location = ctx.currentSelection, individualExtId = ji.IdHelper.generateIndividualExtId(location);
    d.put('individualExtId', individualExtId);
    d.put('householdUuid', location.uuid);
    d.put('householdExtId', location.extId);
    d.put('entityExtId', individualExtId);
    d.put('entityUuid', newId());
    return d;
}

function generateNetCode(ctx) {
    const hierPath = ctx.hierarchyPath,
        e = hierPath.get('household'),
        sector = hierPath.get('sector'),
        map = hierPath.get('mapArea'),
        household = db.locations.findById(e.uuid).first,
        year = new java.text.SimpleDateFormat('yy').format(new java.util.Date());
    return java.lang.String.format('%s/%s%sE%03.0f', year, map.name, sector.name, household.buildingNumber);
}

function bednet(ctx) {
    const d = minData(ctx),
        distributionDateTime = d.get('collectionDateTime'),
        e = ctx.currentSelection,
        locationExtId = e.extId,
        locationUuid = e.uuid,
        individuals = db.individuals.findByResidency(locationUuid).list;
    d.put('distributionDateTime', distributionDateTime);
    d.put('locationExtId', locationExtId);
    d.put('locationUuid', locationUuid);
    d.put('entityExtId', locationExtId);
    d.put('entityUuid', locationUuid);
    d.put('netCode', generateNetCode(ctx));
    d.put('householdSize', java.lang.Integer.toString(individuals.size()));
    return d;
}

function duploc(ctx) {
    const d = minData(ctx),
        path = ctx.hierarchyPath,
        e = ctx.currentSelection,
        sector = path.get('sector'),
        map = path.get('mapArea'),
        locationExtId = e.extId,
        locationUuid = e.uuid,
        existing = db.locations.findById(locationUuid).first,
        nextBuildingNumber = db.locations.nextBuildingNumberInSector(map.name, sector.name);
    d.put('mapAreaName', map.name);
    d.put('sectorName', sector.name);
    d.put('locationBuildingNumber', ji.PayloadTools.formatBuilding(nextBuildingNumber, true));
    d.put('locationFloorNumber', ji.PayloadTools.formatFloor(1, true));
    d.put('locationExtId', locationExtId);
    d.put('locationUuid', locationUuid);
    d.put('entityExtId', locationExtId);
    d.put('entityUuid', locationUuid);
    d.put('description', existing.description);
    return d;
}

function fingerprints(ctx) {
    const d = minData(ctx);
    d.put('individualUuid', ctx.currentSelection.uuid);
    return d;
}

function householdHead(ctx) {
    const d = newIndividualData(minData(ctx), ctx);
    d.put('headPrefilledFlag', 'true');
    return d;
}

function householdMember(ctx) {
    const d = newIndividualData(minData(ctx), ctx), e = ctx.currentSelection,
        residents = db.individuals.findByResidency(e.uuid).list;
    if (residents.size() === 1) {
        const head = residents.get(0);
        d.put('individualPointOfContactName', ji.Individual.getFullName(head));
        d.put('individualPointOfContactPhoneNumber', head.phoneNumber);
    } else {
        for (let i=0; i<residents.size(); i++) {
            const r = residents.get(i);
            const name = r.pointOfContactName, number = r.pointOfContactPhoneNumber;
            if (!ji.StringUtils.isEmpty(name) && !ji.StringUtils.isEmpty(number)) {
                d.put('individualPointOfContactName', name);
                d.put('individualPointOfContactPhoneNumber', number);
                break;
            }
        }
    }
    return d;
}

function household(ctx) {
    const d = minData(ctx), h = ctx.currentSelection;
    d.put('locationExtId', h.extId);
    d.put('locationUuid', h.uuid);
    return d;
}

function location(ctx) {
    return newLocationData(minData(ctx), ctx);
}

function locationEval(ctx) {
    const d = minData(ctx), e = db.locations.findById(ctx.currentSelection.uuid).first;
    d.put('entityExtId', e.extId);
    d.put('entityUuid', e.uuid);
    d.put('description', e.description);
    return d;
}

function map(ctx) {
    const d = minData(ctx), e = ctx.currentSelection;
    d.put('localityUuid', e.uuid);
    d.put('mapUuid', newId());
    return d;
}

function minimal(ctx) {
    return minData(ctx);
}

function mis(ctx) {
    const d = minData(ctx), e = ctx.currentSelection;
    d.put('entityExtId', e.extId);
    d.put('entityUuid', e.uuid);
    d.put('survey_date', ji.PayloadTools.formatDate(java.util.Calendar.getInstance()));
    return d;
}

function respar(ctx) {
    const d = minData(ctx), e = ctx.currentSelection;
    d.put('entityExtId', e.extId);
    d.put('entityUuid', e.uuid);
    return d;
}

function sbcc(ctx) {
    const d = minData(ctx),
        path = ctx.hierarchyPath,
        h = path.get('household'),
        i = path.get('individual');
    d.put('entityExtId', h.extId);
    d.put('entityUuid', h.uuid);
    if (i) {
        d.put('individualExtId', i.extId);
        d.put('individualUuid', i.uuid);
    }
    return d;
}

function sector(ctx) {
    const d = minData(ctx), e = ctx.currentSelection;
    d.put('mapUuid', e.uuid);
    d.put('sectorUuid', newId());
    return d;
}

function superojo(ctx) {
    const d = minData(ctx), fw = ctx.currentFieldWorker, entity = ctx.currentSelection,
        locationExtId = entity.extId, locationUuid = entity.uuid;
    d.put('supervisorExtId', fw.extId);
    d.put('ojo_date', ji.PayloadTools.formatTime(java.util.Calendar.getInstance()));
    d.put('locationExtId', locationExtId);
    d.put('locationUuid', locationUuid);
    d.put('entityExtId', locationExtId);
    d.put('entityUuid', locationUuid);
    return d;
}


exports.bednet = newBuilder(bednet);
exports.duploc = newBuilder(duploc);
exports.fingerprints = newBuilder(fingerprints);
exports.household = newBuilder(household);
exports.householdHead = newBuilder(householdHead);
exports.householdMember = newBuilder(householdMember);
exports.location = newBuilder(location);
exports.locationEval = newBuilder(locationEval);
exports.map = newBuilder(map);
exports.minimal = newBuilder(minimal);
exports.mis = newBuilder(mis);
exports.respar = newBuilder(respar);
exports.sbcc = newBuilder(sbcc);
exports.sector = newBuilder(sector);
exports.superojo = newBuilder(superojo);