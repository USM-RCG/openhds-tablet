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

    bind({ form: 'ccst_sbcc',
           label: 'ccstFormLabel',
           builder: new BiokoFormPayloadBuilders.Sbcc() });

    bind({ form: 'mild_sbcc',
           label: 'mildFormLabel',
           builder: new BiokoFormPayloadBuilders.Sbcc() });

    bind({ form: 'net_education',
           label: 'netEducationFormLabel',
           builder: new BiokoFormPayloadBuilders.Sbcc() });

    function launcher(l) {
        return new Launcher({
            getLabel() { return config.getString(l.label); },
            relevantFor(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding() { return binds[l.bind]; }
        });
    }

    const launchers = {
        household: [
            launcher({ label: 'sbcc.ccstLabel', bind: 'ccst_sbcc' }),
            launcher({ label: 'sbcc.mildLabel', bind: 'mild_sbcc' }),
            launcher({ label: 'netEducationFormLabel', bind: 'net_education' }),
        ],
        individual: [
            launcher({ label: 'sbcc.ccstLabel', bind: 'ccst_sbcc' }),
            launcher({ label: 'sbcc.mildLabel', bind: 'mild_sbcc' }),
            launcher({ label: 'netEducationFormLabel', bind: 'net_education' }),
        ]
    };

    const details = {
        individual: new IndividualDetailFragment()
    };

    exports.module = new NavigatorModule({
        getName() { return 'sbcc'; },
        getActivityTitle() { return config.getString('sbcc.activityTitle'); },
        getLaunchLabel() { return config.getString('sbcc.launchTitle'); },
        getLaunchDescription() { return config.getString('sbcc.launchDescription'); },
        getBindings() { return binds; },
        getLaunchers(level) { return launchers[level] || []; },
        getDetailFragment(level) { return details[level] || null; }
    });
}

