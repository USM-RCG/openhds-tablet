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
            getConsumer: function() { return b.consumer; },
            getSearches: function() { return b.searches || []; },
            requiresSearch: function() { return b.searches? b.searches.length > 0 : false; }
        });
    }

    bind({ form: 'location',
           label: 'locationFormLabel',
           builder: new CensusFormPayloadBuilders.AddLocation(),
           consumer: new CensusFormPayloadConsumers.AddLocation() });

    bind({ form: 'location_evaluation',
           label: 'locationEvaluationFormLabel',
           builder: new CensusFormPayloadBuilders.EvaluateLocation(),
           consumer: new CensusFormPayloadConsumers.EvaluateLocation() });

    bind({ name: 'census_preg_obs',
           form: 'pregnancy_observation',
           label: 'pregnancyObservationFormLabel',
           builder: new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
           consumer: new CensusFormPayloadConsumers.ChainedPregnancyObservation() });

    bind({ name: 'census_preg_visit',
           form: 'visit',
           label: 'visitFormLabel',
           builder: new UpdateFormPayloadBuilders.StartAVisit(),
           consumer: new CensusFormPayloadConsumers.ChainedVisitForPregnancyObservation(binds['census_preg_obs']) });

    bind({ name: 'household_head',
           form: 'individual',
           label: 'individualFormLabel',
           builder: new CensusFormPayloadBuilders.AddHeadOfHousehold(),
           consumer: new CensusFormPayloadConsumers.AddHeadOfHousehold(binds['census_preg_visit']) });

    bind({ name: 'household_member',
           form: 'individual',
           label: 'individualFormLabel',
           builder: new CensusFormPayloadBuilders.AddMemberOfHousehold(),
           consumer: new CensusFormPayloadConsumers.AddMemberOfHousehold(binds['census_preg_visit']) });

    function launcher(l) {
        return new Launcher({
            getLabel: function() { return config.getString(l.label); },
            relevantFor: function(ctx) { return l.filter? l.filter.shouldDisplay(ctx) : true; },
            getBinding: function() { return binds[l.bind]; }
        });
    }

    var launchers = {
        sector: [
            launcher({ label: 'census.locationLabel',
                       bind: 'location',
                       filter: new CensusFormFilters.AddLocation() })
        ],
        household: [
            launcher({ label: 'census.evaluateLocationLabel',
                       bind: 'location_evaluation',
                       filter: new CensusFormFilters.EvaluateLocation() }),
            launcher({ label: 'census.headOfHouseholdLabel',
                       bind: 'household_head',
                       filter: new CensusFormFilters.AddHeadOfHousehold() }),
            launcher({ label: 'census.householdMemberLabel',
                       bind: 'household_member',
                       filter: InvertedFilter.invert(new CensusFormFilters.AddHeadOfHousehold()) })
        ]
    };

    var details = {
        individual: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getActivityTitle: function() { return config.getString('census.activityTitle'); },
        getLaunchLabel: function() { return config.getString('census.launchTitle'); },
        getLaunchDescription: function() { return config.getString('census.launchDescription'); },
        getBindings: function() { return binds; },
        getLaunchers: function(level) { return launchers[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; }
    });

    config.addModule(module);
}