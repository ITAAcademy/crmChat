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
								"add_user_to_room":"ADD_USER_TO_ROOM"});

var phonecatApp = angular.module('springChat.controllers', ['toaster','ngRoute','ngResource','ngCookies']);

phonecatApp.controller('ChatController', ['$scope', '$http', '$location', '$interval','$timeout','$cookies', 'toaster', 'ChatSocket', function($scope, $http, $location, $interval,$timeout,$cookies, toaster, chatSocket) {

	$scope.templateName = null;
	$scope.emails = [];
	

	var typing = undefined;
	var addingUserToRoom = undefined;
	var sendingMessage = undefined;

	//var serverPrefix = "";//"/crmChat";
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



	$scope.searchUserName = "";
	$scope.chatUserId     = '';
	$scope.sendTo       = 'everyone';
	$scope.participants = [];
	$scope.dialogs = [];
	$scope.messages     = [];
	$scope.rooms     = [];
	$scope.roomsCount     = 0;
	$scope.newMessage   = '';
	$scope.roomId		= '';
	$scope.dialogShow = false;
	$scope.messageSended = true;
	$scope.userAddedToRoom = true;


	$scope.dialogName = '';


	$scope.addDialog = function() {
		console.log($scope.dialogName)
		chatSocket.send("/app/chat/rooms/add."+"{0}".format($scope.dialogName), {}, JSON.stringify({}));

		setTimeout(function(){ //@BAG@
			chatSocket.send("/app/chat/rooms/user.{0}".format($scope.chatUserId), {}, JSON.stringify({}));
		}, 1000);  

		$scope.dialogName = '';
	};

	$scope.goToDialogList = function() {
		$scope.templateName = 'dialogsTemplate.html';
		$scope.dialogName = '';
		
		chatSocket.send("/app/chat.go.to.dialog.list/{0}".format($scope.roomId), {}, JSON.stringify({}));
		$scope.roomId = -44;
	}



	$scope.goToDialog = function(roomName) {
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
		lastRoomBindings.push(
				chatSocket.subscribe("/topic/users/{0}/status".format($scope.chatUserId), function(message) {
					var operationStatus = JSON.parse(message.body);
					switch (operationStatus.type){
					case Operations.send_message_to_all:
					case Operations.send_message_to_user:
					$scope.messageSended = true;
						if (!operationStatus.success)
							toaster.pop("error", "Error", message.body);
						break;
					case Operations.add_user_to_room:
					if (operationStatus.success)$scope.userAddedToRoom = true;
					else {
						toaster.pop("error", "Error","user wasn't added to room");
					}
					}
					//ZIGZAG OPS
							
					console.log("SERVER MESSAGE OPERATION STATUS:"+operationStatus.success+ operationStatus.description);
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

	$scope.addRoom=function(name){
		chatSocket.send("/app/chat/rooms/add.{0}".format(name), {}, JSON.stringify({}));
	}
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
	var onConnect = function(frame) {
		console.log("onconnect");


		$scope.chatUserId = frame.headers['user-name'];



		lastRoomBindings.push(
				chatSocket.subscribe("/app/chat.login/{0}".format($scope.chatUserId)  , function(message) {
					//$scope.participants.unshift({username: JSON.parse(message.body).username, typing : false});
					//console.log("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOn");
					var mess_obj = JSON.parse(message.body);
					if(mess_obj.nextWindow == 0)
					{
						$scope.goToDialogList();
					}
					else
					{
						goToDialogEvn(mess_obj.nextWindow);
						$scope.templateName = 'chatTemplate.html';
						toaster.pop('note', "Wait for teacher connect", "...thank",{'position-class':'toast-top-full-width'});
					}
					
					
				}));

		lastRoomBindings.push(
				chatSocket.subscribe("/topic/chat.logout".format(room), function(message) {
					/*	var username = JSON.parse(message.body).username;
					for(var index in $scope.participants) {
						if($scope.participants[index].username == username) {
							$scope.participants.splice(index, 1);
						}
					}
					 */
				}));


		/*
		 * 
		 *Room
		 *
		 */

		chatSocket.subscribe("/app/chat/rooms/user.{0}".format($scope.chatUserId), function(message) {// event update
			$scope.rooms = JSON.parse(message.body);
			$scope.roomsCount = Object.keys($scope.rooms).length;
			console.log($scope.rooms);	
		});
		chatSocket.subscribe("/topic/chat/rooms/user.{0}".format($scope.chatUserId), function(message) {// event update
			$scope.rooms = JSON.parse(message.body);
			$scope.roomsCount = Object.keys($scope.rooms).length;
			console.log($scope.rooms);
		});



		chatSocket.subscribe("/topic/users/must/get.room.num/chat.message", function(message) {// event update

			var num = JSON.parse(message.body);
			//console.log("9999999999999999999 "+ num);
			Object.keys($scope.rooms).forEach(function(value) {
				  console.log("PPPPPPPPPPPPPP " + value + " " + $scope.roomId);
				if (value == num && $scope.roomId != value)
				{
					$scope.rooms[value].nums++;
					new Audio('new_mess.mp3').play();
					toaster.pop('note', "NewMessage in " + $scope.rooms[value].string, "",1000);
					//console.log("SSSSSSSSSSSS  " + $scope.rooms[value].bool );
				}
			});
		});



		/*
		 setTimeout(function(){ 

			 chatSocket.send("/app/chat/rooms", {}, JSON.stringify({}));
			 chatSocket.send("/app//chat/rooms.1/user.add.user", {}, JSON.stringify({}));
		    }, 3000);  

		 */
		lastRoomBindings.push(
				chatSocket.subscribe("/user/exchange/amq.direct/{0}chat.message".format(room), function(message) {
					var parsed = JSON.parse(message.body);
					parsed.priv = true;
					$scope.messages.unshift(parsed);
				}));

		lastRoomBindings.push(
				chatSocket.subscribe("/user/exchange/amq.direct/errors", function(message) {
					toaster.pop('error', "Error", message.body);
				}));

	};

	var initStompClient = function() {

		chatSocket.init(serverPrefix+"/ws");

		chatSocket.connect(onConnect, function(error) {
			toaster.pop('error', 'Error', 'Connection error ' + error);

		});
	};
	function getXmlHttp(){
		var xmlhttp;
		try {
			xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (E) {
				xmlhttp = false;
			}
		}
		if (!xmlhttp && typeof XMLHttpRequest!='undefined') {
			xmlhttp = new XMLHttpRequest();
		}
		return xmlhttp;
	}
	var formData = new FormData();

	// добавить к пересылке ещё пару ключ - значение
	var sessionValue =  $cookies['JSESSIONID'];

	formData.append("username", sessionValue);
	formData.append("password", "password");

	// отослать

	var xhr = getXmlHttp();
	xhr.open("POST", serverPrefix + "/index.html",false);
	xhr.onreadystatechange= function(){
		if (xhr.readyState==4 || xhr.readyState=="complete")
		{
			initStompClient();
		}
	}
	xhr.send(formData);


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

