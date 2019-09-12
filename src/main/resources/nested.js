const builders = require('builders'),
    consumers = require('consumers'),
    navmod = require('navmod'),
    m = new navmod.Builder();

m.bind({
    form: 'nested_pop',
    label: 'nestedLabel',
    builder: builders.nested,
    consumer: consumers.nested });

m.launcher({ level: 'household', label: 'nestedLabel', bind: 'nested_pop' });

exports.module = m.build({
    name: 'nested',
    title: 'nested.activityTitle',
    launchLabel: 'nested.launchTitle',
    launchDescription: 'nested.launchDescription'});
