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

    bind({ form: 'human_landing_and_light_traps',
           label: 'humanLandingLabel',
           builder: new BiokoFormPayloadBuilders.DefaultHousehold() });

    bind({ form: 'irs_quality_control',
           label: 'irsQualityLabel',
           builder: new BiokoFormPayloadBuilders.DefaultHousehold() });

    bind({ form: 'susceptibility_test',
          label: 'susceptibilityTestLabel',
          builder: new BiokoFormPayloadBuilders.DefaultHousehold() });

    function launcher(l) {
        return new Launcher({
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
        household: [
            launcher({ label: 'humanLandingLabel', bind: 'human_landing_and_light_traps' }),
            launcher({ label: 'irsQualityLabel', bind: 'irs_quality_control' }),
            launcher({ label: 'susceptibilityTestLabel', bind: 'susceptibility_test' }),
        ]
    };

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getName: function() { return 'entymology'; },
        getActivityTitle: function() { return config.getString('entymology.activityTitle'); },
        getLaunchLabel: function() { return config.getString('entymology.launchTitle'); },
        getLaunchDescription: function() { return config.getString('entymology.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}

