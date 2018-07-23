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

    bind({ form: 'ccst_sbcc',
           label: 'ccstFormLabel',
           builder: new BiokoFormPayloadBuilders.Sbcc() });

    bind({ form: 'mild_sbcc',
           label: 'mildFormLabel',
           builder: new BiokoFormPayloadBuilders.Sbcc() });

    function launcher(l) {
        return new Launcher({
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
        household: [
            launcher({ label: 'sbcc.ccstLabel', bind: 'ccst_sbcc' }),
            launcher({ label: 'sbcc.mildLabel', bind: 'mild_sbcc' }),
        ],
        individual: [
            launcher({ label: 'sbcc.ccstLabel', bind: 'ccst_sbcc' }),
            launcher({ label: 'sbcc.mildLabel', bind: 'mild_sbcc' }),
        ]
    };

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getName: function() { return 'sbcc'; },
        getActivityTitle: function() { return config.getString('sbcc.activityTitle'); },
        getLaunchLabel: function() { return config.getString('sbcc.launchTitle'); },
        getLaunchDescription: function() { return config.getString('sbcc.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}

