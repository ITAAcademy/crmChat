springChatServices.factory('ChannelFactory', ['$rootScope', '$timeout', '$location', '$http', 'toaster', 'ChatSocket', function($rootScope, $timeout, $location, $http, toaster, chatSocket) {
    var socketSupport = true;
    $rootScope.isInited = false;
    return {
        isSocketSupport: function() {
            return socketSupport;
        },
        changeLocation: function(url) {
            $location.path(url);
            console.log("Change location:" + $location.path());
            toaster.pop('error', "PRIVATE ROOM CREATE FAILD", "", 3000);
            console.log("PRIVATE ROOM CREATE FAILD ");
            /*   $rootScope.goToAuthorize(function() {
                   changeLocation("/chatrooms");
               });*/
        },
        subscribeToConnect(callBack) {
            if ($rootScope.isInited == false) {
                console.log("serverPrefix");
                var onConnect = function(frame) {
                    callBack(socketSupport, frame)
                };
                chatSocket.init(serverPrefix + "/wss"); //9999
                chatSocket.connect(onConnect, function(error) {
                    /***************************************
                     * TRY LONG POLING LOGIN
                     **************************************/
                    if ($rootScope.isInited == false) {
                        socketSupport = false;
                        callBack(socketSupport, {})
                    }
                });
            } else {
                toaster.pop('error', 'Error', 'Websocket not supportet or server not exist' + error, 99999);
                changeLocation("/");
            }
        }
    };

}]);
