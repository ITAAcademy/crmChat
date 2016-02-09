'use strict';

/* Controllers */
function getPropertyByValue(obj, value ) {
	for( var prop in obj ) {
		if( obj.hasOwnProperty( prop ) ) {
			if( obj[ prop ] === value )
				return prop;
		}
	}
}
function getIdInArrayFromObjectsMap(roomNameMap,propertyName,valueToFind){

for (var item in roomNameMap)
	if(roomNameMap[item][propertyName]==valueToFind) return item;
		debugger;
return undefined;
}


var Operations = Object.freeze({"send_message_to_all":"SEND_MESSAGE_TO_ALL",
	"send_message_to_user":"SEND_MESSAGE_TO_USER",
	"add_user_to_room":"ADD_USER_TO_ROOM",
	"add_room":"ADD_ROOM"});

var springChatController = angular.module('springChat.controllers', ['toaster','ngRoute','ngResource','ngCookies']);

springChatController.controller('ChatController', ['$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore',function($rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore) {

	$scope.templateName = null;
	$rootScope.socketSupport = true;
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
	$scope.httpPromise     = [];
	$scope.rooms     = [];
	$scope.roomsCount     = 0;
	$scope.newMessage   = '';
	$scope.roomId		= '';
	$scope.dialogShow = false;
	$scope.messageSended = true;
	$scope.userAddedToRoom = true;
	$scope.roomAdded = true;
	$scope.showDialogListButton = false;
	$scope.dialogName = '';

	

	$scope.addDialog = function() {
		if ($rootScope.socketSupport){
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
	}
	//SOcket don't support case
	else {
		$http.post(serverPrefix + "/chat/rooms/add", $scope.dialogName).
			success(function(data, status, headers, config) {
				console.log("ADD USER OK " + data);
				$scope.userAddedToRoom = true;
			}).
			error(function(data, status, headers, config) {
				$scope.userAddedToRoom = true;
			});
	}


	};

	$scope.goToDialogList = function() {
		if ($scope.rooms[$scope.roomId] !== undefined )
			$scope.rooms[$scope.roomId].date = curentDateInJavaFromat();

		$scope.templateName = 'dialogsTemplate.html';
		$scope.dialogName = '';

		chatSocket.send("/app/chat.go.to.dialog.list/{0}".format($scope.roomId), {}, JSON.stringify({}));
		$scope.roomId = -44;
	}


	$scope.goToDialog = function(roomName) {
		console.log("roomName:"+roomName);
		if ($scope.rooms[$scope.roomId] !== undefined )
			$scope.rooms[$scope.roomId].date = curentDateInJavaFromat();

		$scope.templateName = 'chatTemplate.html';
		console.log("room name:"+roomName);
		$scope.dialogName = roomName;
		var key = getIdInArrayFromObjectsMap($scope.rooms,"string",roomName);
		console.log("gotoDialog key:"+key);

		goToDialogEvn(key);

	};

	function goToDialogEvn(id)
	{
		console.log("goToDialogEvn("+id+")");
		$scope.roomId = id;
		$scope.changeRoom();
		setTimeout(function(){ 
			$scope.rooms[$scope.roomId].nums = 0;
			//$scope.roomsArray[$scope.roomId].nums = 0;
		}, 1000);

		chatSocket.send("/app/chat.go.to.dialog/{0}".format($scope.roomId), {}, JSON.stringify({}));
	}


	/*************************************
	 * CHANGE ROOM
	 *************************************/
	$scope.changeRoom=function(){
		$scope.messages=[];
		console.log("roomId:"+$scope.roomId);
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
		
		while ($scope.httpPromise.length>0)
		{
			var subscription = $scope.httpPromise.pop();
			subscription.success(null).error(null);
			
		}

		if($rootScope.socketSupport == true)
		{
			lastRoomBindings.push(
				chatSocket.subscribe("/topic/{0}chat.message".format(room), function(message) 
				{
					$scope.messages.unshift(JSON.parse(message.body));
	
				}));

			lastRoomBindings.push(chatSocket.subscribe("/app/{0}chat.participants".format(room), function(message) 
			{
				var o = JSON.parse(message.body);
				loadSubscribeAndMessage(o);
			}));

			lastRoomBindings.push(chatSocket.subscribe("/topic/{0}chat.participants".format(room), function(message) 
			{
				var o = JSON.parse(message.body);
				console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!			" );
				console.log(o);
				$scope.participants = o["participants"];
			}));
		}
		else
		{
			
			subscribeMessageLP();//@LP@
			subscribeParticipantsLP();
			
			loadMessageLP();
		}	

		lastRoomBindings.push(
				chatSocket.subscribe("/topic/{0}chat.typing".format(room), function(message) {
					var parsed = JSON.parse(message.body);
					if(parsed.username == $scope.chatUserId) return;
					//$scope.participants[parsed.username].typing = parsed.typing;
					for(var index in $scope.participants) {
						var participant = $scope.participants[index];

						if(participant.chatUserId == parsed.username) {
							$scope.participants[index].typing = parsed.typing;
							//break;
						}
					} 
				}));


		//chatSocket.send("/topic/{0}chat.participants".format(room), {}, JSON.stringify({}));
	}

	/*$scope.addRoom=function(name){

		chatSocket.send("/app/chat/rooms/add.{0}".format(name), {}, JSON.stringify({}));

	}*/
	$scope.addUserToRoom=function(){
		$scope.userAddedToRoom = false;
		room=$scope.roomId+'/';
		if($rootScope.socketSupport === true)
		{
			chatSocket.send("/app/chat/rooms.{0}/user.add.{1}".format($scope.roomId,$scope.searchInputValue.email), {}, JSON.stringify({}));
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
		}
		else
		{
			console.log("$scope.searchInputValue:"+$scope.searchInputValue);
			$http.post(serverPrefix + "/chat/rooms.{0}/user.add.{1}".format($scope.roomId,$scope.searchInputValue.email), {}).
			success(function(data, status, headers, config) {
				console.log("ADD USER OK " + data);
				$scope.userAddedToRoom = true;
			}).
			error(function(data, status, headers, config) {
				$scope.userAddedToRoom = true;
			});
		}
		$scope.searchInputValue.email = '';
	
		/*
		setTimeout(function(){ 
			chatSocket.send("/app/{0}chat.participants".format(room), {}, JSON.stringify({}));
		    }, 3000);  */
	}

	/*************************************
	 * SEND MESSAGE
	 *************************************/
	$scope.sendMessage = function() {
		function messageError(){
			toaster.pop('error', "Error","server request timeout",1000);
		}
		var destination = "/app/{0}chat.message".format(room);
		$scope.messageSended = false;
		if($rootScope.socketSupport == true)
		{
			if($scope.sendTo != "everyone") {
				destination = "/app/{0}chat.private.".format(room) + $scope.sendTo;
				$scope.messages.unshift({message: $scope.newMessage, username: 'you', priv: true, to: $scope.sendTo});
			}

			chatSocket.send(destination, {}, JSON.stringify({message: $scope.newMessage}));
			var myFunc = function(){
				if (angular.isDefined(sendingMessage))
				{
					$timeout.cancel(sendingMessage);
					sendingMessage=undefined;
				}
				if ($scope.messageSended) return;
				messageError();
				$scope.messageSended = true;

			};
			sendingMessage = $timeout(myFunc,2000);
		}
		else
		{
			$http.post(serverPrefix + "/{0}chat/message".format(room), {message: $scope.newMessage}).
			success(function(data, status, headers, config) {
				console.log("MESSAGE SEND OK " + data);
				$scope.messageSended = true;
			}).
			error(function(data, status, headers, config) {
				messageError();
				$scope.messageSended = true;
			});
		};
		$scope.newMessage = '';

	}
	/*************************************
	 * LOAD MESSAGE LP
	 *************************************/

	function loadSubscribeAndMessage(message)
	{
		$scope.participants = message["participants"];
		for (var i=0; i< message["messages"].length;i++){
			$scope.messages.unshift(message["messages"][i]);
			//$scope.messages.unshift(JSON.parse(o["messages"][i].text));
		}
	}
	
	function loadMessageLP(){
		$http.post(serverPrefix + "/{0}chat/participants".format(room), {}).
		success(function(data, status, headers, config) {
			console.log("MESSAGE SEND OK " + data);
			loadSubscribeAndMessage(data);
		}).
		error(function(data, status, headers, config) {

		});
	}

	/*************************************
	* UPDATE ROOM LP
	**************************************/
	function subscribeRoomsUpdateLP(){
			console.log("roomsUpdateLP()");
			 $http.post(serverPrefix+"/chat/rooms/user.login")
    .success(function(data, status, headers, config) {
    	console.log("roomsUpdateLP data:"+data);
        updateRooms(data);
        //console.log("resposnse data received:"+response.data);
        subscribeRoomsUpdateLP();
    }).error(function errorHandler(data, status, headers, config) {
    	//console.log("error during http request");
    	//$scope.topics = ["error"];
         //console.log("resposnse data error:"+response.data);
         subscribeRoomsUpdateLP();
    });
		
    }



	/*************************************
	 * UPDATE MESSAGE LP
	 *************************************/
	function subscribeMessageLP(){
		$scope.httpPromise.push($http.post(serverPrefix + "/{0}chat/message/update".format(room)).

		success(function(data, status, headers, config) {
			console.log("MESSAGES GET OK ");

			subscribeMessageLP();
			for(var index in data) { 
				if(data[index].hasOwnProperty("message"))
					$scope.messages.unshift(data[index])
			}


		}).
		error(function(data, status, headers, config) {
			console.log("MESSAGES GET FALSE ");
				subscribeMessageLP();
		}));
	};
	
	function subscribeParticipantsLP(){
		$scope.httpPromise.push($http.post(serverPrefix + "/{0}/chat/participants/update".format(room)).

		success(function(data, status, headers, config) {
			console.log("SUBSCRIBE GET OK ");
			//if($scope.httpPromise.indexOf(this) > 0)
			subscribeParticipantsLP();
				if(data.hasOwnProperty("participants"))
					$scope.participants = data["participants"];


		}).
		error(function(data, status, headers, config) {
			console.log("SUBSCRIBE GET FALSE ");
			//if($scope.httpPromise.indexOf(this) > 0)
			subscribeParticipantsLP();
		}));
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
	function login(mess_obj)
	{
		$scope.chatUserId = mess_obj.chat_id;
		if(mess_obj.nextWindow == -1)
		{
			toaster.pop('error', "Authentication err", "...Try later",{'position-class':'toast-top-full-width'});
			return;
		}

		if(mess_obj.nextWindow == 0)
		{
			$scope.showDialogListButton = true;
			$scope.goToDialogList();
		}
		else
		{
			goToDialogEvn(mess_obj.nextWindow);
			$scope.templateName = 'chatTemplate.html';
			toaster.pop('note', "Wait for teacher connect", "...thank",{'position-class':'toast-top-full-width'});
		}
	}
	function updateRooms(message)
	{

		if ($rootScope.socketSupport)
		{
		$scope.rooms = JSON.parse(message.body);
		debugger;
		}
		else
		{
		$scope.rooms = message;
		}
		console.log("roomsArray:"+$scope.roomsArray);
		$scope.roomsArray = Object.keys($scope.rooms)
		.map(function(key) {
			return $scope.rooms[key];
		});
console.log("roomsArray:"+$scope.roomsArray);

		$scope.roomsCount = Object.keys($scope.rooms).length;
		console.log($scope.rooms);
	

	}

	
	var onConnect = function(frame) {
		console.log("onconnect");
	
		$scope.chatUserId = frame.headers['user-name'];
		
		

		if($rootScope.socketSupport)//if websocket enabled $rootScope.socketSupport
		{
			lastRoomBindings.push(
					chatSocket.subscribe("/app/chat.login/{0}".format($scope.chatUserId)  , function(message) {
						//$scope.participants.unshift({username: JSON.parse(message.body).username, typing : false});
						//console.log("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOn");
						var mess_obj = JSON.parse(message.body);

						login(mess_obj);

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
		}
		


		/*
		 * 
		 *Room
		 *
		 */

		chatSocket.subscribe("/app/chat/rooms/user.{0}".format($scope.chatUserId), function(message) {// event update
			updateRooms(message);
		});
		chatSocket.subscribe("/topic/chat/rooms/user.{0}".format($scope.chatUserId), function(message) {// event update
			updateRooms(message);
		});
		

		chatSocket.subscribe("/topic/users/must/get.room.num/chat.message", function(message) {// event update

			var num = JSON.parse(message.body);
			Object.keys($scope.rooms).forEach(function(value) {
				if (value == num && $scope.roomId != value)
				{
					$scope.rooms[value].nums++;
					$scope.rooms[value].date = curentDateInJavaFromat();//@TEST@
					//$scope.roomsArray[value].nums++;
					new Audio('new_mess.mp3').play();
					toaster.pop('note', "NewMessage in " + $scope.rooms[value].string, "",1000);

				}
			});
		});

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
				$scope.userAddedToRoom = true;
				if (!operationStatus.success)
					toaster.pop("error", "Error","user wasn't added to room");
				break;
			case Operations.add_room:
				$scope.roomAdded = true;
				if (!operationStatus.success)
					toaster.pop("error", "Error","room wasn't added");

			}
			//ZIGZAG OPS

			console.log("SERVER MESSAGE OPERATION STATUS:"+operationStatus.success+ operationStatus.description);
		});



		/*
		 setTimeout(function(){ 

			 chatSocket.send("/app/chat/rooms", {}, JSON.stringify({}));
			 chatSocket.send("/app//chat/rooms.1/user.add.user", {}, JSON.stringify({}));
		    }, 3000);  

		 */
		/*lastRoomBindings.push(
				chatSocket.subscribe("/user/exchange/amq.direct/{0}chat.message".format(room), function(message) {
					var parsed = JSON.parse(message.body);
					parsed.priv = true;
					$scope.messages.unshift(parsed);
				}));*/

		lastRoomBindings.push(
				chatSocket.subscribe("/user/exchange/amq.direct/errors", function(message) {
					toaster.pop('error', "Error", message.body);
				}));

	};

	var initStompClient = function() {

		chatSocket.init(serverPrefix+"/ws");

		chatSocket.connect(onConnect, function(error) {

			toaster.pop('error', 'Error', 'Websocket not supportet or server not exist' + error);
			$rootScope.socketSupport = false;
			subscribeRoomsUpdateLP();
			/***************************************
			 * TRY LONG POLING LOGIN
			 **************************************/
			//$scope.chatUserId = frame.headers['user-name'];
			$http.post(serverPrefix + "/chat/login/login", {message: $scope.newMessage}).
			success(function(data, status, headers, config) {
				console.log("LOGIN OK " + data);
				login(data);
				
				/*
				 * 
				 * 
				 */
				/*$http.post(serverPrefix + "/chat/rooms/user.login", {}).
				success(function(data, status, headers, config) {
					console.log("ROOMS OK " + data);
					
					$scope.rooms = data;
					$scope.roomsArray = Object.keys($scope.rooms)
					.map(function(key) {
						return $scope.rooms[key];
					});

					$scope.roomsCount = Object.keys($scope.rooms).length;
					console.log($scope.rooms);
				}).
				error(function(data, status, headers, config) {
					//messageError();
					toaster.pop('error', "Authentication err", "...Try later",{'position-class':'toast-top-full-width'});
				});*/
				/*
				 * 
				 * 
				 */
				
			}).
			error(function(data, status, headers, config) {
				messageError();
				toaster.pop('error', "Authentication err", "...Try later",{'position-class':'toast-top-full-width'});
			});
			
			
			
		});
		/*
		if(!$rootScope.socketSupport)
		{
			//@LP INIT@
		}
		 */
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
	function send(destination, data, ok_funk, err_funk)
	{
		/*var formData = new FormData();

		// добавить к пересылке ещё пару ключ - значение

		formData.append("username", "username");
		formData.append("password", "password");
		 */
		// отослать

		var xhr = getXmlHttp();
		xhr.open("POST", serverPrefix + destination, true);
		xhr.onreadystatechange= function(){
			if (xhr.readyState==4 || xhr.readyState=="complete")
			{
				ok_funk();
			}
			else
				err_funk();

		}
		xhr.send(data);
	}


	initStompClient();
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



