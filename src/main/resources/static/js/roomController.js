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

	$('.selected-items-box').bind('click', function(e){
  e.stopPropagation();
  $('.multiple-select-wrapper .list').toggle('slideDown');
});

$('.multiple-select-wrapper .list').bind('click', function(e){
	e.stopPropagation();
});

$(document).bind('click', function(){
	$('.multiple-select-wrapper .list').slideUp();	
});
  
$scope.Airlines = [
  {selected: false, name:'SWISS', img:'http://s9.postimage.org/d9t33we17/Swiss.png'},
	{selected: false, name:'UNITED', img:'http://s9.postimage.org/ykqn85w5n/United.png'},
	{selected: false, name:'KLM', img:'http://s9.postimage.org/p7unhshsb/Klm.png'},
	{selected: false, name:'EL AL', img:'http://s18.postimage.org/oi8ndntud/image.gif'},
	{selected: false, name:'Ethiopian', img:'http://s9.postimage.org/hqlg2ks97/image.gif'}
];

$scope.getSelectedItemsOnly = function(item){
	return item.selected;
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