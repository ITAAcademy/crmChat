/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('ActivityPageCtrl', ActivityPageCtrl);

    /** @ngInject */
    function ActivityPageCtrl($scope, $timeout, ActivityPageService, $http, $window,baConfig,$element,layoutPaths,UserMonitorService, CommonOperationsService) {

$scope.test="ROMA this is test";
$scope.usersList = [];
        $scope.convertUserObjectToStringByMatchedProperty = CommonOperationsService.convertUserObjectToStringByMatchedProperty;


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


function generateFullChartDataAndLabels(receivedData,activityCooldown){

    var resultObjects = [];
  var activityFinishedIdle = false;
for (var i = 0; i < receivedData.length; i++){
         var currentItem = receivedData[i];
  //end idle interval

  if(i>0 && activityFinishedIdle){
          var zeroActivityFinishObject = {};
       zeroActivityFinishObject.status = 0;
        zeroActivityFinishObject.when=(currentItem);
        resultObjects.push(zeroActivityFinishObject);
  }
  //start activity interval
              var presenceActivityObj = {};
        
      presenceActivityObj.when=currentItem;
      presenceActivityObj.status=1;
      resultObjects.push(presenceActivityObj);

  //end activity interval
            if(i!=receivedData.length-1){
              var nextItem = receivedData[i+1];
      if (currentItem+activityCooldown < nextItem ){
        var presenceFinishedObj = {};
        presenceFinishedObj.when = currentItem+activityCooldown;
        presenceFinishedObj.status = 1;
        resultObjects.push(presenceFinishedObj);

  //start idle interval
      var zeroActivityObject = {};
       zeroActivityObject.status = 0;
        zeroActivityObject.when=(currentItem+activityCooldown);
        resultObjects.push(zeroActivityObject);
        activityFinishedIdle = true;
        }
        else{
           activityFinishedIdle = false;
        }
            }
            //end activity interval for last point
            else {
              if (receivedData.length>0){
                var presenceFinishedObj = {};
        presenceFinishedObj.when = currentItem+activityCooldown;
        presenceFinishedObj.status = 1;
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
    var chartId = $element[0].getAttribute('id');
    var testProvider = function(){
      var datesAndValues  = [];
      var currentDateLong = (new Date()).getTime();
      var startTime = currentDateLong - 360 * 24 * 60 * 60 * 1000;
      var currentTime = startTime;
      for (var i = 0; i < 3; i++){
        var obj = {};
        obj.when = new Date(currentTime);
        obj.status =  Math.floor(Math.random() * 9) % 2; //random 1 or 0
        datesAndValues.push(obj);
        currentTime += Math.random()*30000+1;
      }
      return datesAndValues;
    }
    var chartConfig = {
      type: 'serial',
      theme: 'blur',
      color: layoutColors.defaultText,
      dataProvider: testProvider(),
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
          title: "Статус активності"
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
       minPeriod: "mm",
        parseDates: true,
        gridAlpha: 0.15,
        gridColor: layoutColors.border,
        minorGridEnabled: true,
        title: "Дата"
      },
      export: {
        enabled: true
      },
      pathToImages: layoutPaths.images.amChart
    };

    var barChartConfig = {
      type: 'serial',
      theme: 'blur',
      color: layoutColors.defaultText,
       "valueAxes": [ {
      "gridColor": "#FFFFFF",
      "gridAlpha": 0.2,
      "dashLength": 0,
      "title": "Робочі хвилини"
    } ],
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
    "gridAboveGraphs": true,
    "startDuration": 1,
    "graphs": [ {
      "balloonText": "[[category]]: <b>[[value]]</b>",
      "fillAlphas": 0.8,
      "lineAlpha": 0.2,
      "type": "column",
      "valueField": "status"
    } ],
    "chartCursor": {
      "categoryBalloonEnabled": false,
      "cursorAlpha": 0,
      "zoomable": true,
      fullWidth: true,
      categoryBalloonDateFormat: 'YYYY MMM DD',
    },
    "categoryField": "when",
    "categoryAxis": {
      "minPeriod": "DD",
      "gridPosition": "start",
      "gridAlpha": 0,
      "tickPosition": "start",
      "tickLength": 20,
      "parseDates": true,
      "title": "Дні"
    },
    "export": {
      "enabled": true
    },
      pathToImages: layoutPaths.images.amChart
    };



    /*areaChart.addListener('dataUpdated', zoomAreaChart);

function zoomAreaChart() {
       areaChart.zoomToIndexes($scope.userActivityDataValue.length/2 - $scope.userActivityDataValue.length /4, $scope.userActivityDataValue.length/2 + $scope.userActivityDataValue.length /4);
    }*/
$scope.updateUserActivity = updateUserActivity;
$scope.updateUserActivityPerDay = updateUserActivityPerDay;

function generateRequestPayload(){
   var requestPayload = {
            chatUserId: $scope.selected.user.chatUserId,
            beforeDate: $scope.dates.start.getTime(),
            afterDate: $scope.dates.end.getTime()
          }
          return requestPayload;
}

        function updateUserActivity(){
          var requestPayload = generateRequestPayload();
    $http.post(serverPrefix + "/statistic/user/get_activity",requestPayload).then(function(payload, status, headers, config) {
        var data = payload.data;
        var receivedData =  data.activityAtTime;
        var processedData = generateFullChartDataAndLabels(receivedData,data.activityDurationMs,layoutColors.info);
        convertChartDataLongToDate(processedData);
        $scope.areaChart.dataProvider = processedData;
        $scope.userActivityDataValue = processedData;
        $scope.userActivityPerDayDataValue = [];
        $scope.areaChart.validateData();
            });
}

function updateUserActivityPerDay(){
     var requestPayload = generateRequestPayload();
    $http.post(serverPrefix + "/statistic/user/get_activity_per_day",requestPayload).then(function(payload, status, headers, config) {
        var payloadContent = payload.data;
        var activityMap =  payloadContent.data;
        var processedData = convertMapToActivityObjects(activityMap);
        $scope.barChart.dataProvider = processedData;
        $scope.userActivityPerDayDataValue = processedData;
           $scope.userActivityDataValue = [];
        $scope.barChart.validateData();
            });
}
function convertMapToActivityObjects(mapObj){
  var result = [];

for (var key in mapObj) {
  if (mapObj.hasOwnProperty(key)) {
    var msOfActivity = mapObj[key];
    var minutesOfActivity = msOfActivity / 1000 / 60;
    result.push({
      when:new Date(parseInt(key)),
      status: minutesOfActivity
    })
  }
result.sort(function(a,b) {
return a.when.getTime() - b.when.getTime();
});


}
return result;
}


 //TEST
      var chartId = 'areaChart';
      var barChartId = 'barChart';
        $scope.areaChart = AmCharts.makeChart(chartId, chartConfig);
        $scope.barChart = AmCharts.makeChart(barChartId, barChartConfig);
    





    }

})();
