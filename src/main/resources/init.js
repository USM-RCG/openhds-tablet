'use strict';

['census.js',
 'gisops.js',
 'spraying.js',
 'sbcc.js'].forEach(function(m) {
   config.executeScript(m);
 });
