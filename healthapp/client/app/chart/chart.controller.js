'use strict';

angular.module('healthappApp')
.controller('ChartCtrl', function ($scope, $interval, $http, socket) {

  $scope.measures = [];

  $scope.data = {
    series: ['BPM', 'SPO2'],
    data: []
  };

  $http.get('/api/measures').success(function(awesomeMeasures) {
    $scope.measures = awesomeMeasures;

    for(var i = 0 ; i < awesomeMeasures.length; i++) {

      var item = awesomeMeasures[i];
      var date = new Date(item.time);
      var time = ('0'+date.getHours()).slice(-2) + ':' + ('0'+date.getMinutes()).slice(-2) + ':' + ('0'+date.getSeconds()).slice(-2) + ':' + date.getMilliseconds();

      var newData = {
        x: time,
        y: [item.bpm, item.spo2]
      };

      $scope.data.data.push(newData);

      if($scope.data.data.length > 8 ) {
        $scope.data.data.shift();
      }
    }


    socket.syncUpdates('measure', $scope.measures, $scope.measureCallback);
  });

  $scope.$on('$destroy', function () {
    socket.unsyncUpdates('measure');
  });

  $scope.measureCallback = function(event, item, array){
    console.log(event);
    console.log(item);
    console.log(array);

    if(event === 'created') {

      var date = new Date(item.time);
      var time = ('0'+date.getHours()).slice(-2) + ':' + ('0'+date.getMinutes()).slice(-2) + ':' + ('0'+date.getSeconds()).slice(-2) + ':' + date.getMilliseconds();

      var newData = {
        x: time,
        y: [item.bpm, item.spo2]
      };

      $scope.data.data.push(newData);

      if($scope.data.data.length > 8 ) {
        $scope.data.data.shift();
      }
    }


  };

  $scope.config = {
    title: 'BPM & SPO2',
    tooltips: true,
    labels: false,
    mouseover: function() {},
    mouseout: function() {},
    click: function() {},
    legend: {
      display: true,
      //could be 'left, right'
      position: 'right'
    },
    isAnimate: false
  };

  $scope.addRandomMeasure = function() {
    $http.post('/api/measures', { bpm: Math.ceil(Math.random()*100), spo2: Math.ceil(Math.random()*100)  });
  };


  // $interval(function(){
  //
  // }, 3000);


});
