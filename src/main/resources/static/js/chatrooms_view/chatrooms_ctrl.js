//var chatroomsView = angular.module('springChat.chatrooms_view', ['toaster','ngRoute','ngResource','ngCookies']);
chatroomsView.controller('chatroomsViewCtrl', ['$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 'ChatSocket', '$cookieStore',function($scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore) {

	$scope.emails = [];

	var addingRoom = undefined;

	var serverPrefix = "/crmChat";

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

	$scope.chatUserId     = '';
	//$scope.roomsArray = [];
	$scope.dialogs = [];
	$scope.rooms     = [];
	$scope.roomsCount     = 0;
	$scope.newMessage   = '';
	$scope.roomId		= '';
	$scope.dialogShow = false;
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

		$scope.templateName = 'dialogsTemplate.html';
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


	/*$scope.addRoom=function(name){

		chatSocket.send("/app/chat/rooms/add.{0}".focrmat(name), {}, JSON.stringify({}));

	}*/
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

		function updateRooms(message)
		{
			$scope.rooms = JSON.parse(message.body);

			$scope.roomsArray = Object.keys($scope.rooms)
			.map(function(key) {
				return $scope.rooms[key];
			});

			$scope.roomsCount = Object.keys($scope.rooms).length;
			console.log($scope.rooms);
		}
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
					$scope.rooms[value].date = new Date();//@TEST@
					//$scope.roomsArray[value].nums++;
					new Audio('new_mess.mp3').play();
					toaster.pop('note', "NewMessage in " + $scope.rooms[value].string, "",1000);

				}
			});
		});
		chatSocket.subscribe("/topic/users/{0}/status".format($scope.chatUserId), function(message) {
			var operationStatus = JSON.parse(message.body);
			switch (operationStatus.type){
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
			toaster.pop('error', 'Error', 'Connection error ' + error);

		});
	};

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




