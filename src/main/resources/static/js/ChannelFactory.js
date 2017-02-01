'use strict';

springChatServices.factory('ChannelFactory', ['$rootScope', '$timeout', '$location', '$http', 'toaster', 'ChatSocket', function ($rootScope, $timeout, $location, $http, toaster, chatSocket) {
    var socketSupport = true;
    var isInited = false;
    var getIsInited = function getIsInited() {
        return isInited;
    };
    var isInitedCallback;
    var setIsInitedCallback = function setIsInitedCallback(callback) {
        isInitedCallback = callback;
    };
    var setIsInited = function setIsInited(val) {
        var initialized = isInited == false && val == true;
        isInited = val;
        if (initialized && isInitedCallback != null) isInitedCallback();
    };
    return {
        isSocketSupport: function isSocketSupport() {
            return socketSupport;
        },
        changeLocation: function changeLocation(url) {
            $location.path(url);
            console.log("Change location:" + $location.path());
            /*   $rootScope.goToAuthorize(function() {
                   changeLocation("/chatrooms");
               });*/
        },
        subscribeToConnect: function subscribeToConnect(callBack) {
            if (isInited == false) {
                console.log("serverPrefix");
                var onConnect = function onConnect(frame) {
                    callBack(socketSupport, frame);
                };
                chatSocket.init(serverPrefix + "/wss"); //9999
                chatSocket.connect(onConnect, function (error) {
                    /***************************************
                     * TRY LONG POLING LOGIN
                     **************************************/
                    if (isInited == false) {
                        socketSupport = false;
                        callBack(socketSupport, {});
                    }
                });
            } else {
                toaster.pop('error', 'Error', 'Websocket not supportet or server not exist' + error, 99999);
                changeLocation("/");
            }
        },
        getIsInited: getIsInited,
        setIsInited: setIsInited,
        setIsInitedCallback: setIsInitedCallback
    };
}]);