const imports = JavaImporter(
    org.cimsbioko.navconfig,
    org.cimsbioko.navconfig.forms,
    org.cimsbioko.navconfig.forms.filters,
    org.cimsbioko.navconfig.forms.builders,
    org.cimsbioko.navconfig.forms.consumers,
    org.cimsbioko.fragment.navigate.detail
);

with (imports) {

    const binds = {};

    function bind(b) {
        const bind_name = b.name || b.form;
        binds[bind_name] = new Binding({
            getName() { return bind_name; },
            getForm() { return b.form; },
            getLabel() { return config.getString(b.label); },
            getBuilder() { return b.builder; },
            getConsumer() { return b.consumer || new DefaultConsumer(); },
        });
    }

    bind({ form: 'create_map',
           label: 'createMapFormLabel',
           builder: new BiokoFormPayloadBuilders.CreateMap(),
           consumer: new BiokoFormPayloadConsumers.CreateMap() });

    bind({ form: 'create_sector',
           label: 'createSectorFormLabel',
           builder: new BiokoFormPayloadBuilders.CreateSector(),
           consumer: new BiokoFormPayloadConsumers.CreateSector() });

    bind({ form: 'location',
           label: 'locationFormLabel',
           builder: new CensusFormPayloadBuilders.AddLocation(),
           consumer: new CensusFormPayloadConsumers.AddLocation() });

    bind({ form: 'duplicate_location',
           label: 'duplicateLocationFormLabel',
           builder: new BiokoFormPayloadBuilders.DuplicateLocation() });

    function launcher(l) {
        return new Launcher({
            getLabel() { return config.getString(l.label); },
            relevantFor(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding() { return binds[l.bind]; }
        });
    }

    const launchers = {
        locality: [
            launcher({ label: 'gisops.createMapLabel', bind: 'create_map' })
        ],
        mapArea: [
            launcher({ label: 'gisops.createSectorLabel', bind: 'create_sector' })
        ],
        sector: [
            launcher({ label: 'gisops.locationLabel', bind: 'location',
                       filter: new CensusFormFilters.AddLocation() })
        ],
        household: [
            launcher({ label: 'gisops.duplicateLocationLabel', bind: 'duplicate_location' })
        ]
    };

    const details = {
        individual: new IndividualDetailFragment()
    };

    exports.module = new NavigatorModule({
        getName() { return 'gisops'; },
        getActivityTitle() { return config.getString('gisops.activityTitle'); },
        getLaunchLabel() { return config.getString('gisops.launchTitle'); },
        getLaunchDescription() { return config.getString('gisops.launchDescription'); },
        getBindings() { return binds; },
        getLaunchers(level) { return launchers[level] || []; },
        getDetailFragment(level) { return details[level] || null; }
    });
}