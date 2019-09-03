const builders = require('builders'),
    navmod = require('navmod'),
    m = new navmod.Builder();

m.bind({
    form: 'net_register',
    label: 'netRegFormLabel',
    builder: builders.minimal });

m.launcher({ label: 'netRegFormLabel', bind: 'net_register' });

exports.module = m.build({
    name: 'retreg',
    title: 'caseMgmt.activityTitle',
    launchLabel: 'caseMgmt.launchTitle',
    launchDescription: 'caseMgmt.launchDescription'});