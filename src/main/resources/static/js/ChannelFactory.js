springChatServices.factory('ChannelFactory', ['$rootScope', '$timeout', '$location', '$http', 'toaster', 'ChatSocket', function($rootScope, $timeout, $location, $http, toaster, chatSocket) {
    var socketSupport = true;
    var isInited = false;
    var getIsInited = function(){
        return isInited;
    }
    var isInitedCallback;
    var setIsInitedCallback = function(callback){
        isInitedCallback = callback;
    }
    var setIsInited = function(val){
        var initialized = isInited==false && val==true ;
        isInited = val;
        if (initialized && isInitedCallback!=null ) isInitedCallback();

    }
    return {
        isSocketSupport: function() {
            return socketSupport;
        },
        changeLocation: function(url) {
            $location.path(url);
            console.log("Change location:" + $location.path());
            /*   $rootScope.goToAuthorize(function() {
                   changeLocation("/chatrooms");
               });*/
        },
        subscribeToConnect: function(callBack){
            if (isInited == false) {
                console.log("serverPrefix");
                var onConnect = function(frame) {
                    callBack(socketSupport, frame)
                };
                chatSocket.init(serverPrefix + "/wss"); //9999
                chatSocket.connect(onConnect, function(error) {
                    /***************************************
                     * TRY LONG POLING LOGIN
                     **************************************/
                    if (isInited == false) {
                        socketSupport = false;
                        callBack(socketSupport, {})
                    }
                });
            } else {
                toaster.pop('error', 'Error', 'Websocket not supportet or server not exist' + error, 99999);
                changeLocation("/");
            }
        },
        getIsInited : getIsInited,
        setIsInited : setIsInited,
        setIsInitedCallback : setIsInitedCallback
    };

}]);
