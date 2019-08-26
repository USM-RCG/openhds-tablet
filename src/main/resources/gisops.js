const ji = JavaImporter(
    org.cimsbioko.navconfig.forms.filters,
    org.cimsbioko.navconfig.forms.builders,
    org.cimsbioko.navconfig.forms.consumers
);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    form: 'create_map',
    label: 'createMapFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.CreateMap(),
    consumer: new ji.BiokoFormPayloadConsumers.CreateMap()});

m.bind({
    form: 'create_sector',
    label: 'createSectorFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.CreateSector(),
    consumer: new ji.BiokoFormPayloadConsumers.CreateSector()});

m.bind({
    form: 'location',
    label: 'locationFormLabel',
    builder: new ji.CensusFormPayloadBuilders.AddLocation(),
    consumer: new ji.CensusFormPayloadConsumers.AddLocation()});

m.bind({
    form: 'duplicate_location',
    label: 'duplicateLocationFormLabel',
    builder: new ji.BiokoFormPayloadBuilders.DuplicateLocation()});

m.launcher({ level: 'locality', label: 'gisops.createMapLabel', bind: 'create_map' });

m.launcher({ level: 'mapArea', label: 'gisops.createSectorLabel', bind: 'create_sector' });

m.launcher({
    level: 'sector',
    label: 'gisops.locationLabel',
    bind: 'location',
    filter: new ji.CensusFormFilters.AddLocation()});

m.launcher({ level: 'household', label: 'gisops.duplicateLocationLabel', bind: 'duplicate_location' });

exports.module = m.build({
    name: 'gisops',
    title: 'gisops.activityTitle',
    launchLabel: 'gisops.launchTitle',
    launchDescription: 'gisops.launchDescription'});