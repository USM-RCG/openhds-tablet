'use strict';

['census.js',
 'gisops.js',
 'spraying.js',
 'bednetfollowup.js'].forEach(function(m) {
   config.executeScript(m);
 });
