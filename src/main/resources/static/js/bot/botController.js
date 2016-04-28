'use strict';
springChatControllers.controller('ChatBotController',['$routeParams','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes','$q','$controller',function($routeParams,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes,$q, $controller) {
	angular.extend(this, $controller('ChatRouteInterface', {$scope: $scope}));
	/*
	 * 
	 */
	 
	$scope.name = "ChatBotController";
	var chatControllerScope = Scopes.get('ChatController');
	
	$rootScope.$watch('isInited',function(){
						
	});
}]);
