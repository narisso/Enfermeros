'use strict';

angular.module('healthappApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('chart', {
        url: '/chart',
        templateUrl: 'app/chart/chart.html',
        controller: 'ChartCtrl'
      });
  });