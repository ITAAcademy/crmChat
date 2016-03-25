springChatControllers.controller('ChatRouteController',['$routeParams','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes','$q','$controller',function($routeParams,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes,$q, $controller) {
	angular.extend(this, $controller('ChatRouteInterface', {$scope: $scope}));
	/*
	 * 
	 */
	
	$rootScope.$watch('isInited',function(){
			console.log("try " + $rootScope.isInited);
			if($rootScope.isInited == true)
			$scope.goToDialog($routeParams.roomId).then(function() {
				chatControllerScope.currentRoom.roomId = $routeParams.roomId;
				$scope.pageClass = 'page-about';
			}, function(){alert("ERR")});
	});
	
}]);