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

    bind({ name: 'household',
           form: 'location',
           label: 'householdFormLabel',
           builder: new CensusFormPayloadBuilders.AddLocation(),
           consumer: new CensusFormPayloadConsumers.AddLocation() });

    bind({ form: 'location_evaluation',
           label: 'locationEvaluationFormLabel',
           builder: new CensusFormPayloadBuilders.LocationEvaluation() });

    bind({ form: 'bed_net',
           label: 'bedNetFormLabel',
           builder: new BiokoFormPayloadBuilders.DistributeBednets() });

    bind({ name: 'household_head',
           form: 'individual',
           label: 'individualFormLabel',
           builder: new CensusFormPayloadBuilders.AddHeadOfHousehold(),
           consumer: new CensusFormPayloadConsumers.AddHeadOfHousehold() });

    bind({ name: 'household_member',
           form: 'individual',
           label: 'individualFormLabel',
           builder: new CensusFormPayloadBuilders.AddMemberOfHousehold(),
           consumer: new CensusFormPayloadConsumers.AddMemberOfHousehold() });

    bind({ form: 'fingerprints',
           label: 'fingerprintsFormLabel',
           builder: new CensusFormPayloadBuilders.Fingerprints() });

    function launcher(l) {
        return new Launcher({
            getLabel() { return config.getString(l.label); },
            relevantFor(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding() { return binds[l.bind]; }
        });
    }

    const launchers = {
        sector: [
            launcher({ label: 'census.householdLabel', bind: 'household',
                       filter: new CensusFormFilters.AddLocation() })
        ],
        household: [
            launcher({ label: 'census.locationEvaluationLabel',
                       bind: 'location_evaluation' }),
            launcher({ label: 'census.headOfHouseholdLabel',
                       bind: 'household_head',
                       filter: new CensusFormFilters.AddHeadOfHousehold() }),
            launcher({ label: 'census.householdMemberLabel',
                       bind: 'household_member',
                       filter: InvertedFilter.invert(new CensusFormFilters.AddHeadOfHousehold()) }),
            launcher({ label: 'census.bednetsLabel', bind: 'bed_net' })
        ],
        individual: [
            launcher({ label: 'census.fingerprintsLabel',
                       bind: 'fingerprints' })
        ]
    };

    const details = {
        individual: new IndividualDetailFragment()
    };

    exports.module = new NavigatorModule({
        getName() { return 'census'; },
        getActivityTitle() { return config.getString('census.activityTitle'); },
        getLaunchLabel() { return config.getString('census.launchTitle'); },
        getLaunchDescription() { return config.getString('census.launchDescription'); },
        getBindings() { return binds; },
        getLaunchers(level) { return launchers[level] || []; },
        getDetailFragment(level) { return details[level] || null; }
    });
}