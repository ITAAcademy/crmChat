springChatApp.controller('chatroomsViewCtrl', ['$scope', '$http', '$location', '$interval','$cookies','$timeout','toaster', 
'ChatSocket', '$cookieStore',function($scope, $http, $location, $interval,$cookies,$timeout, toaster, chatSocket, $cookieStore) {
	
	$rootScope.emails = [];


	var typing = undefined;
	var addingUserToRoom = undefined;
	var sendingMessage = undefined;
	var addingRoom = undefined;

	var serverPrefix = "/crmChat";

	var room = "1/";
	var lastRoomBindings = [];

	var onConnect = function(frame) {
		console.log("onconnect");

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
		$scope.chatUserId = frame.headers['user-name'];

function goToDialogEvn(id)
	{
		$scope.roomId = id;
		$scope.changeRoom();
		setTimeout(function(){ 
			$scope.rooms[$scope.roomId].nums = 0;
			//$scope.roomsArray[$scope.roomId].nums = 0;
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
			toaster.pop('error', 'Error', 'Connection error ' + error);

		});
	};

	initStompClient();

}]);