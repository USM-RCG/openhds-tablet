const ji = JavaImporter(org.cimsbioko.navconfig.forms.builders);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({
    form: 'malaria_indicator_survey',
    label: 'mis.formLabel',
    builder: new ji.BiokoFormPayloadBuilders.MalariaIndicatorSurvey() });

m.launcher({ level: 'household', label: 'mis.formLabel', bind: 'malaria_indicator_survey' });

exports.module = m.build({
    name: 'mis',
    activityTitle: 'mis.activityTitle',
    launchLabel: 'mis.launchTitle',
    launchDescription: 'mis.launchDescription'});
