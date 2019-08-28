const ji = JavaImporter(org.cimsbioko.navconfig);

const levels = [
    { name: 'region', label: 'hier.region' },
    { name: 'province', label: 'hier.province' },
    { name: 'district', label: 'hier.district' },
    { name: 'subDistrict', label: 'hier.subDistrict' },
    { name: 'locality', label: 'hier.locality' },
    { name: 'mapArea', label: 'hier.mapArea' },
    { name: 'sector', label: 'hier.sector' },
    { name: 'household', label: 'hier.household', builtIn: true },
    { name: 'individual', label: 'hier.individual', builtIn: true }
];

const labelMap = levels.reduce((map, lvl) => { map[lvl.name] = lvl.label; return map }, {});
const adminLevelNames = levels.filter(lvl => !lvl.builtIn).map(lvl => lvl.name);
const levelNames = levels.map(lvl => lvl.name);
const parentNameMap = levelNames.reduce((map, ln, idx, arr) => { map[ln] = arr[idx-1] || null; return map }, {});

exports.hierarchy = new ji.Hierarchy({
    getLevelLabels() { return labelMap; },
    getAdminLevels() { return adminLevelNames; },
    getLevels() { return levelNames; },
    getParentLevel(child) { return parentNameMap[child]; }
});