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

    bind({ form: 'spraying',
           label: 'sprayingFormLabel',
           builder: new BiokoFormPayloadBuilders.SprayHousehold() });

    bind({ form: 'super_ojo',
           label: 'superOjoFormLabel',
           builder: new BiokoFormPayloadBuilders.SuperOjo() });

    function launcher(l) {
        return new Launcher({
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
        household: [
            launcher({ label: 'spraying.sprayingLabel', bind: 'spraying' }),
            launcher({ label: 'spraying.superOjoLabel', bind: 'super_ojo' }),
        ]
    };

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getName: function() { return 'spraying'; },
        getActivityTitle: function() { return config.getString('spraying.activityTitle'); },
        getLaunchLabel: function() { return config.getString('spraying.launchTitle'); },
        getLaunchDescription: function() { return config.getString('spraying.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}

