springChatServices.factory('RoomsFactory', ['$injector', '$route', '$routeParams', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', '$q', '$sce', '$rootScope', 'ChannelFactory', function($injector, $route, $routeParams, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, $q, $sce, $rootScope, ChannelFactory) {
    var UserFactory = $injector.get('UserFactory');
    var dialogAdding = false;
    var addRoomWithBot = function(roomName) {
        $http.post(serverPrefix + "/chat/rooms/create/with_bot/", roomName)
        success(function(data, status, headers, config) {}).
        error(function(data, status, headers, config) {});

    };
    var addDialog = function(dialogName, users) {
        if (dialogAdding) {
            var deferred = $q.defer();
            deferred.reject(null);
            return deferred.promise;
        }
        dialogAdding = true;
        return $http.post(serverPrefix + "/chat/rooms/add?name=" + encodeURIComponent(dialogName), users).
        success(function(data, status, headers, config) {
            dialogAdding = false;
        }).
        error(function(data, status, headers, config) {
            dialogAdding = false;
            toaster.pop('error', "Error", "server request timeout", 1000);
        });
    };
    var errorMsgTitleNotFound = "Сталася помилка";
    var errorMsgContentNotFound = "Кімната не існує або Ви не є її учасником";
    var lastRoomBindings = [];
    var currentRoom = null;
    var messages = [];
    var participants = [];
    var roomType = -1;
    var ajaxRequestsForRoomLP = [];
    var userAddedToRoom = true;
    var lastNonUserActivity = false;
    $rootScope.message_busy = true;
    $rootScope.participant_busy = true;
    var likedMessagesIds = [];
    var dislikedMessagesIds = [];
    var rooms = [];
    var oldMessage;


    function isMessageLiked(messageId) {
        return likedMessagesIds.indexOf(messageId) != -1;
    }

    function isMessageDisliked(messageId) {
        return dislikedMessagesIds.indexOf(messageId) != -1;
    }

    function getWhoLikesByMessage(message, page) {
        return $http.get(serverPrefix + "/chat/like_message/" + message.id, page ? {'page' : page} : {})
    }
    function getWhoDisLikesByMessage(message, page) {
        return $http.get(serverPrefix + "/chat/dislike_message/" + message.id, page ? {'page' : page} : {})
    }

    function likeMessage(message) {
        if (isMessageLiked(message.id)) {
            discardMessageLike(message);
            return;
        }
        var messageId = message.id;
        console.log('like message: ' + messageId);
        $http.post(serverPrefix + "/chat/like_message/" + messageId).then(function success(response) {
                message.likes += 1;
                if (isMessageDisliked(message.id)) {
                    message.dislikes = message.dislikes == 0 ? 0 : message.dislikes - 1;
                }
                console.log('is like success:');
                var unlikedIndex = dislikedMessagesIds.indexOf(messageId);
                if (unlikedIndex != -1) {
                    dislikedMessagesIds.splice(unlikedIndex, 1);
                }
                likedMessagesIds.push(messageId);
            },
            function fail(response) {

            });

    }

    function dislikeMessage(message) {
        if (isMessageDisliked(message.id)) {
            discardMessageLike(message);
            return;
        }
        var messageId = message.id;
        console.log('like message: ' + messageId);
        $http.post(serverPrefix + "/chat/dislike_message/" + messageId).then(function success(response) {
                message.dislikes += 1;
                if (isMessageLiked(message.id)) {
                    message.likes = message.likes == 0 ? 0 : message.likes - 1;
                }
                console.log('is unlike success');
                var likedIndex = likedMessagesIds.indexOf(messageId);
                if (likedIndex != -1) {
                    likedMessagesIds.splice(likedIndex, 1);
                }
                dislikedMessagesIds.push(messageId);
            },
            function fail(response) {

            });

    }

    function discardMessageLike(message) {
        if (!isMessageDisliked(message.id) && !isMessageLiked(message.id)) {
            return;
        }
        var messageId = message.id;
        console.log('discard like message: ' + messageId);
        $http.post(serverPrefix + "/chat/discard_like_message/" + messageId).then(function success(response) {
                if (isMessageDisliked(message.id)) {
                    message.dislikes = message.dislikes == 0 ? 0 : message.dislikes - 1;
                    var dislikedIndex = dislikedMessagesIds.indexOf(messageId);
                    if (dislikedIndex != -1) {
                        dislikedMessagesIds.splice(dislikedIndex, 1);
                    }
                } else {
                    message.likes = message.likes == 0 ? 0 : message.likes - 1;
                    var likedIndex = likedMessagesIds.indexOf(messageId);
                    if (likedIndex != -1) {
                        likedMessagesIds.splice(likedIndex, 1);
                    }
                }

            },
            function fail(response) {

            });

    }


    function isRoomPrivate(room) {
        if (room != null && room.type === 1) return true;
        return false;
    }

    function isRoomGroup(room) {
        if (room != null && room.type === 4) return true;
        return false;
    }

    function isRoomConsultation(room) {
        if (room != null && room.type === 2) return true;
        return false;
    }

    /*var updateContactsMapFromArray = function(contactsList) {
        for (var key in Object.keys(roomsMap))
            delete roomsMap[key];
        for (var i = 0; i < contactsList.length; i++) {
            var contact = contactsList[i];
            var characterForGroup = contact.string.charAt(0);
            if (roomsMap[characterForGroup] == null) {
                roomsMap[characterForGroup] = [];
            }
            roomsMap[characterForGroup].push(contact);
        }
    }*/

    var findParticipant = function(nickname) {
        for (var c_index in participants)
            if (participants[c_index].username == nickname)
                return participants[c_index];
        return null;
    }
    var unsubscribeWatch = null;

    var goToRoom = function(roomId) {
        /* if (!ChannelFactory.getIsInited()) {
             unsubscribeWatch = $rootScope.$watch('getIsInited', function(newValue, oldValue) {
                 if (newValue == true) {
                     goToRoom(roomId);
                 }
             });
             return;
         } else {
             if (unsubscribeWatch != undefined)
                 unsubscribeWatch();
         }*/

        if (currentRoom !== undefined && getRoomById(rooms, currentRoom) !== undefined)
            getRoomById(rooms, currentRoom.roomId).date = curentDateInJavaFromat();
        var evn = goToRoomEvn(roomId);
        //setFocus
        if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) == false) {
            setTimeout(function() {
                $(".transparent_input.message_input").click();
            }, 100);
        }

        return evn;
    };

    var goToRoomById = function(roomId) {
        return goToRoomEvn(roomId).then(function() {
            if (currentRoom !== undefined && getRoomById(rooms, currentRoom) !== undefined)
                getRoomById(rooms, currentRoom.roomId).date = curentDateInJavaFromat();
        });

    };

    function goToRoomEvn(id) {
        currentRoom = { roomId: id };
        changeRoom();
        var deferred = $q.defer();
        var room = getRoomById(rooms, id);
        if (room != undefined) {
            currentRoom = room;
            if (currentRoom != undefined) {
                updateNewMsgNumber(-currentRoom.nums);
                currentRoom.nums = 0;
                //push up previesly room
                //currentRoom.date = curentDateInJavaFromat();
            }
            /*updateNewMsgNumber(-currentRoom.nums);
            currentRoom.nums = 0;*/
        } else {
            deferred.reject();
            return deferred.promise;
        }

        return updateLastActivity();
    }

    function updateLastActivity() {
        if (currentRoom == undefined || currentRoom == '')
            return;

        return $http.post(serverPrefix + "/chat.go.to.dialog/{0}".format(currentRoom.roomId));
    }

    var clearHistory = function() {
        $http.post(serverPrefix + "/chat/room/{0}/clear_history".format(currentRoom.roomId), "");
    }

    var NEXT_MESSAGE_TIME_LIMIT_SECONDS = 60;

    function calcPositionUnshift(msg) {
        if (msg == null)
            return null;
        //     msg.message = msg.message.escapeHtml();//WRAP HTML CODE
        var summarised = false;
        oldMessage = msg;

        if (messages.length > 0) {
            var isActual = differenceInSecondsBetweenDates(new Date(msg.date), new Date(messages[0].date)) < NEXT_MESSAGE_TIME_LIMIT_SECONDS;
            if (messages[0].author.id == msg.author.id) {
                if (isActual && msg.attachedFiles.length == 0) {
                    summarised = true;
                    //messages[0].body = msg.body + "\n\n" + messages[0].body;
                    if(messages[0].olderMessages==null) messages[0].olderMessages = [];
                    messages[0].olderMessages.push(msg);
                    //  $scope.messages[0].date = msg.date;
                }
                msg.position = messages[0].position;

            } else {
                msg.position = !messages[0].position;
            }
        } else {
            msg.position = false;
        }

        if (summarised == false)
            messages.unshift(msg);
        $rootScope.$$postDigest(function() {
            var objDiv = document.getElementById("messagesScroll");
            objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
        });
    }

    function calcPositionPush(msg) {

        if (msg == null)
            return null;
        // msg.message = msg.message.escapeHtml();//WRAP HTML CODE

        var objDiv = document.getElementById("messagesScroll");
        var needScrollDown = Math.round(objDiv.scrollTop + objDiv.clientHeight) >= objDiv.scrollHeight - 100;
        /*if (needScrollDown) {
            $rootScope.$broadcast('MessageAreaScrollDownEvent');
        }*/
        if (messages.length > 0) {
            if (messages[messages.length - 1].author.id == msg.author.id)
                msg.position = messages[messages.length - 1].position;
            else
                msg.position = !messages[messages.length - 1].position;
        } else
            msg.position = false;


        if (messages.length > 0) {
            var isActual = differenceInSecondsBetweenDates(new Date(msg.date), new Date(messages[messages.length - 1].date)) < NEXT_MESSAGE_TIME_LIMIT_SECONDS;
            if (isActual && messages[messages.length - 1].author.id == msg.author.id && msg.attachedFiles.length == 0 && messages[messages.length - 1].attachedFiles.length == 0) {
                messages[messages.length - 1].date = msg.date;
                //messages[messages.length - 1].body += "\n\n" + msg.body;
                if(messages[messages.length - 1].newerMessages==null) messages[messages.length - 1].newerMessages = [];
                    messages[messages.length - 1].newerMessages.push(msg);
            } else {
                messages.push(msg);
            }
        } else {
            messages.push(msg);
        }
        $rootScope.$$postDigest(function() {
            var objDiv = document.getElementById("messagesScroll");
            if (needScrollDown)
                objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
        });

    }

    function removeOrSwapInternalMessage(arr,indexToRemove) {
            var msgToRemove = arr[indexToRemove];
          if ( msgToRemove.olderMessages == null && msgToRemove.newerMessages == null ){
                  arr.splice(indexToRemove,1);
                  return;
            }



            var olderArr = msgToRemove.olderMessages || [];
            var newestArr = msgToRemove.newerMessages || [];

            var totalArr = olderArr.concat(newestArr);

            var earliestMessageIndex = -1;
            var oldestTime = new Date();
            for (var i = 0; i < totalArr.length; i++) {
                if (totalArr[i].date < oldestTime) {
                    earliestMessageIndex = i;
                    oldestTime = totalArr[i].date;
                }
            }
            if(earliestMessageIndex == -1) {
                return;
            }
            var earliestMessage = totalArr[earliestMessageIndex];
            totalArr.splice(earliestMessageIndex,1);
            earliestMessage.newerMessages =  totalArr;

            arr[indexToRemove] = earliestMessage;
    }

   /* function removeMessageById(idToRemove,arr,deep) {
        if (deep == null) deep = true;
        if (arr == null) return;
        var findedIndex = -1;
        var removedInternally = false;
        for (var i = 0; i < arr.length; i++) {
            var msg = arr[i];
            if (msg.id == idToRemove) {
                findedIndex = i;
                break;
            }
            if (deep == false) continue;
            if (removeMessageById(idToRemove,msg.olderMessages,false) ||
                removeMessageById(idToRemove,msg.newerMessages,false) ) {
                    removedInternally = true;
           }
        }
        if ( findedIndex != -1 ){
        removeOrSwapInternalMessage(arr,findedIndex);       
        return true;
        }
        else {
            if (deep && !removedInternally) {
                console.log('Message with id ' + idToRemove + 'not founded' );
            }
        }
        return false;
    }
    */

    function removeMessageById(idToRemove,arr,deep) {
        if (deep == null) deep = true;
        if (arr == null) return;
        var findedIndex = -1;
        var removedInternally = false;
        for (var i = 0; i < arr.length; i++) {
            var msg = arr[i];
            if (msg.id == idToRemove) {
                findedIndex = i;
                break;
            }
            if (deep == false) continue;
            if (removeMessageById(idToRemove,msg.olderMessages,false) ||
                removeMessageById(idToRemove,msg.newerMessages,false) ) {
                    removedInternally = true;
           }
        }
        if ( findedIndex != -1 ){
        arr[findedIndex].active = false;      
        return true;
        }
        else {
            if (deep && !removedInternally) {
                console.log('Message with id ' + idToRemove + 'not founded' );
            }
        }
        return false;
    }
    function changeMessageById(newMessage, arr,deep  ) {
         if (deep == null) deep = true;
        if (arr == null) return;
        var findedIndex = -1;
        var changedInternally = false;
        for (var i = 0; i < arr.length; i++) {
            var msg = arr[i];
            if (msg.id == newMessage.id) {
                findedIndex = i;
                break;
            }
            if (deep == false) continue;
            if (changeMessageById(newMessage,msg.olderMessages,false) ||
                changeMessageById(newMessage,msg.newerMessages,false) ) {
                    changedInternally = true;
           }
        }
        if ( findedIndex != -1 ){
        var msgToChange = arr[findedIndex];
        msgToChange.body = newMessage.body;
        msgToChange.attachedFiles = newMessage.newMessage || [];
        return true;
        }
        else {
            if (deep && !changedInternally) {
                console.log('Message with id ' + newMessage.id + 'not founded' );
            }
        }
        return false;
    }

    /*************************************
     * CHANGE ROOM
     *************************************/
    var changeRoom = function() {
        //alert(16);
        $rootScope.loadingSubscribesAndMessages = true;
        messages = [];
        // $rootScope.message_busy = false;
        $rootScope.$broadcast('RoomChanged', false);


        if (ChannelFactory.isSocketSupport() === true) {
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/{0}/chat.message".format(currentRoom.roomId), function(message) {
                    calcPositionPush(JSON.parse(message.body)); //POP
                }));
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/{0}/chat.message.removed".format(currentRoom.roomId), function(message) {
                   var msgObj = JSON.parse(message.body);
                   removeMessageById(msgObj.id,messages); //POP
                }));
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/{0}/chat.message.changed".format(currentRoom.roomId), function(message) {
                   var msgObj = JSON.parse(message.body);
                   //TODO create changeMessageByIdFunction
                   changeMessageById(msgObj,messages); //POP
                }));
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/chat/rooms/{0}/remove_user/{1}".format(currentRoom.roomId, UserFactory.getChatUserId()), function(message) {
                    if (!UserFactory.isAdmin())
                        unsubscribeCurrentRoom();
                }));

            lastRoomBindings.push(chatSocket.subscribe("/app/{0}/chat.participants/{1}".format(currentRoom.roomId, globalConfig.lang), function(message) {
                if (message.body != "{}") {
                    var o = JSON.parse(message.body);
                    loadSubscribeAndMessage(o);
                } else {
                    $rootScope.goToAuthorize();
                    return;
                }
            }));

            lastRoomBindings.push(chatSocket.subscribe("/topic/{0}/chat.participants".format(currentRoom.roomId, globalConfig.lang), function(message) {
                var o = JSON.parse(message.body);
                participants = o["participants"];
            }));
        } else {
            subscribeMessageLP(); //@LP@
            subscribeParticipantsLP();
            loadSubscribeAndMessageLP();
        }

        lastRoomBindings.push(
            chatSocket.subscribe("/topic/{0}/chat.typing".format(currentRoom.roomId), function(message) {
                var parsed = JSON.parse(message.body);
                if (parsed.username == UserFactory.getChatUserId()) return;

                for (var index in participants) {
                    var participant = participants[index];

                    if (participant.chatUserId == parsed.username) {
                        participants[index].typing = parsed.typing;
                    }
                }
            }));
        //chatSocket.send("/topic/{0}chat.participants".format(room), {}, JSON.stringify({}));
        $rootScope.message_busy = false;
        $rootScope.participant_busy = false;
    }
    var addTenantToRoom = function(id) {
        $http.post(serverPrefix + "/bot_operations/tenant/{0}/askToAddToRoom/{1}".format(id, currentRoom.roomId), {}).
        success(function(data, status, headers, config) {
            userAddedToRoom = true;
        }).
        error(function(data, status, headers, config) {
            userAddedToRoom = true;
        });
    }

    var addTenantsToRoom = function(tenantsIdList, msg, success, error) {
        $http.post(serverPrefix + "/bot_operations/tenants/askToAddToRoom/{0}".format(currentRoom.roomId), { 'msg': msg, 'tenantsIdList': tenantsIdList }).
        success(function(data, status, headers, config) {
            userAddedToRoom = true;
            if (success != undefined)
                success(data);
        }).
        error(function(data, status, headers, config) {
            userAddedToRoom = true;
            if (error != undefined)
                error(data);
        });
    }


    var addUserToRoom = function(chatUserId) {
        $http.post(serverPrefix + "/chat/rooms.{0}/user/add?chatId={1}".format(currentRoom.roomId, chatUserId), {}).
        success(function(data, status, headers, config) {

        }).
        error(function(data, status, headers, config) {

        });
    }

    var addUsersToRoom = function(usersIds) {

        if (usersIds == null || usersIds.length < 1) {
            return;
        }
        $http.post(serverPrefix + "/chat/rooms.{0}/user/add_all".format(currentRoom.roomId), usersIds).
        success(function(data, status, headers, config) {

        }).
        error(function(data, status, headers, config) {

        });
    }

    var removeUserFromRoom = function(userId) {
        $http.post(serverPrefix + "/chat/rooms.{0}/user.remove/{1}".format(currentRoom.roomId, userId), {}).
        success(function(data, status, headers, config) {
            if (data == false) {
                toaster.pop('error', "Error", "Cant remove user from the room", 1000);
                return;
            }
        }).
        error(function(data, status, headers, config) {
            toaster.pop('error', "Error", "Cant remove user from the room", 1000);
        });
    }
    /*************************************
     * UPDATE MESSAGE LP
     *************************************/
    var subscribeMessageLP = function() {
        var currentUrl = serverPrefix + "/{0}/chat/message/update".format(currentRoom.roomId);
        ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                mimeType: "text/html; charset=UTF-8",
                url: currentUrl,
                success: function(data) {
                    var parsedData = JSON.parse(data);
                    for (var index = 0; index < parsedData.length; index++) {
                        if (parsedData[index].hasOwnProperty("message")) {
                            calcPositionPush(parsedData[index]); //POP
                        }
                    }
                    subscribeMessageLP();
                },
                error: function(xhr, text_status, error_thrown) {
                    //if (text_status == "abort")return;

                    if (xhr.status === 0 || xhr.readyState === 0) {
                        //alert("discardMsg");
                        return;
                    }
                    if (xhr.status === 404 || xhr.status === 405) {
                        //alert("13")
                        ChannelFactory.changeLocation("/chatrooms");
                        toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                    }
                    subscribeMessageLP();
                }
            }));
    }

    var subscribeParticipantsLP = function() {
        var currentUrl = serverPrefix + "/{0}/chat/participants/update".format(currentRoom.roomId)
        ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                url: currentUrl,
                mimeType: "text/html; charset=UTF-8",
                success: function(data) {
                    subscribeParticipantsLP();
                    var parsedData = JSON.parse(data);
                    if (parsedData.hasOwnProperty("participants"))
                        participants = parsedData["participants"];
                },
                error: function(xhr, text_status, error_thrown) {
                    if (xhr.status === 0 || xhr.readyState === 0) return;
                    if (xhr.status === 404 || xhr.status === 405) {
                        //alert(14)
                        ChannelFactory.changeLocation("/chatrooms");
                        toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                    }
                    //subscribeParticipantsLP();
                }
            }));
    };
    var changeCurrentRoomName = function(name) {
        return changeRoomName(currentRoom.roomId, name);
    }
    var showChangeRoomNameErr = function() {
        toaster.pop('warning', "Сталася помилка", "помилка зміни імені кімнати", 5000);
    }
    var changeRoomName = function(roomId, name) {
        var currentUrl = serverPrefix + "/{0}/chat/set_name".format(roomId)
        ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                url: currentUrl,
                mimeType: "text/plain; charset=UTF-8",
                data: { 'newName': name },
                success: function(data) {
                    $rootScope.$apply(function() {
                        if (data != null)
                            currentRoom.string = data;
                        else
                            showChangeRoomNameErr();
                    })

                },
                error: function(xhr, text_status, error_thrown) {
                    if (xhr.status === 0 || xhr.readyState === 0) return;
                    if (xhr.status === 404 || xhr.status === 405) {
                        //alert(14)

                    }
                    showChangeRoomNameErr();
                    //subscribeParticipantsLP();
                }
            }));
    };


    /*************************************
     * SEND MESSAGE
     *************************************/

    /*************************************
     * LOAD MESSAGE LP
     *************************************/

    function loadSubscribesOnly(message) {
        participants = message["participants"];
        roomType = message["type"];
    }
    $rootScope.loadingSubscribesAndMessages = true;

    function loadMessagesOnly(message) {
        roomType = message["type"];
        for (var i = 0; i < message["messages"].length; i++) {
            calcPositionPush(message["messages"][i]); //POP
            //calcPositionUnshift(JSON.parse(o["messages"][i].text));
        }
    }

    function loadMessagesContains(searchQuery) {
        // /chat/room/{roomId}/get_messages_contains
        $rootScope.message_busy = true;
        if (currentRoom == null) {
            console.warn('loadMessagesContains failed duing to current room is not selected');
            return;
        }
        var deffered = $http.post(serverPrefix + "/chat/room/{0}/get_messages_contains".format(currentRoom.roomId), searchQuery);
        deffered.success(function(data, status, headers, config) {
            loadMessagesFromArrayList(data);
            $rootScope.message_busy = false;
        }).
        error(function(data, status, headers, config) {

        });
        return deffered;
    }


     function loadOtherMessages(filesOnly,bookmarkedOnly,searchQuery) {
            if ($rootScope.message_busy || currentRoom == null)
                return;
            var urlTemplate = "/{0}/chat/loadOtherMessage";
            var params = {
                filesOnly: filesOnly,
                bookmarkedOnly: bookmarkedOnly
            }
            var paramsPartOfRequest = encodeQueryData(params);
            if (paramsPartOfRequest.length > 0){
               urlTemplate += '?'+paramsPartOfRequest;
            }
            $rootScope.message_busy = true;
            var date = (oldMessage == null) ? null : oldMessage.date;
            var payload = { 'date': date };
            if (searchQuery != null)
                payload['searchQuery'] = searchQuery;
            var promise = 
            $http.post(serverPrefix + urlTemplate.format(currentRoom.roomId), payload);
            promise.then(function(){
                $rootScope.message_busy = false;
            }); //  messages[0]). //
            return promise;
        }



    function loadMessagesFromArrayList(list) {
        oldMessage = list[list.length - 1];
        for (var index = 0; index < list.length; index++) {
            if (list[index].hasOwnProperty("message")) {
                calcPositionUnshift(list[index]);
            }
        }
    }

    function clearMessages() {
        messages = [];
        resetOldMessageInfo();
    }

    $rootScope.$on('roomRead', function(event, data) {
        if (currentRoom.roomId == data.roomId) {
            var UserFactory = $injector.get('UserFactory');
            if (UserFactory != undefined && UserFactory.getChatUserId() != data.chatUserId) {
                lastNonUserActivity = new Date();
            }
        }
    });

    function loadSubscribeAndMessage(message) {
        roomType = message["type"];

        participants = message["participants"];
        lastNonUserActivity = message["lastNonUserActivity"];

        likedMessagesIds.splice(0, likedMessagesIds.length);
        dislikedMessagesIds.splice(0, dislikedMessagesIds.length);

        likedMessagesIds.push(...message["likedMessagesIds"]);
        dislikedMessagesIds.push(...message["dislikedMessagesIds"]);

        if (typeof message["messages"] != 'undefined') {
            $rootScope.loadingSubscribesAndMessages = true;
            // oldMessage = message["messages"][message["messages"].length - 1];

            for (var i = 0; i < message["messages"].length; i++) {
                calcPositionUnshift(message["messages"][i]);
                //calcPositionUnshift(JSON.parse(o["messages"][i].text));
            }
        }
        var bot_params = JSON.parse(message["bot_param"]);
        if (bot_params.length > 0) {

            for (var key in bot_params)
                botParameters[bot_params[key].name] = JSON.parse(bot_params[key].value);
        }
        // $timeout(function () {

        //  },2000);  

        $rootScope.$$postDigest(function() {

            $('#messagesScroll').stop(true).animate({
                scrollTop: 999999
            }, 500);

        });
        $timeout(function() {
            $rootScope.loadingSubscribesAndMessages = false;
        });


    }

    function loadSubscribeAndMessageLP() {
        $http.post(serverPrefix + "/{0}/chat/participants_and_messages".format(currentRoom.roomId), {}).
        success(function(data, status, headers, config) {
            loadSubscribeAndMessage(data);
        }).
        error(function(data, status, headers, config) {

        });
    }

    var checkUserAdditionPermission = function(chatUserId) {
        if (currentRoom == undefined)
            return false;
        // if (isRoomGroup(currentRoom)) return false;
        var needPrivilege = USER_COPABILITIES_BY_ROOM.ADD_USER | USER_COPABILITIES_BY_ROOM.REMOVE_USER;
        var havePermitions = chatUserId == currentRoom.roomAuthorId;
        havePermitions = havePermitions || (currentRoom.userPermissions & needPrivilege) == needPrivilege
        if (typeof currentRoom === "undefined") return false;
        var resultOfChecking = currentRoom.active /*&& ($scope.roomType != 1)*/ && havePermitions && $rootScope.isMyRoom && $rootScope.authorize;
        return resultOfChecking;
    }
    var checkMessageAdditionPermission = function() {
        if (currentRoom == undefined)
            return false;
        if (typeof currentRoom === "undefined") return false;
        var resultOfChecking = currentRoom.active && $rootScope.isMyRoom;
        return resultOfChecking;
    }

    var unsubscribeCurrentRoom = function(event) {
        var isLastRoomBindingsEmpty = lastRoomBindings == undefined || lastRoomBindings.length == 0;
        if (!isLastRoomBindingsEmpty) {
            while (lastRoomBindings.length > 0) {
                var subscription = lastRoomBindings.pop();
                //if (subscription!=undefined)
                subscription.unsubscribe();
            }
        }
        while (ajaxRequestsForRoomLP.length > 0) {
            var subscription = ajaxRequestsForRoomLP.pop();
            subscription.abort();
        }
        /* var answer = confirm("Are you sure you want to leave this page?")
        if (!answer) {
            event.preventDefault();
        }*/
    }
    var subscribeBindings = [];

    // $watch('isInited', function() {
    ChannelFactory.setIsInitedCallback(afterIsInited);

    function afterIsInited() {
        if (ChannelFactory.getIsInited() == true) {

            var room = getRoomById(rooms, $routeParams.roomId);

            if (room != null && isRoomConsultation(room)) //redirect to consultation
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

            if ($routeParams.roomId == null) return;
            goToRoom($routeParams.roomId).then(function(data) {
                /* if (data != undefined && data != null) {
                     currentRoom = data.data;
                 }*/
            }, function() {
                $rootScope.goToAuthorize();
                toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                // location.reload();
            });
            $http.post(serverPrefix + "/bot_operations/tenant/did_am_wait_tenant/{0}".format(currentRoom.roomId)).
            success(function(data, status, headers, config) {
                if (data == true)
                    showToasterWaitFreeTenant();
            }).
            error(function(data, status, headers, config) {
                //  alert("did_am_wait_tenant: server error")
            });

        }

    }

    var updateRooms = function(message) {
        var parseObj;
        if (ChannelFactory.isSocketSupport() === true) {
            parseObj = JSON.parse(message.body);
        } else {
            parseObj = message;
        }

        var needReplace = parseObj.replace;
        var roomList = parseObj.list;
        if (needReplace) {
            rooms = roomList;
        } else {
            for (var i = 0; i < rooms.length; i++) {
                for (var j = roomList.length - 1; j >= 0; j--) {
                    if (roomList[j].roomId == rooms[i].roomId) {
                        rooms[i] = roomList[j];
                        roomList.splice(j, 1);
                    }
                }
            }
        }
        if (currentRoom != null) {
            currentRoom = getRoomById(rooms, currentRoom.roomId);
        }
        updateNewMsgNumber();
    }

    function subscribeRoomsUpdateLP() {
        $http.post(serverPrefix + "/chat/rooms/user/login")
            .success(function(data, status, headers, config) {
                updateRooms(data);
                subscribeRoomsUpdateLP();
            }).error(function errorHandler(data, status, headers, config) {
                //$scope.topics = ["error"];
                subscribeRoomsUpdateLP();
            });
    }

    var goToPrivateDialogErr = function(data, status, headers, config) {
        toaster.pop('error', "PRIVATE ROOM CREATE FAILD", "", 3000);
        console.warn("PRIVATE ROOM CREATE FAILD ");
        $rootScope.goToAuthorize(function() { ChannelFactory.changeLocation("/chatrooms"); });
    }

    function goToPrivateDialog(intitaUserId, isChatId) {
        $rootScope.hideMenu();
        var _isChatId = isChatId === true ? '?isChatId=true' : '';
        $http.post(serverPrefix + "/chat/get/rooms/private/" + intitaUserId + _isChatId).
        success(function(data, status, headers, config) {

            //$scope.goToDialogById(data);
            if (RoomsFactory.getCurrentRoom() == null)
                currentRoom = {};
            if (data != null && data != undefined) {
                //    $scope.currentRoom.roomId = data;
                ChannelFactory.changeLocation("/dialog_view/" + data);
            } else {
                goToPrivateDialogErr();
            }


        }).
        error(goToPrivateDialogErr);
    }

    function resetOldMessageInfo() {
        oldMessage = null;
    }
    var newMsgNumber = 0;
    var updateNewMsgNumber = function(diff) {
        if (diff != undefined && !isNaN(diff)) {
            newMsgNumber += diff;
            return;
        }
        newMsgNumber = 0;
        for (var i = 0; i < rooms.length; i++) {
            newMsgNumber += rooms[i].nums;
        }
    }

    var RoomsFactory = {
        goToRoom: goToRoom,
        unsubscribeCurrentRoom: unsubscribeCurrentRoom,
        updateRooms: updateRooms,
        getCurrentRoom: function() {
            return currentRoom;
        },
        setRooms: function(roomsArg) {
            rooms = roomsArg;
            updateNewMsgNumber();
        },
        getRooms: function() {
            return rooms;
        },
        getMessages: function() {
            return messages;
        },
        getParticipants: function() {
            return participants;
        },
        getOldMessage: function() {
            return oldMessage;
        },
        updateNewMsgNumber: updateNewMsgNumber,
        getNewMsgNumber: function() {
            return newMsgNumber;
        },
        getSkypeContacts: function() {
            var skypes = [];
            for (var participant of participants) {
                if (participant.skype == null || participant.skype.length < 1) continue;
                skypes.push(participant.skype);
            }
            return skypes;
        },
        calcPositionUnshift: calcPositionUnshift,
        checkUserAdditionPermission: checkUserAdditionPermission,
        removeUserFromRoom: removeUserFromRoom,
        subscribeRoomsUpdateLP: subscribeRoomsUpdateLP,
        clearMessages: clearMessages,
        loadMessagesContains: loadMessagesContains,
        goToPrivateDialog: goToPrivateDialog,
        addDialog: addDialog,
        isRoomPrivate: isRoomPrivate,
        isRoomGroup: isRoomGroup,
        isRoomConsultation: isRoomConsultation,
        addTenantToRoom: addTenantToRoom,
        addTenantsToRoom: addTenantsToRoom,
        addUserToRoom: addUserToRoom,
        checkMessageAdditionPermission: checkMessageAdditionPermission,
        changeCurrentRoomName: changeCurrentRoomName,
        clearHistory: clearHistory,
        lastMsgIsReaded: function() {
            return lastNonUserActivity > messages[messages.length - 1].date
        },
        getUserAddedToRoom: function() {
            return userAddedToRoom;
        },
        setUserAddedToRoom: function(val) {
            userAddedToRoom = val;
        },
        containsUserId: function(chatUserId) {
            for (var i = 0; i < participants.length; i++) {
                if (participants[i].chatUserId == chatUserId) return true;
            }
            return false;
        },
        isCurrentRoomPrivate: function() {
            return isRoomPrivate(currentRoom);
        },
        updateLastActivity: updateLastActivity,
        calcPositionPush: calcPositionPush,
        addUsersToRoom: addUsersToRoom,
        isMessageLiked,
        isMessageDisliked,
        likeMessage,
        dislikeMessage,
        getWhoLikesByMessage,
        getWhoDisLikesByMessage,
        loadOtherMessages


    };

    return RoomsFactory;



}]);