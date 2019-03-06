'use strict';

['census.js',
 'gisops.js',
 'advspray.js',
 'spraying.js',
 'sbcc.js',
 'netreg.js',
 'mis.js'].forEach(function(m) {
   config.executeScript(m);
 });
