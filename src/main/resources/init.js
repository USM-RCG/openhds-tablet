"use strict";

if (!String.prototype.padStart) {
  String.prototype.padStart = function padStart(len, pad) {
    len = len >> 0; // truncate if number, non-number to 0
    pad = String(typeof pad !== 'undefined' ? pad : ' ');
    if (this.length >= len) {
      return String(this);
    } else {
      len = len - this.length;
      if (len > pad.length) {
        pad += pad.repeat(len / pad.length);
      }
      return pad.slice(0, len) + String(this);
    }
  };
}

exports.hierarchy = require('hierarchy').hierarchy;

const navmods = [
  'census',
  'gisops',
  'advspray',
  'spraying',
  'netreg',
  'mis',
  'entomology',
  'egmvi',
  'nested'
];

exports.navmods = navmods.map(name => require(name).module);
