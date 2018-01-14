var imports = JavaImporter(
    org.openhds.mobile.navconfig,
    org.openhds.mobile.navconfig.forms,
    org.openhds.mobile.navconfig.forms.filters,
    org.openhds.mobile.navconfig.forms.builders,
    org.openhds.mobile.navconfig.forms.consumers,
    org.openhds.mobile.fragment.navigate.detail
);

with (imports) {

    var binds = {};

    function bind(b) {
        var bind_name = b.name || b.form;
        binds[bind_name] = new Binding({
            getName: function() { return bind_name; },
            getForm: function() { return b.form; },
            getLabel: function() { return config.getString(b.label); },
            getBuilder: function() { return b.builder; },
            getConsumer: function() { return b.consumer || new DefaultConsumer(); },
        });
    }

    bind({ form: 'create_map',
           label: 'createMapFormLabel',
           builder: new BiokoFormPayloadBuilders.CreateMap() });

    bind({ form: 'create_sector',
           label: 'createSectorFormLabel',
           builder: new BiokoFormPayloadBuilders.CreateSector() });

    bind({ form: 'location',
           label: 'locationFormLabel',
           builder: new CensusFormPayloadBuilders.AddLocation(),
           consumer: new CensusFormPayloadConsumers.AddLocation() });

    bind({ form: 'duplicate_location',
           label: 'duplicateLocationFormLabel',
           builder: new BiokoFormPayloadBuilders.DuplicateLocation() });

    function launcher(l) {
        return new Launcher({
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
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

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getName: function() { return 'gisops'; },
        getActivityTitle: function() { return config.getString('gisops.activityTitle'); },
        getLaunchLabel: function() { return config.getString('gisops.launchTitle'); },
        getLaunchDescription: function() { return config.getString('gisops.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}

