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

    bind({ form: 'irs_iec_r26',
           label: 'advSprayFormLabel',
           builder: new BiokoFormPayloadBuilders.DefaultHousehold() });

    function launcher(l) {
        return new Launcher({
            getLabel() { return config.getString(l.label); },
            relevantFor(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding() { return binds[l.bind]; }
        });
    }

    const launchers = {
        household: [
            launcher({ label: 'advspray.advSprayLabel', bind: 'irs_iec_r26' }),
        ]
    };

    const details = {
        individual: new IndividualDetailFragment()
    };

    exports.module = new NavigatorModule({
        getName() { return 'advspray'; },
        getActivityTitle() { return config.getString('advspray.activityTitle'); },
        getLaunchLabel() { return config.getString('advspray.launchTitle'); },
        getLaunchDescription() { return config.getString('advspray.launchDescription'); },
        getBindings() { return binds; },
        getLaunchers(level) { return launchers[level] || []; },
        getDetailFragment(level) { return details[level] || null; }
    });
}

