"use strict";

config.hierarchy = require('hierarchy').hierarchy;

const navmods = [
  'gisops',
  'advspray',
  'spraying',
  'netreg',
  'mis',
  'entomology',
  'egmvi'
];

navmods.map(name => require(name).module).forEach(module => config.addModule(module));
