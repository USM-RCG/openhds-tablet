var _s = org.openhds.mobile.R.string;

var levels = [
    { name: 'region', label: _s.region_label},
    { name: 'province', label: _s.province_label },
    { name: 'district', label: _s.district_label },
    { name: 'subDistrict', label: _s.sub_district_label },
    { name: 'locality', label: _s.locality_label },
    { name: 'mapArea', label: _s.map_area_label },
    { name: 'sector', label: _s.sector_label },
    { name: 'household', label: _s.household_label },
    { name: 'individual', label: _s.individual_label },
    { name: 'bottom', label: _s.bottom_label }
];

var states = [], levelLabels = {};
for (var l = 0; l < levels.length; l++) {
    var level = levels[l];
    states.push(level.name);
    levelLabels[level.name] = level.label;
}

var info = new org.openhds.mobile.projectdata.HierarchyInfo({
    getHierarchyName: function() { return 'biokoHierarchy'; },
    getStateSequence: function() { return states; },
    getStateLabels: function() { return levelLabels },
});