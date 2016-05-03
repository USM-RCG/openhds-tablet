var imports = JavaImporter(
    org.openhds.mobile.navconfig,
    org.openhds.mobile.navconfig.forms,
    org.openhds.mobile.navconfig.forms.filters,
    org.openhds.mobile.navconfig.forms.builders,
    org.openhds.mobile.navconfig.forms.consumers,
    org.openhds.mobile.fragment.navigate.detail
);

with (imports) {

    var labels = {
        location: 'locationFormLabel',
        pregnancy_observation: 'pregnancyObservationFormLabel',
        visit: 'visitFormLabel',
        location_evaluation: 'locationEvaluationFormLabel',
        individual: 'individualFormLabel'
    };

    var pregObFormBehavior = new FormBehavior('pregnancy_observation', 'shared.pregnancyObservationLabel',
        new UpdateFormFilters.PregnancyFilter(),
        new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
        new CensusFormPayloadConsumers.ChainedPregnancyObservation());

    var visitPregObFormBehavior = new FormBehavior('visit', 'shared.visitLabel',
        new UpdateFormFilters.StartAVisit(),
        new UpdateFormPayloadBuilders.StartAVisit(),
        new CensusFormPayloadConsumers.ChainedVisitForPregnancyObservation(pregObFormBehavior));

    var forms = {
        household: [
            new FormBehavior('location', 'census.locationLabel',
                new CensusFormFilters.AddLocation(),
                new CensusFormPayloadBuilders.AddLocation(),
                new CensusFormPayloadConsumers.AddLocation())
        ],
        individual: [
            new FormBehavior('location_evaluation', 'census.evaluateLocationLabel',
                new CensusFormFilters.EvaluateLocation(),
                new CensusFormPayloadBuilders.EvaluateLocation(),
                new CensusFormPayloadConsumers.EvaluateLocation()),
            new FormBehavior('individual', 'census.headOfHouseholdLabel',
                new CensusFormFilters.AddHeadOfHousehold(),
                new CensusFormPayloadBuilders.AddHeadOfHousehold(),
                new CensusFormPayloadConsumers.AddHeadOfHousehold(visitPregObFormBehavior)),
            new FormBehavior('individual', 'census.householdMemberLabel',
                InvertedFilter.invert(new CensusFormFilters.AddHeadOfHousehold()),
                new CensusFormPayloadBuilders.AddMemberOfHousehold(),
                new CensusFormPayloadConsumers.AddMemberOfHousehold(visitPregObFormBehavior))
        ]
    };

    var details = {
        bottom: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getLaunchLabel: function() { return config.getString('census.launchTitle'); },
        getLaunchDescription: function() { return config.getString('census.launchDescription'); },
        getActivityTitle: function() { return config.getString('census.activityTitle'); },
        getForms: function(level) { return forms[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; },
        getFormLabels: function() { return labels; }
    });

    config.addModule(module);
}