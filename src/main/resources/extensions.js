var imports = JavaImporter(
    org.openhds.mobile.model.form,
    org.openhds.mobile.navconfig,
    org.openhds.mobile.navconfig.forms.filters,
    org.openhds.mobile.navconfig.forms.builders,
    org.openhds.mobile.navconfig.forms.consumers,
    org.openhds.mobile.fragment.navigate.detail
);

with (imports) {

    var labels = {
        bed_net: 'bedNetFormLabel',
        spraying: 'sprayingFormLabel',
        super_ojo: 'superOjoFormLabel',
        duplicate_location: 'duplicateLocationFormLabel'
    };

    var forms = {
        individual: [
            new FormBehavior('bed_net', 'bioko.bednetsLabel',
                new BiokoFormFilters.DistributeBednets(),
                new BiokoFormPayloadBuilders.DistributeBednets(),
                new BiokoFormPayloadConsumers.DistributeBednets()),
            new FormBehavior('spraying', 'bioko.sprayingLabel',
                new BiokoFormFilters.SprayHousehold(),
                new BiokoFormPayloadBuilders.SprayHousehold(),
                new BiokoFormPayloadConsumers.SprayHousehold()),
            new FormBehavior('super_ojo', 'bioko.superOjoLabel',
                null,
                new BiokoFormPayloadBuilders.SuperOjo(),
                null),
            new FormBehavior('duplicate_location', 'bioko.duplicateLocationLabel',
                null,
                new BiokoFormPayloadBuilders.DuplicateLocation(),
                null)
        ]
    };

    var details = {
        bottom: new IndividualDetailFragment()
    };

    var module = new NavigatorModule({
        getLaunchLabel: function() { return config.getString('bioko.launchTitle'); },
        getLaunchDescription: function() { return config.getString('bioko.launchDescription'); },
        getActivityTitle: function() { return config.getString('bioko.activityTitle'); },
        getForms: function(level) { return forms[level] || []; },
        getDetailFragment: function(level) { return details[level] || null; },
        getFormLabels: function() { return labels; }
    });

    config.addModule(module);
}