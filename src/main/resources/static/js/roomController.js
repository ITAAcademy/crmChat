
springChatControllers.controller('DialogsRouteController',['$q','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes',function($q,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes) {
	Scopes.store('DialogsRouteController', $scope);
	var chatControllerScope = Scopes.get('ChatController');

	/*
	 * 
	 */
	//$location.path("http://localhost/IntITA/site/authorize");//not found => go out
	$scope.dialogName = '';

	$scope.showNewRoomModal = false;
	$scope.toggleNewRoomModal = function(){
		$('#new_room_modal').modal('toggle');
	}
	$scope.dcGoToRoom = function(roomId)
	{
		debugger;
		if($scope.mouseBusy == false)
		chatControllerScope.changeLocation('/dialog_view/'+ roomId);
	}
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
		if ($rootScope.socketSupport){
			chatSocket.send("/app/chat.go.to.dialog.list/{0}".format(chatControllerScope.currentRoom.roomId), {}, JSON.stringify({}));	
		}
		else
		{
			$http.post(serverPrefix + "/chat.go.to.dialog.list/{0}".format(chatControllerScope.currentRoom.roomId));
		}

		chatControllerScope.currentRoom = {roomId: ''} ;
	}
	$scope.mouseBusy = false;

	$('.multiple-select-wrapper').bind('click', function(e){
		$scope.mouseBusy = true;
  e.stopPropagation();
  $('.multiple-select-wrapper .list').toggle('slideDown');
});

$('.multiple-select-wrapper .list').bind('click', function(e){
	e.stopPropagation();
});

$(document).bind('click', function(){
	$scope.mouseBusy = false;
	$('.multiple-select-wrapper .list').slideUp();	
});
  
$scope.Airlines = [
  {selected: true, name:'Анонімні', img:'http://s9.postimage.org/d9t33we17/Swiss.png'},
	{selected: true, name:'Приватні', img:'http://s9.postimage.org/ykqn85w5n/United.png'},
	{selected: true, name:'Консультації', img:'http://s9.postimage.org/p7unhshsb/Klm.png'}
];

$scope.getSelectedItemsOnly = function(item){
	return item.selected;
};

$scope.filterDialogsByType = function(value) {
if ($scope.Airlines[value.type].selected)
	return true;
else
	return false;
};



	
	/*
	 * 
	 */
	if($rootScope.isInited == true)
		$scope.goToDialogList();
	console.log("initing:"+chatControllerScope.socketSupport);
	$scope.pageClass = 'page-about';
	$scope.$$postDigest(function () {
		var nice = $(".chat-box").niceScroll();
		
	})

}]);