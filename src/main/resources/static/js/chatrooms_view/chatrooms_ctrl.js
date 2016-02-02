'use strict';

/* Controllers */
Object.prototype.getKeyByValue = function( value ) {
	for( var prop in this ) {
		if( this.hasOwnProperty( prop ) ) {
			if( this[ prop ] === value )
				return prop;
		}
	}
}
var Operations = Object.freeze({"send_message_to_all":"SEND_MESSAGE_TO_ALL",
	"send_message_to_user":"SEND_MESSAGE_TO_USER",
	"add_user_to_room":"ADD_USER_TO_ROOM",
	"add_room":"ADD_ROOM"});


chatroomsView.controller('chatroomsViewCtrl', ['$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','$routeParams',function($scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,$routeParams) {

	$scope.templateName = null;
	$scope.emails = [];


	var typing = undefined;
	var addingUserToRoom = undefined;
	var sendingMessage = undefined;
	var addingRoom = undefined;

	var serverPrefix = "/crmChat";
	var room = "default_room/";

	var room = "1/";
	var lastRoomBindings = [];

//	Format string
	if (!String.prototype.format) {
		String.prototype.format = function() {
			var args = arguments;
			return this.replace(/{(\d+)}/g, function(match, number) { 
				return typeof args[number] != 'undefined'
					? args[number]
				: match
				;
			});
		};
	}

	$scope.show_search_list = true;

	var getEmailsTimer;

	$scope.searchInputValue = {email: ""};

	$scope.hideSearchList = function () {
		setTimeout(function ()  {$scope.show_search_list = false; }, 20);
	}

	$scope.showSearchList = function () {

		$scope.show_search_list = true;
		$scope.emails = [];
		clearTimeout(getEmailsTimer);

		getEmailsTimer = setTimeout(function () {
			$scope.show_search_list = true;
			/*var data = {"login=" + message,
			var config = "";
			var buka = $http.post('/get_users_emails_like', data, config)
			.then(function (data, status, headers, config) {		 			
				$scope.emails = (data['data']);
			});*/
//			alert($scope.emails);	
			var request = $http({
				method: "get",
				url: serverPrefix + "/get_users_emails_like?login=" + $scope.searchInputValue.email + "&room=" + $scope.roomId,//'/get_users_emails_like',
				data: null ,
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
			});

			request.success(function (data) {
				$scope.emails = data;
			});
		}, 500);
	};

	$scope.appendToSearchInput = function(value) {
		$scope.searchInputValue.email = value;
		$scope.show_search_list = false;
		console.log($scope.searchInputValue.email);
	}

	var curentDateInJavaFromat = function() {
		var currentdate = new Date(); 
		var day = currentdate.getDate();
		if (day < "10")
			day = "0" + day;

		var mouth = (currentdate.getMonth()+1);
		if (mouth < "10")
			mouth = "0" + mouth;

		var datetime =  currentdate.getFullYear() + "-" + mouth + "-" +
		day +" " + currentdate.getHours() + ":"  
		+ currentdate.getMinutes() + ":" + currentdate.getSeconds()+".0";
		//console.log("------------------ " + datetime)
		return  datetime;
	};



	$scope.searchUserName = "";
	$scope.chatUserId     = '';
	$scope.sendTo       = 'everyone';
	$scope.participants = [];
	//$scope.roomsArray = [];
	$scope.dialogs = [];
	$scope.messages     = [];
	$scope.rooms     = [];
	$scope.roomsCount     = 0;
	$scope.newMessage   = '';
	$scope.roomId		= '';
	$scope.dialogShow = false;
	$scope.messageSended = true;
	$scope.userAddedToRoom = true;
	$scope.roomAdded = true;


	$scope.dialogName = '';


	$scope.addDialog = function() {
		$scope.roomAdded = false;
		console.log($scope.dialogName)
		chatSocket.send("/app/chat/rooms/add."+"{0}".format($scope.dialogName), {}, JSON.stringify({}));

		$scope.dialogName = '';
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
	};

	$scope.goToDialogList = function() {
		if ($scope.rooms[$scope.roomId] !== undefined )
			$scope.rooms[$scope.roomId].date = curentDateInJavaFromat();

		//$scope.templateName = 'dialogsTemplate.html';
		$scope.dialogName = '';

		chatSocket.send("/app/chat.go.to.dialog.list/{0}".format($scope.roomId), {}, JSON.stringify({}));
		$scope.roomId = -44;
	}



	$scope.goToDialog = function(roomName) {
		if ($scope.rooms[$scope.roomId] !== undefined )
			$scope.rooms[$scope.roomId].date = curentDateInJavaFromat();

		$scope.templateName = 'chatTemplate.html';
		$scope.dialogName = roomName;
		goToDialogEvn($scope.rooms.getKeyByValue(roomName));

	};

	function goToDialogEvn(id)
	{
		$scope.roomId = id;
		$scope.changeRoom();
		setTimeout(function(){ 
			$scope.rooms[$scope.roomId].nums = 0;
			$scope.roomsArray[$scope.roomId].nums = 0;
		}, 1000);

		chatSocket.send("/app/chat.go.to.dialog/{0}".format($scope.roomId), {}, JSON.stringify({}));
	}


	$scope.changeRoom=function(){
		$scope.messages=[];
		room=$scope.roomId+'/';
		var isLastRoomBindingsEmpty = lastRoomBindings==undefined || lastRoomBindings.length == 0;
		if ( !isLastRoomBindingsEmpty ) {

			while (lastRoomBindings.length>0)
			{
				var subscription = lastRoomBindings.pop();
				//if (subscription!=undefined)
				subscription.unsubscribe();
			}
		}

		lastRoomBindings.push(
				chatSocket.subscribe("/topic/{0}chat.message".format(room), function(message) {
					$scope.messages.unshift(JSON.parse(message.body));

				}));
		lastRoomBindings.push(
				chatSocket.subscribe("/topic/{0}chat.typing".format(room), function(message) {
					var parsed = JSON.parse(message.body);
					if(parsed.username == $scope.chatUserId) return;
					//debugger;
					//$scope.participants[parsed.username].typing = parsed.typing;
					for(var index in $scope.participants) {
						var participant = $scope.participants[index];

						if(participant.chatUserId == parsed.username) {
							$scope.participants[index].typing = parsed.typing;
							//break;
						}
					} 
				}));

		lastRoomBindings.push(chatSocket.subscribe("/app/{0}chat.participants".format(room), function(message) {
			var o = JSON.parse(message.body);
			console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!			" );
			console.log(o);
			//$scope.messages = [];
			$scope.participants = o["participants"];
			for (var i=0; i< o["messages"].length;i++){
				$scope.messages.unshift(o["messages"][i]);
				//$scope.messages.unshift(JSON.parse(o["messages"][i].text));
			}
		}));
		lastRoomBindings.push(chatSocket.subscribe("/topic/{0}chat.participants".format(room), function(message) {
			var o = JSON.parse(message.body);
			console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!			" );
			console.log(o);
			$scope.participants = o["participants"];
		}));
		//chatSocket.send("/topic/{0}chat.participants".format(room), {}, JSON.stringify({}));
	}

	/*$scope.addRoom=function(name){

		chatSocket.send("/app/chat/rooms/add.{0}".format(name), {}, JSON.stringify({}));

	}*/
	$scope.addUserToRoom=function(){
		$scope.userAddedToRoom = false;
		room=$scope.roomId+'/';
		chatSocket.send("/app/chat/rooms.{0}/user.add.{1}".format($scope.roomId,$scope.searchInputValue.email), {}, JSON.stringify({}));
		$scope.searchInputValue.email = '';
		var myFunc = function(){
			if (angular.isDefined(addingUserToRoom))
			{
				$timeout.cancel(addingUserToRoom);
				addingUserToRoom=undefined;
			}
			if ($scope.userAddedToRoom) return;
			toaster.pop('error', "Error","server request timeout",1000);
			$scope.userAddedToRoom = true;

		};
		addingUserToRoom = $timeout(myFunc,6000);
		/*
		setTimeout(function(){ 
			chatSocket.send("/app/{0}chat.participants".format(room), {}, JSON.stringify({}));
		    }, 3000);  */
	}

	$scope.sendMessage = function() {
		var destination = "/app/{0}chat.message".format(room);
		$scope.messageSended = false;
		if($scope.sendTo != "everyone") {
			destination = "/app/{0}chat.private.".format(room) + $scope.sendTo;
			$scope.messages.unshift({message: $scope.newMessage, username: 'you', priv: true, to: $scope.sendTo});
		}

		chatSocket.send(destination, {}, JSON.stringify({message: $scope.newMessage}));
		$scope.newMessage = '';
		var myFunc = function(){
			if (angular.isDefined(sendingMessage))
			{
				$timeout.cancel(sendingMessage);
				sendingMessage=undefined;
			}
			if ($scope.messageSended) return;
			toaster.pop('error', "Error","server request timeout",1000);
			$scope.messageSended = true;

		};
		sendingMessage = $timeout(myFunc,2000);
	};



	$scope.startTyping = function() {
		// Don't send notification if we are still typing or we are typing a private message
		if (angular.isDefined(typing) || $scope.sendTo != "everyone") return;

		typing = $interval(function() {
			$scope.stopTyping();
		}, 500);

		chatSocket.send("/topic/{0}chat.typing".format(room), {}, JSON.stringify({username: $scope.chatUserId, typing: true}));
	};

	$scope.stopTyping = function() {
		if (angular.isDefined(typing)) {
			$interval.cancel(typing);
			typing = undefined;

			chatSocket.send("/topic/{0}chat.typing".format(room), {}, JSON.stringify({username: $scope.chatUserId, typing: false}));

		}
	};

	$scope.privateSending = function(username) {
		$scope.sendTo = (username != $scope.sendTo) ? username : 'everyone';
	};
	/*
	 $http.post(serverPrefix + "/index.html", {"username":"initIntita", "password":"initIntita"}, { headers: { 'Content-Type': 'application/x-www-form-urlencoded'}})
     .success(function (data, status, headers, config) {
    	 console.log("YRAAAAAAAAAAAAAAAAAAA");
    		initStompClient();
     }
     )
     .error(function (data, status, header, config) {
     });*/


	/*$scope.$on('$routeUpdate', function(scope, next, current) {
			   console.log('address changed');
			});*/
}]);



