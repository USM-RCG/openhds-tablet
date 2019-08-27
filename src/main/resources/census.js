const ji = JavaImporter(
    org.cimsbioko.navconfig.forms.filters,
    org.cimsbioko.navconfig.forms.builders,
    org.cimsbioko.navconfig.forms.consumers
);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    name: 'household',
    form: 'location',
    label: 'householdFormLabel',
    builder: new ji.CensusFormPayloadBuilders.AddLocation(),
    consumer: new ji.CensusFormPayloadConsumers.AddLocation() });

m.bind({
    form: 'location_evaluation',
    label: 'locationEvaluationFormLabel',
    builder: new ji.CensusFormPayloadBuilders.LocationEvaluation() });

m.bind({
    form: 'bed_net',
    label: 'bedNetFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.DistributeBednets() });

m.bind({
    name: 'household_head',
    form: 'individual',
    label: 'individualFormLabel',
    builder: new ji.CensusFormPayloadBuilders.AddHeadOfHousehold(),
    consumer: new ji.CensusFormPayloadConsumers.AddHeadOfHousehold() });

m.bind({
    name: 'household_member',
    form: 'individual',
    label: 'individualFormLabel',
    builder: new ji.CensusFormPayloadBuilders.AddMemberOfHousehold(),
    consumer: new ji.CensusFormPayloadConsumers.AddMemberOfHousehold() });

m.bind({
    form: 'fingerprints',
    label: 'fingerprintsFormLabel',
    builder: new ji.CensusFormPayloadBuilders.Fingerprints() });

m.launcher({
    level: 'sector',
    label: 'census.householdLabel',
    bind: 'household',
    filter: new ji.CensusFormFilters.AddLocation() });

m.launcher({ level: 'household', label: 'census.locationEvaluationLabel', bind: 'location_evaluation' });

m.launcher({
    level: 'household',
    label: 'census.headOfHouseholdLabel',
    bind: 'household_head',
    filter: new ji.CensusFormFilters.AddHeadOfHousehold() });

m.launcher({
    level: 'household',
    label: 'census.householdMemberLabel',
    bind: 'household_member',
    filter: ji.InvertedFilter.invert(new ji.CensusFormFilters.AddHeadOfHousehold()) });

m.launcher({ level: 'household', label: 'census.bednetsLabel', bind: 'bed_net' });

m.launcher({ level: 'individual', label: 'census.fingerprintsLabel', bind: 'fingerprints' });

exports.module = m.build({
    name: 'census',
    title: 'census.activityTitle',
    launchLabel: 'census.launchTitle',
    launchDescription: 'census.launchDescription'});