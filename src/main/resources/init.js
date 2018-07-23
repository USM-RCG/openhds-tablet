'use strict';

['census.js',
 'gisops.js',
 'spraying.js',
 'sbcc.js',
 'mis.js'].forEach(function(m) {
   config.executeScript(m);
 });
