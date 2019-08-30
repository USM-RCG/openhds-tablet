const ji = JavaImporter(org.cimsbioko.navconfig.forms.builders);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    form: 'eg_respar',
    label: 'resparLabel',
    builder: new ji.BiokoFormPayloadBuilders.Respar()});

m.launcher({ level: 'household', label: 'resparLabel', bind: 'eg_respar' });

exports.module = m.build({
    name: 'egmvi',
    title: 'egmvi.activityTitle',
    launchLabel: 'egmvi.launchTitle',
    launchDescription: 'egmvi.launchDescription' });