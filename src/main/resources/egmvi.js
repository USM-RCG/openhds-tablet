const builders = require('builders'),
    navmod = require('navmod'),
    m = new navmod.Builder();

m.bind({
    form: 'eg_respar',
    label: 'resparLabel',
    builder: builders.respar });

m.launcher({ level: 'household', label: 'resparLabel', bind: 'eg_respar' });

exports.module = m.build({
    name: 'egmvi',
    title: 'egmvi.activityTitle',
    launchLabel: 'egmvi.launchTitle',
    launchDescription: 'egmvi.launchDescription' });