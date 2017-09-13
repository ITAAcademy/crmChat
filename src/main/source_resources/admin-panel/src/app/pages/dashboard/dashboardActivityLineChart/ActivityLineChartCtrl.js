/**
 * @author v.lugovksy
 * created on 16.12.2015
 */
(function() {
  'use strict';

  angular.module('BlurAdmin.pages.dashboard')
    .controller('DashboardActivityLineChartCtrl', DashboardActivityLineChartCtrl);

  /** @ngInject */
  function DashboardActivityLineChartCtrl($scope,$http, baConfig, layoutPaths) {
    var layoutColors = baConfig.colors;

    var dataProviderTest = function() {
      var datesAndValues = [];
      var currentDateLong = (new Date()).getTime();
      var startTime = currentDateLong - 360 * 24 * 60 * 60 * 1000;
      var currentTime = startTime;
      for (var i = 0; i < 30; i++) {
        var obj = {};
        obj.when = new Date(currentTime);
        obj.status = Math.floor(Math.random() * 9) % 2; //random 1 or 0
        datesAndValues.push(obj);
        currentTime += Math.random() * 30000 + 1;
      }
      return datesAndValues;
    }

    var chartConfig = {
      type: 'serial',
      theme: 'blur',
      color: layoutColors.defaultText,
      //dataProvider: dataProvider(),
      balloon: {
        cornerRadius: 6,
        horizontalPadding: 15,
        verticalPadding: 10
      },
      valueAxes: [{
        max: 1,
        min: 0,
        gridAlpha: 0.5,
        gridColor: 'black',
        title: "Кількість користувачів"
      }],
      graphs: [{
        id: 'gl',
        bullet: 'square',
        bulletBorderAlpha: 1,
        bulletBorderThickness: 1,
        fillAlphas: 0.5,
        fillColorsField: 'lineColor',
        legendValueText: '[[value]]',
        lineColorField: 'lineColor',
        title: 'Був активний',
        valueField: 'status',
        type: "smoothedLine"
      }],
      chartScrollbar: {
        "graph": "g1",
        "scrollbarHeight": 80,
        "backgroundAlpha": 0,
        "selectedBackgroundAlpha": 0.1,
        "selectedBackgroundColor": "#888888",
        "graphFillAlpha": 0,
        "graphLineAlpha": 0.5,
        "selectedGraphFillAlpha": 0,
        "selectedGraphLineAlpha": 1,
        "autoGridCount": true,
        "color": "#AAAAAA"
      },

      chartCursor: {
        categoryBalloonDateFormat: 'YYYY MMM DD',
        cursorAlpha: 0,
        fullWidth: true
      },
      categoryField: 'when',
      categoryAxis: {
        minPeriod: "mm",
        parseDates: true,
        gridAlpha: 0.15,
        gridColor: 'black',
        minorGridEnabled: true,
        title: "Дата"
      },
      export: {
        enabled: true
      },
      pathToImages: layoutPaths.images.amChart
    };

    function convertMapToActivityObjects(mapObj){
  var result = [];

for (var key in mapObj) {
  if (mapObj.hasOwnProperty(key)) {
    var day = parseInt(key);
    var users = mapObj[key];
    result.push({
      when:new Date(day),
      status: users
    })
  }
result.sort(function(a,b) {
return a.when.getTime() - b.when.getTime();
});
}
return result;
}


    function updateActivity(date1,
      date2) {
      var parameters = {
        'earlyDate': date1.getTime(),
        'lateDate': date2.getTime()
      }
      var requestParamsPart = encodeQueryData(parameters);

      $http.get(serverPrefix + "/statistic/user/get_visits?" + requestParamsPart, {}).then(function(payload, status, headers, config) {

        var payloadMap= payload.data;
        $scope.areaChart.dataProvider = convertMapToActivityObjects(payloadMap);
        $scope.areaChart.validateData();
      });
    }

    function initChart(){

    var chartId = 'areaChart';
    $scope.areaChart = AmCharts.makeChart(chartId, chartConfig);
    var currentDate = new Date();
    var msInDay = 1000 * 60 * 60 * 24;
    var oneMonthAgo = new Date(currentDate.getTime() - msInDay*30);
    updateActivity(oneMonthAgo,currentDate);
    }

    initChart();


  }
})();