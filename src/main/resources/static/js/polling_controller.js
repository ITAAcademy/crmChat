//var longPollApp = angular.module('springChat.controllers', ['toaster','ngRoute','ngResource']);
springChat.controller('PollingController', ['$scope', '$http', '$interval','$timeout',
	function ($scope, $http, $interval, $timeout) {
		function poll(){
			console.log("poll()");
            $timeout(function (){
			 $http.get("longpoll_topics")
    .then(function(response) {
        $scope.topics = response.data;
        console.log("resposnse data received:"+response.data);
        poll();
    },function errorHandler(response) {
    	console.log("error during http request");
    	$scope.topics = ["error"];
         console.log("resposnse data error:"+response.data);
    });
		},0);
    }
console.log("test");
//$scope.topics=[112,'2313','3131'];
//poll();


	}]);
