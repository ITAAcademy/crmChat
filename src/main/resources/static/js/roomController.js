springChatControllers.controller('DialogsRouteController',['$q','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes',function($q,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes) {
	Scopes.store('DialogsRouteController', $scope);
	var chatControllerScope = Scopes.get('ChatController');

	/*
	 * 
	 */
	$scope.dialogName = '';
	
	$scope.addDialog = function() {
		if ($rootScope.socketSupport){
			chatControllerScope.roomAdded = false;
			chatSocket.send("/app/chat/rooms/add."+"{0}".format($scope.dialogName), {}, JSON.stringify({}));

			var myFunc = function(){
				if (angular.isDefined(addingRoom))
				{
					$timeout.cancel(addingRoom);
					addingRoom=undefined;
				}
				if ($scope.roomAdded) return;
				toaster.pop('error', "Error","server request timeout",1000);
				$scope.roomAdded = true;
				console.log("!!!!!!!!!!!!!!!!!!!!!Room added");

			};
			addingRoom = $timeout(myFunc,6000);
		}
		//SOcket don't support case
		else {
			$http.post(serverPrefix + "/chat/rooms/add", $scope.dialogName).
			success(function(data, status, headers, config) {
				console.log("ADD USER OK " + data);
				chatControllerScope.userAddedToRoom = true;
			}).
			error(function(data, status, headers, config) {
				chatControllerScope.userAddedToRoom = true;
				toaster.pop('error', "Error","server request timeout",1000);
			});
		}
		$scope.dialogName = '';
	};

	$scope.goToDialogList = function() {

		if (chatControllerScope.currentRoom!==undefined && getRoomById(chatControllerScope.rooms, chatControllerScope.currentRoom.date) !== undefined )
			getRoomById(chatControllerScope.rooms, chatControllerScope.currentRoom.roomId).date = curentDateInJavaFromat();

		//$scope.templateName = 'dialogsTemplate.html';
		//changeLocation("/chatrooms")
		$scope.dialogName = '';
	if (typeof chatControllerScope.currentRoom.roomId != 'undefined')
	{
		if ($rootScope.socketSupport){
			chatSocket.send("/app/chat.go.to.dialog.list/{0}".format(chatControllerScope.currentRoom.roomId), {}, JSON.stringify({}));	
		}
		else
		{
			$http.post(serverPrefix + "/chat.go.to.dialog.list/{0}".format(chatControllerScope.currentRoom.roomId));
		}
	}
	else console.log('ERROR:chat.go.to.dialog.list:currentRoom.roomId is null');

		chatControllerScope.currentRoom = {roomId: ''} ;
	}
	
	/*
	 * 
	 */
	if(isInited == true)
		$scope.goToDialogList();
	console.log("initing:"+chatControllerScope.socketSupport);
	$scope.pageClass = 'page-home';

}]);