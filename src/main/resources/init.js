"use strict";

const modules = [
  'gisops',
  'advspray',
  'spraying',
  'netreg',
  'mis',
  'entomology'
];

modules.map(name => require(name).module).forEach(module => config.addModule(module));
