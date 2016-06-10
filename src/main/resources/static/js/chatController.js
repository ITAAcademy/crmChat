springChatControllers.controller('ChatRouteController',['$routeParams','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes','$q','$controller',function($routeParams,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes,$q, $controller) {
	angular.extend(this, $controller('ChatRouteInterface', {$scope: $scope}));
	/*
	 * 
	 */
	 
	$scope.controllerName = "ChatRouteController";
	var chatControllerScope = Scopes.get('ChatController');
	$rootScope.$watch('isInited',function(){
			console.log("try " + chatControllerScope.currentRoom);
			if($rootScope.isInited == true)
			{
				if ($rootScope.socketSupport){
					$scope.goToDialog($routeParams.roomId).then(function() {
						chatControllerScope.currentRoom.roomId = $routeParams.roomId;
						$scope.pageClass = 'scale-fade-in';
					}, function(){
					chatControllerScope.changeLocation("/");
						//alert("ERR");
						location.reload();
					});
				}
				else
				{
					$scope.goToDialog($routeParams.roomId).then(function() {
						chatControllerScope.currentRoom.roomId = $routeParams.roomId;
						$scope.pageClass = 'scale-fade-in';
					}, function(){
						chatControllerScope.changeLocation("/");
						location.reload();
						//alert("ERR");
					});
				}
			}

	});
}]);
