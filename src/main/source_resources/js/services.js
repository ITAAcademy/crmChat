'use strict';

/* Services */

var springChatServices = angular.module('springChat.services', []);
springChatServices.factory('ChatSocket', ['$rootScope', function($rootScope) {
    var stompClient;
    var reconnect = 0;

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

            cock.onclose = function(e) {
                if (reconnect++ < 5) {
                    this.init();
                }
            };
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
/*
    $(window).on("beforeunload", function(event) {
        event.preventDefault();
        wrappedSocket.disconnect()
    })*/

    return wrappedSocket;

}]);

springChatServices.service('AskWindow', ['$rootScope', 'ngDialog', '$timeout', '$http', '$injector', function($rootScope, ngDialog, $timeout, $http, $injector) {
    var hideAskTenantToTakeConsultation_tenantNotRespond;
    var tenantInviteDialog;
    var yesLink;
    var noLink;

    this.setLinks = function(yesLinkArg, noLinkArg) {
        if (typeof yesLinkArg === "undefined") return;
        yesLink = yesLinkArg;
        if (typeof noLinkArg === "undefined") return;
        noLink = noLinkArg;
    };
    this.setAskObject = function(obj) {
        $rootScope.askObject = obj;
        this.setLinks(obj.yesLink, obj.noLink);
    }


    this.showAskWindow = function() {
        var UserFactory = $injector.get('UserFactory');
        tenantInviteDialog = ngDialog.open({
            template: 'askTenantToTakeConsultationWindow.html'
                /*,
                            height: 400*/
        });
        UserFactory.setTenantBusy();

        $timeout.cancel(hideAskTenantToTakeConsultation_tenantNotRespond);
        hideAskTenantToTakeConsultation_tenantNotRespond =
            $timeout(function() {
                if (tenantInviteDialog != null)
                    tenantInviteDialog.close();
            }, TIME_FOR_WAITING_ANSWER_FROM_TENANT);
    };

    function performeResponse(data) {
        debugger;
        if (parseInt(data) != NaN) {
            if (data == -1)
                ngDialog.open({
                    template: '<div style="    padding: 30px; text-align: center;">Уппс щось пішло не так!!! Приносимо наші вибачення.</div>',
                    plain: true
                });
        } else {
            ngDialog.open({
                template: '<div style="    padding: 30px; text-align: center;">' + data + '</div>',
                plain: true
            });
        }
    }

    function response(data) {
        return data;
    }

    $rootScope.answerToTakeConsultation = function(value) {
        $timeout.cancel(this.hideAskTenantToTakeConsultation_tenantNotRespond);
        tenantInviteDialog.close();
        if (value) {
            $http.post(serverPrefix + yesLink, {}, { transformResponse: response }). //$scope.chatUserId)
            success(function(data, status, headers, config) {
                performeResponse(data);
            }).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        } else {
            $http.post(serverPrefix + noLink, {}, { transformResponse: response }).
            success(function(data, status, headers, config) {
                performeResponse(data);
            }).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        }
    };

}]);
