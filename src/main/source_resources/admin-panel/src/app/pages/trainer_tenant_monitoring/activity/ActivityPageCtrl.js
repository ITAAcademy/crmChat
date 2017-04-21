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
            if(i!=receivedData.length-1){
                var presenceActivityObj = {};
                let nextItem = receivedData[i+1];
                let currentItem = receivedData[i];
        presenceActivityObj.when=currentItem;
        presenceActivityObj.status=1;
        presenceActivityObj.lineColor = lineColor;
        resultObjects.push(presenceActivityObj);
                if (currentItem+activityCooldown < nextItem ){
                    var zeroActivityObject = {};
       zeroActivityObject.status = 0;
        zeroActivityObject.when=(currentItem+activityCooldown+1);
         zeroActivityObject.lineColor = lineColor;
        resultObjects.push(zeroActivityObject);
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
      dataProvider: [
        {
          lineColor: layoutColors.info,
          when: new Date(),
          status: 1
        },
         {
          lineColor: layoutColors.info,
          when: new Date(),
          status: 0
        },
         {
          lineColor: layoutColors.info,
          when: new Date(),
          status: 1
        }
      ],
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

      chartCursor: {
        categoryBalloonDateFormat: 'YYYY MMM DD',
        cursorAlpha: 0,
        fullWidth: true
      },
      dataDateFormat: 'YYYY-MM-DD',
      categoryField: 'when',
      categoryAxis: {
        dateFormats: [
          {
            period: 'DD',
            format: 'DD'
          },
          {
            period: 'WW',
            format: 'MMM DD'
          },
          {
            period: 'MM',
            format: 'MMM'
          },
          {
            period: 'YYYY',
            format: 'YYYY'
          }
        ],
        parseDates: true,
        gridAlpha: 0.5,
        gridColor: layoutColors.border,
      },
      export: {
        enabled: true
      },
      pathToImages: layoutPaths.images.amChart
    });
    /*areaChart.addListener('dataUpdated', zoomAreaChart);

function zoomAreaChart() {
      areaChart.zoomToDates(new Date(2012, 0, 3), new Date(2012, 0, 11));
    }*/



        function updateCurrentUserActivity(){
    $http.get(serverPrefix + "/statistic/user/get_week_activity_current_user?days={0}".format(360)).success(function(data, status, headers, config) {
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
