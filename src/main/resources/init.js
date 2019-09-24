"use strict";

if (!String.prototype.padStart) {
  String.prototype.padStart = function padStart(targetLen, padStr) {
    targetLen = targetLen >> 0; // truncate if number, or convert non-number to 0;
    padStr = String(typeof padStr !== 'undefined' ? padStr : ' ');
    if (this.length >= targetLen) {
      return String(this);
    } else {
      targetLen = targetLen - this.length;
      if (targetLen > padStr.length) {
        padStr += padStr.repeat(targetLen / padStr.length); // append to original to ensure we are longer than needed
      }
      return padStr.slice(0, targetLen) + String(this);
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
