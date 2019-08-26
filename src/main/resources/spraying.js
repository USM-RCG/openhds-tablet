const ji = JavaImporter(org.cimsbioko.navconfig.forms.builders);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    form: 'irs_sp1_r26',
    label: 'sprayingFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.DefaultHousehold() });

m.bind({
    form: 'super_ojo',
    label: 'superOjoFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.SuperOjo() });

m.launcher({ level: 'household', label: 'spraying.sprayingLabel', bind: 'irs_sp1_r26' });
m.launcher({ level: 'household', label: 'spraying.superOjoLabel', bind: 'super_ojo' });

exports.module = m.build({
    name: 'spraying',
    title: 'spraying.activityTitle',
    launchLabel: 'spraying.launchTitle',
    launchDescription: 'spraying.launchDescription'});
