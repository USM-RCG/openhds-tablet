
function builder(fn) {
    return new FormBuilder({ build: fn });
}

function minData(d, ctx) {
    const fw = ctx.currentFieldWorker, e = d.rootElement;
    e.getChild('fieldWorkerUuid').text = fw.uuid;
    e.getChild('fieldWorkerExtId').text = fw.extId;
    e.getChild('collectionDateTime').text = DateUtils.formatTime(new Date());
}

function newId() {
    return IdHelper.generateEntityUuid();
}

function formatBuilding(building, includePrefix) {
    return (includePrefix? "E" : "") + ("" + building).padStart(3,'0');
}


function formatFloor(floor, includePrefix) {
    return (includePrefix? "P" : "") + ("" + floor).padStart(2,'0');
}

function newLocationData(d, ctx) {
    const hierPath = ctx.hierarchyPath,
        sector = ctx.currentSelection,
        map = hierPath.get('mapArea'),
        nextBuildingNumber = db.locations.nextBuildingNumberInSector(map.name, sector.name),
        e = d.rootElement;
    e.getChild('entityUuid').text = newId();
    e.getChild('locationBuildingNumber').text = formatBuilding(nextBuildingNumber, false);
    e.getChild('hierarchyExtId').text = sector.extId;
    e.getChild('hierarchyUuid').text = sector.uuid;
    e.getChild('hierarchyParentUuid').text = map.uuid;
    e.getChild('sectorName').text = sector.name;
    e.getChild('locationFloorNumber').text = formatFloor(1, false);
    e.getChild('mapAreaName').text = map.name;
}

function generateIndividualExtId(l) {
    const suffix = db.individuals.findByResidency(l.uuid).list.size() + 1;
    return l.extId + "-" + ("" + suffix).padStart(3,'0');
}

function newIndividualData(d, ctx) {
    const location = ctx.currentSelection,
        individualExtId = generateIndividualExtId(location),
        e = d.rootElement;
    e.getChild('individualExtId').text = individualExtId;
    e.getChild('householdUuid').text = location.uuid;
    e.getChild('householdExtId').text = location.extId;
    e.getChild('entityExtId').text = individualExtId;
    e.getChild('entityUuid').text = newId();
}

function generateNetCode(ctx) {
    const hierPath = ctx.hierarchyPath,
        e = hierPath.get('household'),
        sector = hierPath.get('sector'),
        map = hierPath.get('mapArea'),
        household = db.locations.findById(e.uuid).first,
        year = ('' + new Date().getFullYear()).slice(2,4);
    return year + '/' + map.name + sector.name + ('' + household.buildingNumber).padStart(3,'0');
}

function bednet(d, ctx) {
    const cs = ctx.currentSelection,
        locationExtId = cs.extId,
        locationUuid = cs.uuid,
        individuals = db.individuals.findByResidency(locationUuid).list,
        e = d.rootElement,
        distributionDateTime = e.getChildText('collectionDateTime');
    minData(d, ctx);
    e.getChild('distributionDateTime').text = distributionDateTime;
    e.getChild('locationExtId').text = locationExtId;
    e.getChild('locationUuid').text = locationUuid;
    e.getChild('entityExtId').text = locationExtId;
    e.getChild('entityUuid').text = locationUuid;
    e.getChild('netCode').text = generateNetCode(ctx);
    e.getChild('householdSize').text = individuals.size();
}

function duploc(d, ctx) {
    const path = ctx.hierarchyPath, cs = ctx.currentSelection,
        sector = path.get('sector'), map = path.get('mapArea'),
        existing = db.locations.findById(cs.uuid).first,
        nextBuildingNumber = db.locations.nextBuildingNumberInSector(map.name, sector.name),
        e = d.rootElement;
    minData(d, ctx);
    e.getChild('mapAreaName').text = map.name;
    e.getChild('sectorName').text = sector.name;
    e.getChild('locationBuildingNumber').text = formatBuilding(nextBuildingNumber, true);
    e.getChild('locationFloorNumber').text = formatFloor(1, true);
    e.getChild('entityExtId').text = cs.extId;
    e.getChild('entityUuid').text = cs.uuid;
    e.getChild('description').text = existing.description;
}

function fingerprints(d, ctx) {
    minData(d, ctx);
    d.rootElement.getChild('individualUuid').text = ctx.currentSelection.uuid;
}

function household(d, ctx) {
    const cs = ctx.currentSelection, e = d.rootElement;
    minData(d, ctx);
    e.getChild('locationExtId').text = cs.extId;
    e.getChild('locationUuid').text = cs.uuid;
}

function householdHead(d, ctx) {
    minData(d, ctx);
    newIndividualData(d, ctx);
    d.rootElement.getChild('headPrefilledFlag').text = 'true';
}

function householdMember(d, ctx) {
    const cs = ctx.currentSelection, residents = db.individuals.findByResidency(cs.uuid).list, e = d.rootElement;
    minData(d, ctx);
    newIndividualData(d, ctx);
    if (residents.size() === 1) {
        const head = residents.get(0);
        e.getChild('individualPointOfContactName').text = Individual.getFullName(head);
        e.getChild('individualPointOfContactPhoneNumber').text = head.phoneNumber;
    } else {
        for (let i=0; i<residents.size(); i++) {
            const r = residents.get(i);
            const name = r.pointOfContactName, number = r.pointOfContactPhoneNumber;
            if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(number)) {
                e.getChild('individualPointOfContactName').text = name;
                e.getChild('individualPointOfContactPhoneNumber').text = number;
                break;
            }
        }
    }
}

function location(d, ctx) {
    minData(d, ctx);
    return newLocationData(d, ctx);
}

function locationEval(d, ctx) {
    const l = db.locations.findById(ctx.currentSelection.uuid).first, e = d.rootElement;
    minData(d, ctx);
    e.getChild('entityExtId').text = l.extId;
    e.getChild('entityUuid').text = l.uuid;
    e.getChild('description').text = l.description;
}

function map(d, ctx) {
    const cs = ctx.currentSelection, e = d.rootElement;
    minData(d, ctx);
    e.getChild('localityUuid').text = cs.uuid;
    e.getChild('mapUuid').text = newId();
}

function minimal(d, ctx) {
    minData(d, ctx);
}

function mis(d, ctx) {
    const cs = ctx.currentSelection, e = d.rootElement;
    minData(d, ctx);
    e.getChild('entityExtId').text = cs.extId;
    e.getChild('entityUuid').text = cs.uuid;
    e.getChild('survey_date').text = DateUtils.formatDate(new Date());
}

function nested(d, ctx) {
    const cs = ctx.currentSelection, e = d.rootElement,
        residents = db.individuals.findByResidency(cs.uuid).list,
        template = e.getChild('individuals').clone();
    template.removeAttribute('template', FormUtils.JR_NS);
    for (let ridx = 0; ridx<residents.size(); ridx++) {
        let r = residents.get(ridx), re = template.clone();
        re.getChild('uuid').text = r.uuid;
        re.getChild('extId').text = r.extId;
        re.getChild('firstName').text = r.firstName;
        re.getChild('lastName').text = r.lastName;
        e.addContent(re);
    }
}

function respar(d, ctx) {
    const cs = ctx.currentSelection, e = d.rootElement;
    minData(d, ctx);
    e.getChild('entityExtId').text = cs.extId;
    e.getChild('entityUuid').text = cs.uuid;
}

function sbcc(d, ctx) {
    const path = ctx.hierarchyPath, h = path.get('household'), i = path.get('individual'), e = d.rootElement;
    minData(d, ctx);
    e.getChild('entityExtId').text = h.extId;
    e.getChild('entityUuid').text = h.uuid;
    if (i) {
        e.getChild('individualExtId').text = i.extId;
        e.getChild('individualUuid').text = i.uuid;
    }
}

function sector(d, ctx) {
    const cs = ctx.currentSelection, e = d.rootElement;
    minData(d, ctx);
    e.getChild('mapUuid').text = cs.uuid;
    e.getChild('sectorUuid').text = newId();
}

function superojo(d, ctx) {
    const fw = ctx.currentFieldWorker, cs = ctx.currentSelection, extId = cs.extId, uuid = cs.uuid, e = d.rootElement;
    minData(d, ctx);
    e.getChild('supervisorExtId').text = fw.extId;
    e.getChild('ojo_date').text = DateUtils.formatTime(new Date());
    e.getChild('locationExtId').text = extId;
    e.getChild('locationUuid').text = uuid;
    e.getChild('entityExtId').text = extId;
    e.getChild('entityUuid').text = uuid;
}


exports.bednet = builder(bednet);
exports.duploc = builder(duploc);
exports.fingerprints = builder(fingerprints);
exports.household = builder(household);
exports.householdHead = builder(householdHead);
exports.householdMember = builder(householdMember);
exports.location = builder(location);
exports.locationEval = builder(locationEval);
exports.map = builder(map);
exports.minimal = builder(minimal);
exports.mis = builder(mis);
exports.nested = builder(nested);
exports.respar = builder(respar);
exports.sbcc = builder(sbcc);
exports.sector = builder(sector);
exports.superojo = builder(superojo);