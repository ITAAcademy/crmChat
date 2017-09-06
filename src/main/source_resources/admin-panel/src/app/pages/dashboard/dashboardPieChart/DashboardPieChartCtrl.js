/**
 * @author v.lugovksy
 * created on 16.12.2015
 */
 (function () {
  'use strict';

  angular.module('BlurAdmin.pages.dashboard')
  .controller('DashboardPieChartCtrl', DashboardPieChartCtrl);

  /** @ngInject */
  function DashboardPieChartCtrl($scope, $timeout, baConfig, baUtil,$http,$q) {
    var pieColor = baUtil.hexToRGB(baConfig.colors.defaultText, 0.2);
    $scope.chartIdPrefix = "dashboardChart";

    $scope.charts = [{
      color: pieColor,
      description: 'користувачів сьогодні',
      stats: '57,820',
      icon: 'person',
      dataUrl: "statistic/user/count_active_users",
      totalCountFieldName: "totalUsers",
      currentCountFieldName: "activeUsers",
      dataPercent: 0
    }, {
      color: pieColor,
      description: 'користувачів за тиждень',
      stats: '$ 89,745',
      icon: 'person',
      dataUrl: "statistic/user/count_active_users?days=7",
      totalCountFieldName: "totalUsers",
      currentCountFieldName: "activeUsers",
      dataPercent: 0
    }, {
      color: pieColor,
      description: 'користувачв за місяць',
      stats: '178,391',
      icon: 'person',
      dataUrl: "statistic/user/count_active_users?days=30",
      totalCountFieldName: "totalUsers",
      currentCountFieldName: "activeUsers",
      dataPercent: 0
    }, {
      color: pieColor,
      description: 'повідомлень сьогодні',
      stats: '32,592',
      icon: 'comment',
      dataUrl: "statistic/count_messages_today",
      totalCountFieldName: "totalMessagesCount",
      currentCountFieldName: "activeMessagesCount",
      dataPercent: 0
    }
    ];

    function loadPieCharts() {
      var currentCharIndex = 0;
      var deffereds = [];
      for (var currentChar of $scope.charts ) {
      
       var chartModel = $scope.charts[currentCharIndex];
       var isRequestParameterAlreadyPresent = chartModel.dataUrl.includes('?');
       var unionSymbol = isRequestParameterAlreadyPresent ? '&' : '?';
       var deffered =     $http({
        method: 'GET',
        url: chartModel.dataUrl+unionSymbol+'requestId='+currentCharIndex
      });
       deffered.then(function successCallback(response) {

        var responseData = response.data;
        var responseId = Number(responseData.requestId);
        var chartModelForResponseId = $scope.charts[responseId];
         var chart =  $('#'+$scope.chartIdPrefix+responseId);
        var totalCount = responseData[chartModelForResponseId.totalCountFieldName];
        var currentCount = responseData[chartModelForResponseId.currentCountFieldName];
        chartModelForResponseId.stats = currentCount;
       var easyPie =  chart.easyPieChart({
          easing: 'easeOutBounce',
          onStep: function (from, to, percent) {
            var percent = Math.round((currentCount*100)/totalCount);
            chartModelForResponseId.dataPercent = percent;
            $(this.el).find('.percent').text(percent);
          },
          barColor: chart.attr('rel'),
          trackColor: 'rgba(0,0,0,0)',
          size: 84,
          scaleLength: 0,
          animation: 2000,
          lineWidth: 9,
          lineCap: 'round',
        });


      }); 
       deffereds.push(deffered);
       currentCharIndex++;
     }
     $('.refresh-data').on('click', function () {
      updatePieCharts();
    });
     return  deffereds;
   }

   function updatePieCharts() {
    var i = 0;
for (var currentChar of $scope.charts ) {
  var chart =  $('#'+$scope.chartIdPrefix+i);
       var chartModel = $scope.charts[i];
       chart.data('easyPieChart').update(chartModel.dataPercent);
       i++;
}

    /*$('.pie-charts .chart').each(function(index, chart) {
      $(chart).data('easyPieChart').update(50);
    });*/
  }

   

         $timeout(function () {
           $q.all(loadPieCharts()).then(function(){
        updatePieCharts();
               }, 4000);
   })

}
})();