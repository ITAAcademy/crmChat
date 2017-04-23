/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('ActivityPageCtrl', ActivityPageCtrl);

    /** @ngInject */
    function ActivityPageCtrl($scope, $timeout, ActivityPageService, $http, $window,baConfig,$element,layoutPaths,UserMonitorService, CommonOperationsService) {

$scope.test="ROMA this is test";
$scope.usersList = [];

   $scope.dates = { start: new Date(), end: new Date() }
        $scope.dates.start.setMinutes(0); $scope.dates.start.setHours(0);
        $scope.dates.end.setMinutes(59); $scope.dates.end.setHours(23);
   // var searchBefore = '';
    var fetchUsers = function(info) {
            return CommonOperationsService.fetchUsers(info).then(function(payload){
             $scope.usersList = payload.data;
            })
        };
      $scope.fetchUsers = fetchUsers;
      $scope.selected = {user:null};


     function openStart() {
            $scope.opened.start = true;
        }

        function openEnd() {
            $scope.opened.end = true;
        }

         $scope.openStart = openStart;
          $scope.openEnd = openEnd;
          $scope.opened = { start: false, end: false };


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
  }
              var presenceActivityObj = {};
        
      presenceActivityObj.when=currentItem;
      presenceActivityObj.status=1;
      presenceActivityObj.lineColor = lineColor;
      resultObjects.push(presenceActivityObj);

            if(i!=receivedData.length-1){
              var nextItem = receivedData[i+1];
      if (currentItem+activityCooldown < nextItem ){
        var presenceFinishedObj = {};
        presenceFinishedObj.when = currentItem+activityCooldown-1;
        presenceFinishedObj.status = 1;
        presenceFinishedObj.lineColor = lineColor;
        resultObjects.push(presenceFinishedObj);

      var zeroActivityObject = {};
       zeroActivityObject.status = 0;
        zeroActivityObject.when=(currentItem+activityCooldown);
         zeroActivityObject.lineColor = lineColor;
        resultObjects.push(zeroActivityObject);

        }
            }
            else {
              if (receivedData.length>0){
                var presenceFinishedObj = {};
        presenceFinishedObj.when = currentItem+activityCooldown-1;
        presenceFinishedObj.status = 1;
        presenceFinishedObj.lineColor = lineColor;
        resultObjects.push(presenceFinishedObj);
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


        var layoutColors = baConfig.colors;
    var id = 'areaChart';//$element[0].getAttribute('id');
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
        gridAlpha: 0.15,
        gridColor: layoutColors.border,
        minorGridEnabled: true
      },
      export: {
        enabled: true
      },
      pathToImages: layoutPaths.images.amChart
    });
    /*areaChart.addListener('dataUpdated', zoomAreaChart);

function zoomAreaChart() {
       areaChart.zoomToIndexes($scope.userActivityDataValue.length/2 - $scope.userActivityDataValue.length /4, $scope.userActivityDataValue.length/2 + $scope.userActivityDataValue.length /4);
    }*/
$scope.updateUserActivity = updateUserActivity;

        function updateUserActivity(){
          var requestPayload = {
            chatUserId: $scope.selected.user.chatUserId,
            beforeDate: $scope.dates.start.getTime(),
            afterDate: $scope.dates.end.getTime()
          }
    $http.post(serverPrefix + "/statistic/user/get_activity",requestPayload).success(function(data, status, headers, config) {
        var receivedData =  data.activityAtTime;
        var processedData = generateFullChartDataAndLabels(receivedData,data.activityDurationMs,layoutColors.info);
        convertChartDataLongToDate(processedData);
        areaChart.dataProvider = processedData;
        areaChart.validateData();
        $scope.userActivityDataValue = processedData;
            });
}

 angular.element(document).ready(function () {
        console.log('Hello World');
    });

    





    }

})();
