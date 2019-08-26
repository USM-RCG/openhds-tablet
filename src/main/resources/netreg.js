const ji = JavaImporter(org.cimsbioko.navconfig.forms.builders);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    form: 'net_register',
    label: 'netRegFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.Minimal()});

m.launcher({ label: 'netRegFormLabel', bind: 'net_register' });

exports.module = m.build({
    name: 'retreg',
    title: 'caseMgmt.activityTitle',
    launchLabel: 'caseMgmt.launchTitle',
    launchDescription: 'caseMgmt.launchDescription'});