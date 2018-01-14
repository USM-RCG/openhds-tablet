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
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
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

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getName: function() { return 'census'; },
        getActivityTitle: function() { return config.getString('census.activityTitle'); },
        getLaunchLabel: function() { return config.getString('census.launchTitle'); },
        getLaunchDescription: function() { return config.getString('census.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}