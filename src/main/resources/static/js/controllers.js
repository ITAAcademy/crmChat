'use strict';

/* Controllers */
var springChatControllers = angular.module('springChat.controllers', ['infinite-scroll','toaster','ngRoute', 'ngAnimate','ngResource','ngCookies']);
springChatControllers.config(function($routeProvider){
	$routeProvider.when("/chatrooms",
			{
		templateUrl: "dialogsTemplate.html",
		controller: "DialogsRouteController"
			});
	$routeProvider.when("/dialog_view/:roomId/",
			{
		templateUrl: "chatTemplate.html",
		controller: "ChatRouteController"
			});
	$routeProvider.when("/teachers_list",{
		templateUrl: "teachersTemplate.html",
		controller: "TeachersListRouteController"
	});
	$routeProvider.when("/private_dialog_view/:chatUserId",{
		templateUrl: "redirectPage.html",
		controller: "StrictedDialogRouteController"
	});
	$routeProvider.otherwise({redirectTo: '/'});
	console.log("scope test");

});

var isInited = false;

springChatControllers.controller('TeachersListRouteController',['$routeParams','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes',function($routeParams,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes) {
	Scopes.store('TeachersListRouteController', $scope);
	var chatControllerScope = Scopes.get('ChatController');
//	while (!chatControllerScope.isInited);//chatControllerScope.initStompClient();
//	typeof chatControllerScope.socketSupport!=='undefined'
	chatControllerScope.goToTeachersList();
	$scope.pageClass = 'page-contact';
}]);

springChatControllers.controller('StrictedDialogRouteController',['$routeParams','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes',function($routeParams,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes) {
	Scopes.store('StrictedDialogRouteController', $scope);
	var chatControllerScope = Scopes.get('ChatController');
	//if(isInited == true)
	chatControllerScope.goToPrivateDialog($routeParams.chatUserId);

}]);


var chatController = springChatControllers.controller('ChatController', ['$q','$rootScope','$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore','Scopes',function($q,$rootScope,$scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore,Scopes) {
	Scopes.store('ChatController', $scope);

	function changeLocation(url ) {
		$location.path(url);
		console.log("Change location:"+url);
		// $scope.$apply();

	};
	//$scope.templateName = null;
	$rootScope.socketSupport = true;

	var typing = undefined;
	var addingUserToRoom = undefined;
	var sendingMessage = undefined;
	var addingRoom = undefined;
	var changeLastRoomCanceler = $q.defer();

	var room = "default_room/";

	var room = "1/";

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

	$scope.emails = [];
	$scope.show_search_list_admin = false;

	var getEmailsTimer;

	$scope.searchInputValue = {email: ""};

	$scope.hideSearchList = function () {
		$timeout(function ()  {$scope.show_search_list = false; $scope.show_search_list_admin = false; }, 200);
	}

	$scope.showSearchList = function () {

		$scope.show_search_list = true;
		$scope.emails = [];
		$timeout.cancel(getEmailsTimer);

		getEmailsTimer = $timeout(function () {
			$scope.show_search_list = true;
			var request = $http({
				method: "get",
				url: serverPrefix + "/get_users_emails_like?login=" + $scope.searchInputValue.email + "&room=" + $scope.currentRoom.roomId,//'/get_users_emails_like',
				data: null ,
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
			});

			request.success(function (data) {
				$scope.emails = data;
			});
		}, 500);//for click event
	};

	$scope.appendToSearchInput = function(value) {
		console.log("searchInputValue:"+$scope.searchInputValue.email);
		$scope.searchInputValue.email = value;
		$scope.show_search_list = false;
	}

	$scope.showSearchListAdmin = function () {

		$scope.emails = [];
		$timeout.cancel(getEmailsTimer);

		getEmailsTimer = $timeout(function () {
			var request = $http({
				method: "get",
				url: serverPrefix + "/get_users_nicknames_like_without_room?nickName=" + $scope.searchResultAdmin, 
				data: null ,
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
			});

			request.success(function (data) {
				$scope.show_search_list_admin = true;
				$scope.emails = data;
			});
		}, 500);//for click event
	};

	$scope.returnToRealUser = function () {
		$scope.changeUser($scope.realChatUserId, $scope.realChatUserId);
		$scope.isMyRoom = true;
	}
	
	$scope.changeUser = function (chatUserId, chatUserNickName) {

		$scope.emails = [];

		$scope.chatUserId = chatUserId;
		$scope.chatUserNickname = chatUserNickName;
		$scope.searchResultAdmin = "";
		var temp_role = $scope.chatUserRole

		if($rootScope.socketSupport)
			initForWS(true);
		else
			reInitForLP();

		$timeout(function () {
			$scope.chatUserRole = temp_role;
		}, 2000);


		$scope.isMyRoom = false;
	}


	$scope.searchUserName = "";
	$scope.chatUserId     = -1;
	$scope.realChatUserId = -1;
	$scope.chatUserRole = 0;
	$scope.chatUserNickname = "";
	$scope.sendTo       = 'everyone';
	$scope.dialogs = [];
	$scope.rooms     = [];
	$scope.roomsCount     = 0;
	$scope.currentRoom		= {roomId:''};
	$scope.dialogShow = false;
	$scope.roomAdded = true;
	$scope.showDialogListButton = false;
	$scope.searchResultAdmin;
	$scope.isMyRoom = true;
	$scope.messageSended = true;
	$scope.userAddedToRoom = true;


	$scope.goToTeachersList = function() {

		$http.post(serverPrefix + "/chat/users").

		success(function(data, status, headers, config) {
			console.log("USERS GET OK ");
			$scope.seachersTeachers = data;
		}).
		error(function(data, status, headers, config) {
			$scope.seachersTeachers = [];
		});
	}
	$scope.goToPrivateDialog = function(intitaUserId) {
		$http.post(serverPrefix + "/chat/rooms/private/" + intitaUserId).
		success(function(data, status, headers, config) {
			console.log("PRIVATE ROOM CREATE OK ");

			//$scope.goToDialogById(data);
			$scope.currentRoom.roomId = data;
			changeLocation("/dialog_view/" + data);

		}).
		error(function(data, status, headers, config) {
			console.log("PRIVATE ROOM CREATE FAILD ");
		});
	}

	function subscribeInfoUpdateLP(){
		$http.post(serverPrefix+"/chat/global/lp/info")
		.success(function(data, status, headers, config) {
			//console.log("infoUpdateLP data:"+data);
			if(data["newMessage"] != null)//new message in room
			{
				for(var i in data["newMessage"])
					newMessageEvent(data["newMessage"][i]);
			}
			if (data["newGuestRoom"] != null){
				changeLocation("dialog_view/"+data["newGuestRoom"]);
			}
			subscribeInfoUpdateLP();
		}).error(function errorHandler(data, status, headers, config) {
			subscribeInfoUpdateLP();
		});

	}

	/*************************************
	 * UPDATE ROOM LP
	 **************************************/
	function subscribeRoomsUpdateLP(){
		console.log("roomsUpdateLP()");
		$http.post(serverPrefix+"/chat/rooms/user/login")
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

	$scope.checkRole = function()
	{
		if($scope.chatUserRole & 256)
			return true;
		return false;
	}

	function login(mess_obj)
	{
		isInited = true;
		$scope.chatUserId = mess_obj.chat_id;
		$scope.chatUserNickname = mess_obj.chat_user_nickname;
		$scope.chatUserRole = mess_obj.chat_user_role;

		if(mess_obj.nextWindow == -1)
		{
			toaster.pop('error', "Authentication err", "...Try later",{'position-class':'toast-top-full-width'});
			return;
		}

		if(mess_obj.nextWindow == 0)
		{
			if($rootScope.socketSupport == false)
			{
				updateRooms(JSON.parse(mess_obj.chat_rooms));
			}

			if ($scope.currentRoom.roomId != undefined && $scope.currentRoom.roomId != '')
			{
				//mess_obj.nextWindow=$scope.currentRoom.roomId;
				//	goToDialogEvn($scope.currentRoom.roomId);
				console.log("currentRoom");
				changeLocation("/dialog_view/" + $scope.currentRoom.roomId);
				$scope.showDialogListButton = true;
				return;
			}
			$scope.showDialogListButton = true;

			if($location.path() == "/")
				changeLocation("/chatrooms");
		}
		else
		{
			changeLocation("/dialog_view/" + mess_obj.nextWindow);
			toaster.pop('note', "Wait for teacher connect", "...thank",{'position-class':'toast-top-full-width'});
		}
	}
	function updateRooms(message)
	{

		if ($rootScope.socketSupport)
		{
			$scope.rooms = JSON.parse(message.body);
		}
		else
		{
			$scope.rooms = message;
		}
		$scope.roomsCount = $scope.rooms.length;
	}


	function newMessageEvent(roomId)
	{
		for (var roomIndex = 0; roomIndex < $scope.rooms.length; roomIndex++)
		{
			var room = $scope.rooms[roomIndex];
			if (room.roomId == roomId && $scope.currentRoom.roomId != room.roomId){
				room.nums++;
				console.log("room "+room.roomId + "=="+roomId+" currentRoom="+$scope.currentRoom.roomId);
				room.date=curentDateInJavaFromat();
				new Audio('data/new_mess.mp3').play();
				toaster.pop('note', "NewMessage in " + room.string, "",1000);
			}
		}
	}


	function initForWS(reInit)
	{
		chatSocket.subscribe("/app/chat.login/{0}".format($scope.chatUserId)  , function(message) {
			var mess_obj = JSON.parse(message.body);

			login(mess_obj);

			if(reInit == false)
				$timeout(function ()  {
					chatSocket.subscribe("/topic/chat.login".format(room), function(message) {

						var chatUserId = JSON.parse(message.body).chatUserId;
						$rootScope.$broadcast("login", chatUserId);
					});
					chatSocket.subscribe("/topic/chat.logout".format(room), function(message) {
						var chatUserId = JSON.parse(message.body).username;
						$rootScope.$broadcast("logout", chatUserId);

					});

					chatSocket.subscribe("/topic/users/must/get.room.num/chat.message", function(message) {// event update
						console.log("new message in room:"+message.body);
						var num = JSON.parse(message.body);
						newMessageEvent(num);
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
							break;
						case Operations.add_room_on_login:
							debugger;
							$timeout(function(){
							changeLocation("dialog_view/"+operationStatus.description);
						},1000);
							break;
						case Operations.add_room_from_tenant:
							//changeLocation("dialog_view/"+operationStatus.description);
							break;

						}
//						ZIGZAG OPS

						console.log("SERVER MESSAGE OPERATION STATUS:"+operationStatus.success+ operationStatus.description);
					});

					chatSocket.subscribe("/user/exchange/amq.direct/errors", function(message) {
						toaster.pop('error', "Error", message.body);
					});
				}, 1500);

		});

		/*
		 * 
		 *Room
		 *
		 */
		console.log("chatUserId:"+$scope.chatUserId);
		chatSocket.subscribe("/app/chat/rooms/user.{0}".format($scope.chatUserId), function(message) {// event update
			console.log("chatUserId:"+$scope.chatUserId);
			updateRooms(message);
		});
		chatSocket.subscribe("/topic/chat/rooms/user.{0}".format($scope.chatUserId), function(message) {// event update
			console.log("chatUserId:"+$scope.chatUserId);
			updateRooms(message);
		});
	}
	function reInitForLP()
	{
		$http.post(serverPrefix + "/chat/login/" + $scope.chatUserId, {message: $scope.newMessage}).
		success(function(data, status, headers, config) {
			console.log("LOGIN OK " + data);
			login(data);
		}).
		error(function(data, status, headers, config) {
			messageError();
			toaster.pop('error', "Authentication err", "...Try later",{'position-class':'toast-top-full-width'});
		});
	}

	var onConnect = function(frame) {
		console.log("onconnect");
		$scope.chatUserId = frame.headers['user-name'];
		initForWS(false);
		$scope.realChatUserId = $scope.chatUserId; 


	};


	var initStompClient = function() {

		console.log("initStompClient");

		chatSocket.init(serverPrefix+"/ws");


		chatSocket.connect(onConnect, function(error) {
			toaster.pop('error', 'Error', 'Websocket not supportet or server not exist' + error);
			$rootScope.socketSupport = false;
			/***************************************
			 * TRY LONG POLING LOGIN
			 **************************************/
			//$scope.chatUserId = frame.headers['user-name'];
			$http.post(serverPrefix + "/chat/login/" + $scope.chatUserId, {message: $scope.newMessage}).
			success(function(data, status, headers, config) {
				console.log("LOGIN OK " + data);
				login(data);
				subscribeRoomsUpdateLP();
				subscribeInfoUpdateLP();
				$scope.realChatUserId = $scope.chatUserId;
			}).
			error(function(data, status, headers, config) {
				messageError();
				toaster.pop('error', "Authentication err", "...Try later",{'position-class':'toast-top-full-width'});
			});




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
	function send(destination, data, ok_funk, err_funk)
	{
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

}]);



springChatControllers.factory('Scopes', function ($rootScope) {
	var mem = {};

	return {
		store: function (key, value) {
			mem[key] = value;
		},
		get: function (key) {
			return mem[key];
		}
	};
});
