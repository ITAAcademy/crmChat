'use strict';

/* Services */

var springChatServices = angular.module('springChat.services', []);
springChatServices.factory('ChatSocket', ['$rootScope', function($rootScope) {
			var stompClient;

			var wrappedSocket = {
					
					init: function(url) {
						var cock = new SockJS(url, null, {
						    'transports': ['websocket', 'xdr-streaming', 'xhr-streaming',
						                            'iframe-eventsource', 'iframe-htmlfile',
						                            'xdr-polling', 'xhr-polling', 'iframe-xhr-polling',
						                            'jsonp-polling'
						                          ]
						                        });
						
						stompClient = Stomp.over(cock);
						//stompClient.debug = null
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