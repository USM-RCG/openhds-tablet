const ji = JavaImporter(org.cimsbioko.navconfig.forms.builders);
const navmod = require('navmod');
const m = new navmod.Builder();

m.bind({ form: 'ccst_sbcc', label: 'ccstFormLabel', builder: new ji.BiokoFormPayloadBuilders.Sbcc() });
m.bind({ form: 'mild_sbcc', label: 'mildFormLabel', builder: new ji.BiokoFormPayloadBuilders.Sbcc() });
m.bind({ form: 'net_education', label: 'netEducationFormLabel', builder: new ji.BiokoFormPayloadBuilders.Sbcc() });

m.launcher({ level: 'household', label: 'sbcc.ccstLabel', bind: 'ccst_sbcc' });
m.launcher({ level: 'household', label: 'sbcc.mildLabel', bind: 'mild_sbcc' });
m.launcher({ level: 'household', label: 'netEducationFormLabel', bind: 'net_education' });
m.launcher({ level: 'individual', label: 'sbcc.ccstLabel', bind: 'ccst_sbcc' });
m.launcher({ level: 'individual', label: 'sbcc.mildLabel', bind: 'mild_sbcc' });
m.launcher({ level: 'individual', label: 'netEducationFormLabel', bind: 'net_education' });

exports.module = m.build({
    name: 'sbcc',
    title: 'sbcc.activityTitle',
    launchLabel: 'sbcc.launchTitle',
    launchDescription: 'sbcc.launchDescription'});

