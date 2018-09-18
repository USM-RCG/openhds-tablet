'use strict';

['census.js',
 'gisops.js',
 'spraying.js',
 'sbcc.js',
 'netreg.js',
 'mis.js',
 'active_detection.js'
 ].forEach(function(m) {
   config.executeScript(m);
 });
