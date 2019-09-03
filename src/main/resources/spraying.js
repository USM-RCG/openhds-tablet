const builders = require('builders'),
    navmod = require('navmod'),
    m = new navmod.Builder();

m.bind({
    form: 'irs_sp1_r26',
    label: 'sprayingFormLabel',
    builder: builders.household });

m.bind({
    form: 'super_ojo',
    label: 'superOjoFormLabel',
    builder: builders.superojo });

m.launcher({ level: 'household', label: 'spraying.sprayingLabel', bind: 'irs_sp1_r26' });
m.launcher({ level: 'household', label: 'spraying.superOjoLabel', bind: 'super_ojo' });

exports.module = m.build({
    name: 'spraying',
    title: 'spraying.activityTitle',
    launchLabel: 'spraying.launchTitle',
    launchDescription: 'spraying.launchDescription'});
