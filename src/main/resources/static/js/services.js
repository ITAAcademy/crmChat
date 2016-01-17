'use strict';

/* Services */

angular.module('springChat.services', [])
	.factory('ChatSocket', ['$rootScope', function($rootScope) {
			var stompClient;

			var wrappedSocket = {
					
					init: function(url) {
						stompClient = Stomp.over(new SockJS(url));
					},
					disconnect: function (){
						stompClient.disconnect();
					},
					connect: function(successCallback, errorCallback) {
						
						stompClient.connect({}, function(frame) {
							$rootScope.$apply(function() {
								successCallback(frame);
							});
						}, function(error) {
							$rootScope.$apply(function(){
								errorCallback(error);
							});
				        });
					},
					subscribe : function(destination, callback) {
						var res= stompClient.subscribe(destination, function(message) {
							  $rootScope.$apply(function(){
								  callback(message);
							  });
				          });	
						return res;
					},
					send: function(destination, headers, object) {
						stompClient.send(destination, headers, object);
					}
			}
			
			return wrappedSocket;
		
	}]);