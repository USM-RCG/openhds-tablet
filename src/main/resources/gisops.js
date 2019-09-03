const consumers = require('consumers'),
    builders = require('builders'),
    navmod = require('navmod'),
    m = new navmod.Builder();

m.bind({
    form: 'create_map',
    label: 'createMapFormLabel',
    builder: builders.map,
    consumer: consumers.map });

m.bind({
    form: 'create_sector',
    label: 'createSectorFormLabel',
    builder: builders.sector,
    consumer: consumers.sector });

m.bind({
    form: 'location',
    label: 'locationFormLabel',
    builder: builders.location,
    consumer: consumers.location });

m.bind({
    form: 'duplicate_location',
    label: 'duplicateLocationFormLabel',
    builder: builders.duploc });

m.launcher({ level: 'locality', label: 'gisops.createMapLabel', bind: 'create_map' });
m.launcher({ level: 'mapArea', label: 'gisops.createSectorLabel', bind: 'create_sector' });
m.launcher({ level: 'sector', label: 'gisops.locationLabel', bind: 'location' });
m.launcher({ level: 'household', label: 'gisops.duplicateLocationLabel', bind: 'duplicate_location' });

exports.module = m.build({
    name: 'gisops',
    title: 'gisops.activityTitle',
    launchLabel: 'gisops.launchTitle',
    launchDescription: 'gisops.launchDescription'});