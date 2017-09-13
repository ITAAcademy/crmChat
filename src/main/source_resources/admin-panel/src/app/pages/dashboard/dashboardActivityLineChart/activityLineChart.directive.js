/**
 * @author Roman Zinchuk
 * created on 2017
 */
(function () {
  'use strict';

  angular.module('BlurAdmin.pages.dashboard')
      .directive('dashboardActivityLinechart', dashboardActivityLinechart);

  /** @ngInject */
  function dashboardActivityLinechart() {
    return {
      restrict: 'E',
      controller: 'DashboardActivityLineChartCtrl',
      templateUrl: 'app/pages/dashboard/dashboardActivityLineChart/activityLineChart.html'
    };
  }
})();