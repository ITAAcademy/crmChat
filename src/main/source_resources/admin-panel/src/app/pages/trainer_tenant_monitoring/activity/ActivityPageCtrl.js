/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('ActivityPageCtrl', ActivityPageCtrl);

function generateFullChartDataAndLabels(receivedData,activityCooldown){
var fullData = [];
var fullLabel = [];
for (var i = 0; i < receivedData.length; i++){
            if(i!=receivedData.length-1){
                let nextItem = receivedData[i+1];
                let currentItem = receivedData[i];
        fullData.push(currentItem);
        fullLabel.push(1); 
                if (currentItem+activityCooldown < nextItem ){
        fullLabel.push(0);
        fullData.push(currentItem+activityCooldown+1);
        }
        
            }
           
        }
        return {
            'fullData': fullData,
            'fullLabel': fullLabel
        }
}
    /** @ngInject */
    function ActivityPageCtrl($scope, $timeout, ActivityPageService, $http, $window) {

                $scope.userActivityChartOptions = {
            scales: {
                yAxes: [{
                    id: 'y-axis-1',
                    type: 'linear',
                    display: true,
                    position: 'left',
                    ticks: { min: 0, max: 10 }
                }]
            },
            elements: {
                arc: {
                    borderWidth: 0
                }
            },
            tooltips: {
                enabled: true
            }
        };
        var receivedData =  [1000,2000,3000,6000,10000,15000,30000];
        var processedData = generateFullChartDataAndLabels(receivedData,5000);
        $scope.userActivityDataValue = processedData.fullData;
        $scope.labels=processedData.fullLabel;






    }

})();
