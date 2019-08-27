const ji = JavaImporter(org.cimsbioko.navconfig.forms.builders);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    form: 'human_landing_and_light_traps',
    label: 'humanLandingLabel',
    builder: new ji.BiokoFormPayloadBuilders.DefaultHousehold() });

m.bind({
    form: 'irs_quality_control',
    label: 'irsQualityLabel',
    builder: new ji.BiokoFormPayloadBuilders.DefaultHousehold() });

m.bind({
    form: 'susceptibility_test',
    label: 'susceptibilityTestLabel',
    builder: new ji.BiokoFormPayloadBuilders.DefaultHousehold() });

m.launcher({ level: 'household', label: 'humanLandingLabel', bind: 'human_landing_and_light_traps' });
m.launcher({ level: 'household', label: 'irsQualityLabel', bind: 'irs_quality_control' });
m.launcher({ level: 'household', label: 'susceptibilityTestLabel', bind: 'susceptibility_test' });

exports.module = m.build({
    name: 'entomology',
    title: 'entomology.activityTitle',
    launchLabel: 'entomology.launchTitle',
    launchDescription: 'entomology.launchDescription'});

