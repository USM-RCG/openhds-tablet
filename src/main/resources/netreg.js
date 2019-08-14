var imports = JavaImporter(
    org.cimsbioko.navconfig,
    org.cimsbioko.navconfig.forms,
    org.cimsbioko.navconfig.forms.filters,
    org.cimsbioko.navconfig.forms.builders,
    org.cimsbioko.navconfig.forms.consumers,
    org.cimsbioko.fragment.navigate.detail
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

    bind({ form: 'net_register',
           label: 'netRegFormLabel',
           builder: new BiokoFormPayloadBuilders.Minimal() });

    function launcher(l) {
        return new Launcher({
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
        root: [
            launcher({ label: 'netRegFormLabel', bind: 'net_register' }),
        ]
    };

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getName: function() { return 'retreg'; },
        getActivityTitle: function() { return config.getString('caseMgmt.activityTitle'); },
        getLaunchLabel: function() { return config.getString('caseMgmt.launchTitle'); },
        getLaunchDescription: function() { return config.getString('caseMgmt.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}