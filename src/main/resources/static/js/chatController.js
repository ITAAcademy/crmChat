springChatControllers.controller('ChatRouteController',['$routeParams','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes','$q','$controller',function($routeParams,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes,$q, $controller) {
	angular.extend(this, $controller('ChatRouteInterface', {$scope: $scope}));
	/*
	 * 
	 */
	$rootScope.isWaiFreeTenatn = false;
	
	$rootScope.showToasterWaitFreeTenant = function () {
		//alert($rootScope.isConectedWithFreeTenant);
		if (!$rootScope.isWaiFreeTenatn) {
	    	toaster.pop({
	            type: 'wait',
	            body: 'Wait for free consultant',
	            timeout: 0,
	            onHideCallback: function () {             	
	            	if (!$rootScope.isConectedWithFreeTenant) {
		            	$rootScope.isWaiFreeTenatn = false;
		            	$rootScope.showToasterWaitFreeTenant();
	            	}
	            },
	            showCloseButton: false
			});
	    	$rootScope.isWaiFreeTenatn = true;
		}
}
	
	 
	$scope.controllerName = "ChatRouteController";
	var chatControllerScope = Scopes.get('ChatController');
	$rootScope.$watch('isInited',function() {
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
						if ($scope.needReloadPage != undefined)
							if ($scope.needReloadPage ) 
								return;

						chatControllerScope.changeLocation("/");
						location.reload();
						//alert("ERR");							
						scope.needReloadPage = true;
					});
				}
				
				//444
				//alert(444);
				$http.post(serverPrefix + "/bot_operations/tenant/did_am_wait_tenant").
		        success(function(data, status, headers, config) {
		        	if (data == true)
		        		$scope.showToasterWaitFreeTenant();
		        }).
		        error(function(data, status, headers, config) {
		            alert("did_am_wait_tenant: server error")
		        });
			}

	});
}]);
