const navmod = require('navmod'),
    builders = require('builders'),
    m = new navmod.Builder();

m.bind({ form: 'irs_iec_r26', label: 'advSprayFormLabel', builder: builders.household });
m.launcher({ level: 'household', label: 'advspray.advSprayLabel', bind: 'irs_iec_r26' });

exports.module = m.build({
    name: 'advspray',
    title: 'advspray.activityTitle',
    launchLabel: 'advspray.launchTitle',
    launchDescription: 'advspray.launchDescription'});

