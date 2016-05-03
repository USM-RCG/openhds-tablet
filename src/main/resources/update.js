var imports = JavaImporter(
    org.openhds.mobile.R,
    org.openhds.mobile.navconfig,
    org.openhds.mobile.navconfig.forms,
    org.openhds.mobile.navconfig.forms.filters,
    org.openhds.mobile.navconfig.forms.builders,
    org.openhds.mobile.navconfig.forms.consumers,
    org.openhds.mobile.fragment.navigate.detail,
    org.openhds.mobile.repository.search.SearchUtils
);

with (imports) {

    var labels = {
        visit: 'visitFormLabel',
        in_migration: 'inMigrationFormLabel',
        individual: 'individualFormLabel',
        out_migration: 'outMigrationFormLabel',
        death: 'deathFormLabel',
        pregnancy_observation: 'pregnancyObservationFormLabel',
        pregnancy_outcome: 'pregnancyOutcomeFormLabel'
    };

    var migrantSearch = SearchUtils.getIndividualModule(
        ProjectFormFields.Individuals.INDIVIDUAL_UUID, R.string.search_individual_label);

    var externalInMigrationFormBehavior = new FormBehavior('in_migration', 'update.externalInMigrationLabel',
        new UpdateFormFilters.RegisterInMigration(),
        new UpdateFormPayloadBuilders.RegisterExternalInMigration(),
        new UpdateFormPayloadConsumers.RegisterInMigration());

    var paternitySearch = SearchUtils.getIndividualModule(
        ProjectFormFields.PregnancyOutcome.FATHER_UUID, R.string.search_father_label);

    var forms = {
        individual: [
            new FormBehavior('visit', 'shared.visitLabel',
                new UpdateFormFilters.StartAVisit(),
                new UpdateFormPayloadBuilders.StartAVisit(),
                new UpdateFormPayloadConsumers.StartAVisit()),
            new FormBehavior('in_migration', 'update.internalInMigrationLabel',
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.RegisterInternalInMigration(),
                new UpdateFormPayloadConsumers.RegisterInMigration(), migrantSearch),
            new FormBehavior('individual', 'update.externalInMigrationLabel',
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.AddIndividualFromInMigration(),
                new UpdateFormPayloadConsumers.AddIndividualFromInMigration(externalInMigrationFormBehavior))
        ],
        bottom: [
            new FormBehavior('out_migration', 'update.outMigrationLabel',
                new UpdateFormFilters.DeathOrOutMigrationFilter(),
                new UpdateFormPayloadBuilders.RegisterOutMigration(),
                new UpdateFormPayloadConsumers.RegisterOutMigration()),
            new FormBehavior('death', 'update.deathLabel',
                new UpdateFormFilters.DeathOrOutMigrationFilter(),
                new UpdateFormPayloadBuilders.RegisterDeath(),
                new UpdateFormPayloadConsumers.RegisterDeath()),
            new FormBehavior('pregnancy_observation', 'shared.pregnancyObservationLabel',
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                null),
            new FormBehavior('pregnancy_outcome', 'update.pregnancyOutcomeLabel',
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyOutcome(),
                null, paternitySearch)
        ]
    };

    var details = {
        bottom: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getLaunchLabel: function() { return config.getString('update.launchTitle'); },
        getLaunchDescription: function() { return config.getString('update.launchDescription'); },
        getActivityTitle: function() { return config.getString('update.activityTitle'); },
        getForms: function(level) { return forms[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; },
        getFormLabels: function() { return labels; }
    });

    config.addModule(module);
}
