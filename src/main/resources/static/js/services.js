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
        disconnect: function() {
            stompClient.disconnect();
        },
        connect: function(successCallback, errorCallback) {

            stompClient.connect({}, function(frame) {
                $rootScope.$apply(function() {
                    successCallback(frame);
                });
            }, function(error) {
                $rootScope.$apply(function() {
                    errorCallback(error);
                });
            });
        },
        subscribe: function(destination, callback) {
            var res = stompClient.subscribe(destination, function(message) {
                $rootScope.$apply(function() {
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

springChatServices.service('AskWindow', ['$rootScope','ngDialog','$timeout','$http','$injector', function($rootScope,ngDialog,$timeout,$http,$injector) {
    var hideAskTenantToTakeConsultation_tenantNotRespond;
    var tenantInviteDialog;
    var yesLink;
    var noLink;

    this.setLinks = function(yesLinkArg,noLinkArg){
        if (typeof yesLinkArg === "undefined")return;
        yesLink = yesLinkArg;
        if (typeof noLinkArg === "undefined")return;
        noLink = noLinkArg;
    };
        this.showAskWindow = function() {
            var UserFactory = $injector.get('UserFactory');
         tenantInviteDialog = ngDialog.open({
            template: 'askTenantToTakeConsultationWindow.html',
            height: 400
        });
            UserFactory.setTenantBusy();

            $timeout.cancel(hideAskTenantToTakeConsultation_tenantNotRespond);
            hideAskTenantToTakeConsultation_tenantNotRespond =
                $timeout(function() {
                    if (tenantInviteDialog!=null)
                    tenantInviteDialog.close();
                }, TIME_FOR_WAITING_ANSWER_FROM_TENANT);
    };

    $rootScope.answerToTakeConsultation = function(value) {
        $timeout.cancel(this.hideAskTenantToTakeConsultation_tenantNotRespond);
        tenantInviteDialog.close();
        if (value) {
            $http.post(serverPrefix + yesLink). //$scope.chatUserId)
            success(function(data, status, headers, config) {}).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        } else {
            $http.post(serverPrefix + noLink).
            success(function(data, status, headers, config) {
            }).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        }
    };

}]);
