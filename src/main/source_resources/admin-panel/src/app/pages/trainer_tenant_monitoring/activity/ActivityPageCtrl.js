/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('ActivityPageCtrl', ActivityPageCtrl);

function generateFullChartDataAndLabels(receivedData,activityCooldown,lineColor){
    var resultObjects = [];
for (var i = 0; i < receivedData.length; i++){
         var currentItem = receivedData[i];
  if(i>0){
          var zeroActivityFinishObject = {};
       zeroActivityFinishObject.status = 0;
        zeroActivityFinishObject.when=(currentItem-1);
         zeroActivityFinishObject.lineColor = lineColor;
        resultObjects.push(zeroActivityFinishObject);
        console.log('({0},{1}) added to chart data'.format(zeroActivityFinishObject.when,zeroActivityFinishObject.status));
  }
              var presenceActivityObj = {};
        
      presenceActivityObj.when=currentItem;
      presenceActivityObj.status=1;
      presenceActivityObj.lineColor = lineColor;
      resultObjects.push(presenceActivityObj);
      console.log('({0},{1}) added to chart data'.format(presenceActivityObj.when,presenceActivityObj.status));


            if(i!=receivedData.length-1){
              var nextItem = receivedData[i+1];
      if (currentItem+activityCooldown < nextItem ){
        var presenceFinishedObj = {};
        presenceFinishedObj.when = currentItem+activityCooldown-1;
        presenceFinishedObj.status = 1;
        presenceFinishedObj.lineColor = lineColor;
        resultObjects.push(presenceFinishedObj);
        console.log('({0},{1}) added to chart data'.format(presenceFinishedObj.when,presenceFinishedObj.status));


      var zeroActivityObject = {};
       zeroActivityObject.status = 0;
        zeroActivityObject.when=(currentItem+activityCooldown);
         zeroActivityObject.lineColor = lineColor;
        resultObjects.push(zeroActivityObject);
        console.log('({0},{1}) added to chart data'.format(zeroActivityObject.when,zeroActivityObject.status));

        }

        
            }

            else {
              if (receivedData.length>0){
                var presenceFinishedObj = {};
        presenceFinishedObj.when = currentItem+activityCooldown-1;
        presenceFinishedObj.status = 1;
        presenceFinishedObj.lineColor = lineColor;
        resultObjects.push(presenceFinishedObj);
        console.log('({0},{1}) added to chart data'.format(presenceFinishedObj.when,presenceFinishedObj.status));
              }
            }

           
        }
        return resultObjects;
}

function convertChartDataLongToDate(fullChartData){
for (var i = 0; i < fullChartData.length; i++ ){
    var date = new Date(fullChartData[i].when);
    var yymmdd = date.toISOString();
  fullChartData[i].when = date;
}
}



    /** @ngInject */
    function ActivityPageCtrl($scope, $timeout, ActivityPageService, $http, $window,baConfig,$element,layoutPaths) {

        var layoutColors = baConfig.colors;
    var id = $element[0].getAttribute('id');
 var areaChart = AmCharts.makeChart(id, {
      type: 'serial',
      theme: 'blur',
      color: layoutColors.defaultText,
      balloon: {
        cornerRadius: 6,
        horizontalPadding: 15,
        verticalPadding: 10
      },
      valueAxes: [
        {
            max: 1,
            min: 0,
          gridAlpha: 0.5,
          gridColor: layoutColors.border,
        }
      ],
      graphs: [
        {
          id: 'gl',
          bullet: 'square',
          bulletBorderAlpha: 1,
          bulletBorderThickness: 1,
          fillAlphas: 0.5,
          fillColorsField: 'lineColor',
          legendValueText: '[[value]]',
          lineColorField: 'lineColor',
          title: 'Був активний',
          valueField: 'status'
        }
      ],
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
       minPeriod: "ss",
        parseDates: true,
      //  parseDates: true,
        gridAlpha: 0.5,
        gridColor: layoutColors.border,
        minorGridEnabled: true
      },
      export: {
        enabled: true
      },
      pathToImages: layoutPaths.images.amChart
    });
    areaChart.addListener('dataUpdated', zoomAreaChart);

function zoomAreaChart() {
       areaChart.zoomToIndexes($scope.userActivityDataValue.length/2 - $scope.userActivityDataValue.length /4, $scope.userActivityDataValue.length/2 + $scope.userActivityDataValue.length /4);
    }



        function updateCurrentUserActivity(){
    $http.get(serverPrefix + "/statistic/user/get_week_activity_current_user?days={0}".format(350)).success(function(data, status, headers, config) {
                  var receivedData =  data.activityAtTime;
        var processedData = generateFullChartDataAndLabels(receivedData,data.activityDurationMs,layoutColors.info);
        convertChartDataLongToDate(processedData);
        areaChart.dataProvider = processedData;
        areaChart.validateData();
        $scope.userActivityDataValue = processedData;
            }).error(function(data, status, headers, config) {});
}

      
updateCurrentUserActivity();


    }

})();
