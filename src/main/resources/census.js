const consumers = require('consumers'),
    builders = require('builders'),
    navmod = require('navmod'),
    m = new navmod.Builder();

m.bind({
    name: 'household',
    form: 'location',
    label: 'householdFormLabel',
    builder: builders.location,
    consumer: consumers.location });

m.bind({
    form: 'location_evaluation',
    label: 'locationEvaluationFormLabel',
    builder: builders.locationEval });

m.bind({
    form: 'bed_net',
    label: 'bedNetFormLabel',
    builder: builders.bednet });

m.bind({
    name: 'household_head',
    form: 'individual',
    label: 'individualFormLabel',
    builder: builders.householdHead,
    consumer: consumers.householdHead });

m.bind({
    name: 'household_member',
    form: 'individual',
    label: 'individualFormLabel',
    builder: builders.householdMember,
    consumer: consumers.householdMember });

m.bind({
    form: 'fingerprints',
    label: 'fingerprintsFormLabel',
    builder: builders.fingerprints });

m.launcher({
    level: 'sector',
    label: 'census.householdLabel',
    bind: 'household' });

m.launcher({ level: 'household', label: 'census.locationEvaluationLabel', bind: 'location_evaluation' });

function housePopulated(uuid) {
    return db.individuals.findByResidency(uuid).exists();
}

m.launcher({
    level: 'household',
    label: 'census.headOfHouseholdLabel',
    bind: 'household_head',
    relevant: ctx => !housePopulated(ctx.hierarchyPath.get('household').uuid) });

m.launcher({
    level: 'household',
    label: 'census.householdMemberLabel',
    bind: 'household_member',
    relevant: ctx => housePopulated(ctx.hierarchyPath.get('household').uuid) });

m.launcher({ level: 'household', label: 'census.bednetsLabel', bind: 'bed_net' });

m.launcher({ level: 'individual', label: 'census.fingerprintsLabel', bind: 'fingerprints' });

exports.module = m.build({
    name: 'census',
    title: 'census.activityTitle',
    launchLabel: 'census.launchTitle',
    launchDescription: 'census.launchDescription'});