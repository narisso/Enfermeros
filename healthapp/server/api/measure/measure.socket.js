/**
 * Broadcast updates to client when the model changes
 */

'use strict';

var Measure = require('./measure.model');

exports.register = function(socket) {
  Measure.schema.post('save', function (doc) {
    onSave(socket, doc);
  });
  Measure.schema.post('remove', function (doc) {
    onRemove(socket, doc);
  });
}

function onSave(socket, doc, cb) {
  socket.emit('measure:save', doc);
}

function onRemove(socket, doc, cb) {
  socket.emit('measure:remove', doc);
}