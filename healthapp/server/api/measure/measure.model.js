'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var MeasureSchema = new Schema({
  time : { type : Date, default: Date.now },
  bpm: Number,
  spo2: Number,
  active: Boolean
});

module.exports = mongoose.model('Measure', MeasureSchema);
