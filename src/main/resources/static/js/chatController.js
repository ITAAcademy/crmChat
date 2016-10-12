springChatControllers.controller('ChatRouteController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {
    angular.extend(this, $controller('ChatRouteInterface', { $scope: $scope }));
    /*
     * 
     */
    $scope.controllerName = "ChatRouteController";
    $scope.tenants = [];
    var chatControllerScope = Scopes.get('ChatController');
    var subscribeBindings = [];
    Scopes.store('ChatRouteController',$scope);

   function updateTenants(tenants){
        $scope.tenants = tenants;
        var itemsToRemove = [];
        if ($scope.tenants!=null){
       for (var i = 0; i <  $scope.tenants.length; i++){
       if($scope.chatUserId== $scope.tenants[i].chatUserId){
        itemsToRemove.push(i);
        continue;
       }
       //Uncomment if you wan't to hide tenants, which present in room
       /*for (var j = 0; j <  $scope.participants.length; j++){
        if ($scope.tenants[i].chatUserId==$scope.participants[j].chatUserId){
        itemsToRemove.push(i);
        continue;
        }
       }
       */
    }
    for (var k = itemsToRemove.length -1; k >= 0; k--)
   $scope.tenants.splice(itemsToRemove[k],1);
    }
    }

   function initSocketsSubscribes(){
        console.log('initSocketsSubscribes');
               subscribeBindings.push(chatSocket.subscribe("/topic/chat.tenants.remove", function(message) {
                var tenant = JSON.parse(message.body);
                for (var i = 0; i < $scope.tenants.length; i++){
                    if (tenant.chatUserId==$scope.tenants[i].chatUserId){
                       $scope.tenants.splice(i,1); 
                       break;
                    }
                   
                }
                //updateTenants(o);
            }));
                subscribeBindings.push(chatSocket.subscribe("/app/chat.tenants", function(message) {
                var o = JSON.parse(message.body);
                $updateTenants(o);
            }));
          subscribeBindings.push(chatSocket.subscribe("/topic/chat.tenants.add", function(message) {
                var tenant = JSON.parse(message.body);
                var alreadyExcist = false;
                  for (var i = 0; i < $scope.tenants.length; i++){
                    if (tenant.chatUserId==$scope.tenants[i].chatUserId){
                       alreadyExcist = true;
                       break;
                    }   
                }
                if (!alreadyExcist && tenant.chatUserId!=$scope.chatUserId) $scope.tenants.push(tenant);
               // updateTenants(o);
            }));
    }

    $rootScope.$watch('isInited', function() {
        console.log("try " + chatControllerScope.currentRoom);
        if ($rootScope.isInited == true) {
            updateTenants(chatControllerScope.tenants);
            var room = getRoomById($scope.rooms, $routeParams.roomId);

            if (room != null && room.type == 2 && $scope.controllerName != "ConsultationController") //redirect to consultation
            {
                $http.post(serverPrefix + "/chat/consultation/fromRoom/" + room.roomId)
                    .success(function(data, status, headers, config) {
                        if (data == "" || data == undefined)
                            $rootScope.goToAuthorize(); //not found => go out
                        else
                            $location.path("consultation_view/" + data);
                    }).error(function errorHandler(data, status, headers, config) {
                        $rootScope.goToAuthorize(); //not found => go out
                    });
                return;
            }

            $scope.goToDialog($routeParams.roomId).then(function(data) {
                if ($rootScope.socketSupport) {
                        initSocketsSubscribes();
                }
                if (data != undefined && data != null) {
                    chatControllerScope.currentRoom = data.data;
                    $scope.dialogName = chatControllerScope.currentRoom.string;
                }
                $scope.pageClass = 'scale-fade-in';
            }, function() {
                $rootScope.goToAuthorize();
                toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                // location.reload();
            });

            $http.post(serverPrefix + "/bot_operations/tenant/did_am_wait_tenant/{0}".format(chatControllerScope.currentRoom.roomId)).
            success(function(data, status, headers, config) {
                if (data == true)
                    $scope.showToasterWaitFreeTenant();
            }).
            error(function(data, status, headers, config) {
                alert("did_am_wait_tenant: server error")
            });

        }

    });
}]);
