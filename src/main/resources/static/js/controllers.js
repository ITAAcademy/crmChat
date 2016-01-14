'use strict';

/* Controllers */

angular.module('springChat.controllers', ['toaster','ngRoute','ngResource'])
/*.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
            .when('/', {
                templateUrl: 'chat.html',
                controller: 'ChatController',
                controllerAs: 'chatController',
                reloadOnSearch: false
            });
}])*/
	.controller('ChatController', ['$scope', '$location', '$interval', 'toaster', 'ChatSocket', function($scope, $location, $interval, toaster, chatSocket) {
		  
		var typing = undefined;

		var serverPrefix = "";//"/crmChat";
		var room = "default_room/";
		var lastRoomBindings = [];
		
//Format string
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
		
		
		$scope.username     = '';
		$scope.sendTo       = 'everyone';
		$scope.participants = [];
		$scope.messages     = [];
		$scope.newMessage   = '';
		$scope.roomId		= '';
		
		$scope.changeRoom=function(){
			room=$scope.roomId+'/';
			onConnect();
		}
		  
		$scope.sendMessage = function() {
			var destination = "/app/{0}chat.message".format(room);
			
			if($scope.sendTo != "everyone") {
				destination = "/app/{0}chat.private.".format(room) + $scope.sendTo;
				$scope.messages.unshift({message: $scope.newMessage, username: 'you', priv: true, to: $scope.sendTo});
			}
			
			chatSocket.send(destination, {}, JSON.stringify({message: $scope.newMessage}));
			$scope.newMessage = '';
		};
		
		$scope.startTyping = function() {
			// Don't send notification if we are still typing or we are typing a private message
	        if (angular.isDefined(typing) || $scope.sendTo != "everyone") return;
	        
	        typing = $interval(function() {
	                $scope.stopTyping();
	            }, 500);
	        
	        chatSocket.send("/topic/{0}chat.typing".format(room), {}, JSON.stringify({username: $scope.username, typing: true}));
		};
		
		$scope.stopTyping = function() {
			if (angular.isDefined(typing)) {
		        $interval.cancel(typing);
		        typing = undefined;
		        
		        chatSocket.send("/topic/{0}chat.typing".format(room), {}, JSON.stringify({username: $scope.username, typing: false}));
			}
		};
		
		$scope.privateSending = function(username) {
				$scope.sendTo = (username != $scope.sendTo) ? username : 'everyone';
		};
		var onConnect = function(frame) {
			  console.log("onconnect");
			$scope.username = frame.headers['user-name'];
			{
				var isLastRoomBindingsEmpty = lastRoomBindings==undefined || lastRoomBindings.length == 0;
			if ( !isLastRoomBindingsEmpty ) {
				
				while (lastRoomBindings.length>0)
					{
				var subscription = lastRoomBindings.pop();
				//if (subscription!=undefined)
					subscription.unsubscribe();
					}
			}
			}
		
				var test = chatSocket.subscribe("/app/{0}chat.participants".format(room), function(message) {
				$scope.participants = JSON.parse(message.body);
			});
				lastRoomBindings.push(test);
	
			lastRoomBindings.push(
				chatSocket.subscribe("/topic/{0}chat.login".format(room), function(message) {
				$scope.participants.unshift({username: JSON.parse(message.body).username, typing : false});
			}));
			
			lastRoomBindings.push(
			chatSocket.subscribe("/topic/{0}chat.logout".format(room), function(message) {
				var username = JSON.parse(message.body).username;
				for(var index in $scope.participants) {
					if($scope.participants[index].username == username) {
						$scope.participants.splice(index, 1);
					}
				}
	        }));
			
			lastRoomBindings.push(
			chatSocket.subscribe("/topic/{0}chat.typing".format(room), function(message) {
				var parsed = JSON.parse(message.body);
				if(parsed.username == $scope.username) return;
			  					
				for(var index in $scope.participants) {
					var participant = $scope.participants[index];
					  
					if(participant.username == parsed.username) {
						$scope.participants[index].typing = parsed.typing;
					}
			  	} 
			}));
	        	 
			lastRoomBindings.push(
			chatSocket.subscribe("/topic/{0}chat.message".format(room), function(message) {
				$scope.messages.unshift(JSON.parse(message.body));
	        }));
			  
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
		  
		initStompClient();
		/*$scope.$on('$routeUpdate', function(scope, next, current) {
			   console.log('address changed');
			});*/
		
		
		
	}]);
	