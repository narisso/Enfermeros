'use strict';

var _ = require('lodash');
var Measure = require('./measure.model');

// Get list of measures
exports.index = function(req, res) {
  Measure.find(function (err, measures) {
    if(err) { return handleError(res, err); }
    return res.json(200, measures);
  });
};

// Get a single measure
exports.show = function(req, res) {
  Measure.findById(req.params.id, function (err, measure) {
    if(err) { return handleError(res, err); }
    if(!measure) { return res.send(404); }
    return res.json(measure);
  });
};

// Creates a new measure in the DB.
exports.create = function(req, res) {
  Measure.create(req.body, function(err, measure) {
    if(err) { return handleError(res, err); }
    return res.json(201, measure);
  });
};

// Updates an existing measure in the DB.
exports.update = function(req, res) {
  if(req.body._id) { delete req.body._id; }
  Measure.findById(req.params.id, function (err, measure) {
    if (err) { return handleError(res, err); }
    if(!measure) { return res.send(404); }
    var updated = _.merge(measure, req.body);
    updated.save(function (err) {
      if (err) { return handleError(res, err); }
      return res.json(200, measure);
    });
  });
};

// Deletes a measure from the DB.
exports.destroy = function(req, res) {
  Measure.findById(req.params.id, function (err, measure) {
    if(err) { return handleError(res, err); }
    if(!measure) { return res.send(404); }
    measure.remove(function(err) {
      if(err) { return handleError(res, err); }
      return res.send(204);
    });
  });
};

function handleError(res, err) {
  return res.send(500, err);
}