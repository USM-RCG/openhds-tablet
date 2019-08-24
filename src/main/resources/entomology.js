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
            getLabel() { return config.getString(l.label); },
            relevantFor(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding() { return binds[l.bind]; }
        });
    }

    const launchers = {
        household: [
            launcher({ label: 'humanLandingLabel', bind: 'human_landing_and_light_traps' }),
            launcher({ label: 'irsQualityLabel', bind: 'irs_quality_control' }),
            launcher({ label: 'susceptibilityTestLabel', bind: 'susceptibility_test' }),
        ]
    };

    const details = {
        individual: new IndividualDetailFragment()
    };

    exports.module = new NavigatorModule({
        getName() { return 'entomology'; },
        getActivityTitle() { return config.getString('entomology.activityTitle'); },
        getLaunchLabel() { return config.getString('entomology.launchTitle'); },
        getLaunchDescription() { return config.getString('entomology.launchDescription'); },
        getBindings() { return binds; },
        getLaunchers(level) { return launchers[level] || []; },
        getDetailFragment(level) { return details[level] || null; }
    });
}

