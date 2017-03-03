/* Directives */

var directivesModule = angular.module('springChat.directives', []);
directivesModule.directive('printMessage', function() {
    return {
        restrict: 'A',
        template: '<span ng-show="message.priv">[private] </span><strong>{{message.username}}<span ng-show="message.to"> -> {{message.to}}</span>:</strong> {{message.message}}<br/>'

    };
});
directivesModule.constant('mySettings', {
    baseUrl: globalConfig.baseUrl
});
var imageDrop = function($parse, $document) {
    return {
        restrict: "A",
        link: function(scope, element, attrs) {
            var onImageDrop = $parse(attrs.onImageDrop);

            //When an item is dragged over the document
            var onDragOver = function(e) {
                e.preventDefault();
                angular.element('body').addClass("dragOver");
            };

            //When the user leaves the window, cancels the drag or drops the item
            var onDragEnd = function(e) {
                e.preventDefault();
                angular.element('body').removeClass("dragOver");
            };

            //When a file is dropped
            var loadFile = function(files) {
                scope.uploadedFiles = files;
                scope.$apply(onImageDrop(scope));
            };

            //Dragging begins on the document
            $document.bind("dragover", onDragOver);

            //Dragging ends on the overlay, which takes the whole window
            element.bind("dragleave", onDragEnd)
                .bind("drop", function(e) {
                    onDragEnd(e);
                    loadFile(e.originalEvent.dataTransfer.files);
                });
        }
    };
};
angular.module('springChat.directives').directive("imagedrop", ['$parse', '$document', imageDrop]);

var autoGrow = function() {
    return function(scope, element, attr) {
        var minHeight = element[0].offsetHeight,
            paddingLeft = element.css('paddingLeft'),
            paddingRight = element.css('paddingRight');

        var $shadow = angular.element('<div></div>').css({
            position: 'absolute',
            top: -10000,
            left: -10000,
            width: element[0].offsetWidth - parseInt(paddingLeft || 0) - parseInt(paddingRight || 0),
            fontSize: element.css('fontSize'),
            fontFamily: element.css('fontFamily'),
            lineHeight: element.css('lineHeight'),
            resize: 'none'
        });
        angular.element(document.body).append($shadow);

        var update = function() {
            var times = function(string, number) {
                for (var i = 0, r = ''; i < number; i++) {
                    r += string;
                }
                return r;
            }

            var val = element.val().replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/&/g, '&amp;')
                .replace(/\n$/, '<br/>&nbsp;')
                .replace(/\n/g, '<br/>')
                .replace(/\s{2,}/g, function(space) {
                    return times('&nbsp;', space.length - 1) + ' '
                });
            $shadow.html(val);

            element.css('height', Math.max($shadow[0].offsetHeight + 10 /* the "threshold" */ , minHeight) + 'px');
        }

        element.bind('keyup keydown keypress change', update);
        update();
    }
};
angular.module('springChat.directives').directive('autoGrow', autoGrow);

var dirFunc = function($compile, $parse) {
    return {
        restrict: 'E',
        link: function(scope, element, attr) {
            scope.$watch(attr.content, function() {
                element.html($parse(attr.content)(scope));
                if (typeof attr.callback != 'undefined') {
                    var callBackFunction = new Function("return " + attr.callback)();
                    if (typeof callBackFunction != 'undefined')
                        callBackFunction(element);
                }
                $compile(element.contents())(scope);
                //element.html =$parse(attr.content)(scope);

            }, true);
        }
    }
};
angular.module('springChat.directives').directive('dir', ['$compile', '$parse', dirFunc])

var modaleToggle = function($compile, $parse, $rootScope) {
    return {
        restrict: 'EA',
        scope: {
            modaleToggleId: '@modaleToggleId',
            callback: '&callback',
            ignoreId: '@ignoreId'
        },
        link: function(scope, element, attr) {
            if (scope.modaleToggleId != undefined) {
                if ($rootScope.__modaleToggle == undefined)
                    $rootScope.__modaleToggle = new Map();
                $rootScope.__modaleToggle[scope.modaleToggleId] = {
                    restart: function() { toggle = false }
                }
            }
            var toggle = false;
            var ignoredList = [];
            if (scope.ignoreId.trim()[0] == '[') {
                try {
                    ignoredList = eval(scope.ignoreId);
                } catch (e) {
                    ignoredList.push(scope.ignoreId);
                }
            } else
                ignoredList.push(scope.ignoreId);

            $(element).click(function(e) {
                e.preventDefault();
                e.stopPropagation();
                scope.$apply(function() {
                    var res = scope.callback();
                    if (res == undefined)
                        toggle = !toggle;
                    else
                        toggle = res;
                });
            });
            if (scope.callback != null) {
                $(window).click(function(e) {
                    if (e.target.style.pointerEvents == "none")
                        return;

                    var ignore = true;
                    for (var i = 0; i < ignoredList.length; i++) {
                        var ignoredElement = document.getElementById(ignoredList[i]);
                        if (ignoredElement == null)
                            continue;
                        ignore = ignore && e.target != ignoredElement && !ignoredElement.contains(e.target);
                    }

                    if (e.target === element[0] || element[0].contains(e.target) || (toggle && ignore)) {
                        e.preventDefault();
                        e.stopPropagation();
                        scope.$apply(function() {
                            var res = scope.callback();
                            if (res == undefined)
                                toggle = !toggle;
                            else
                                toggle = res;
                        });

                    }
                    //alert(e.target === element[0] || element[0].contains(e.target));
                });
            }
        }
    }
};

angular.module('springChat.directives').directive('modaleToggle', ['$compile', '$parse', '$rootScope', modaleToggle]);


angular.module('springChat.directives').directive('starRating', starRating);

function starRating() {
    return {
        restrict: 'EA',
        template: '<ul class="star-rating" ng-class="{readonly: readonly}">' +
            '  <li ng-repeat="star in stars" class="star" ng-class="{filled: star.filled}" ng-click="toggle($index)">' +
            '    <i class="fa fa-star"></i>' + // or &#9733
            '  </li>' +
            '</ul>',
        scope: {
            ratingValue: '=ngModel',
            max: '=?', // optional (default is 5)
            onRatingSelect: '&?',
            readonly: '=?'
        },
        link: function(scope, element, attributes) {

            if (attributes.onratingselect == "angular.noop")
                attributes.onratingselect = function() {};

            if (scope.max == undefined) {
                scope.max = 5;
            }

            function updateStars() {
                scope.stars = [];
                for (var i = 0; i < scope.max; i++) {
                    scope.stars.push({
                        filled: i < scope.ratingValue
                    });
                }
            };
            scope.toggle = function(index) {
                if (scope.readonly == undefined || scope.readonly === false) {
                    scope.ratingValue = index + 1;
                    //
                    attributes.onratingselect({
                        rating: index + 1
                    });
                    updateStars();
                }
            };
            scope.ratingValue = 0;
            /*scope.$watch('ratingValue', function(oldValue, newValue) {
              if (newValue) {
                updateStars();
              }
          });*/
            updateStars();
        }
    };
};

function updateModelGet(http, requestUrl, callback) {
    http({
        method: 'GET',
        url: requestUrl
    }).then(callback, function errorCallback(response) {
        console.warn('updateModelGet():requestUrl:' + requestUrl + " failed");
    });
};


angular.module('springChat.directives').directive('tenantsBlock', ['$rootScope', 'ngDialog', '$http', 'mySettings', 'UserFactory', 'RoomsFactory', function($rootScope, ngDialog, $http, mySettings, UserFactory, RoomsFactory) {
    return {
        restrict: 'EA',
        scope: {

        },
        templateUrl: 'static_templates/tenants_block.html',
        link: function(scope, element, attributes) {
            scope.blockName = lgPack.blockNames.tenants;
            initFolded(scope, element);
            scope.isUserOnline = UserFactory.isUserOnline;
            scope.isTrainer = UserFactory.isTrainer;
            scope.goToPrivateDialog = RoomsFactory.goToPrivateDialog;
            scope.clickToUserEvent = function(user) {
                if (scope.selectForAsk == false)
                    RoomsFactory.goToPrivateDialog(user.intitaUserId);
            };

            scope.toggleUserToAskList = function(user) {
                //addOrRemove(scope.checked, parseInt(user.chatUserId));
                var size = 0;
                for (var key of Object.keys(scope.checked)) {
                    if (scope.checked[key] === true)
                        size++;

                }
                scope.checked.size = size;
                console.log(size);
            }

            scope.toggleSelect = function() {
                if (scope.selectForAsk == false) {} else if (scope.checked.size > 0) {
                    (function() {
                        var checkedList = [];
                        for (var key of Object.keys(scope.checked)) {
                            if (scope.checked[key] === true)
                                checkedList.push(key);

                        }
                        var callBack = function() {
                            scope.addTenantsToRoom(checkedList, $rootScope.askObject.param, function(data) {
                                tenantInviteDialog.close();
                            }, function(data) {});
                        };
                        if ($rootScope.askObject == undefined)
                            $rootScope.askObject = {};
                        $rootScope.askObject.callBack = callBack;
                        $rootScope.askObject.msg = "";
                        var tenantInviteDialog = ngDialog.open({
                            template: 'askTrainerMsgForTenant.html'
                        });
                    })();
                }
                cleanChecked();
                scope.toggleAll.fs = false;
                scope.selectForAsk = !scope.selectForAsk;
                return scope.selectForAsk;
            }

            function cleanChecked() {
                scope.checked = { size: 0 };
            }
            cleanChecked();
            scope.selectForAsk = false;
            scope.canSelectForAsk = false;

            scope.toggleAll = { fs: false };

            scope.toggleAllFunc = function() {
                cleanChecked();
                if (scope.toggleAll.fs) {
                    for (var tenant of scope.getTenantsList()) {
                        if (tenant.isParticipant == false) {
                            scope.checked[tenant.chatUserId] = true;
                        }
                    }
                }
            };

            scope.getTenantsList = function() {
                var tenantsList = UserFactory.getTenantsList();
                var participantsList = RoomsFactory.getParticipants();
                var resultList = [];
                for (var i = 0; i < tenantsList.length; i++) {
                    var isParticipant = false;
                    for (var j = 0; j < participantsList.length; j++) {
                        if (tenantsList[i].chatUserId == participantsList[j].chatUserId) {
                            isParticipant = true;
                            break;
                        }
                    }
                    if (!isParticipant) resultList.push(tenantsList[i]);
                    tenantsList[i].isParticipant = isParticipant;
                    //resultList.push(tenantsList[i]);
                }
                scope.canSelectForAsk = resultList.length > 0;
                return tenantsList;
            }
            scope.addTenantsToRoom = RoomsFactory.addTenantsToRoom;
        }

    };
}]);

angular.module('springChat.directives').directive('studentsBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', 'ChannelFactory', studentsBlock]);
angular.module('springChat.directives').directive('trainersBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', trainersBlock]);


function initFolded(scope, element) {
    scope.collapseBlock = function() {
        scope.collapsed = true;
    }
    scope.unCollapseBlock = function() {
        scope.collapsed = false;
    }
    scope.toggleCollapseBlock = function(event) {
        event.stopPropagation();
        event.preventDefault();
        scope.collapsed = !scope.collapsed;
        localStorage.setItem("chat/" + scope.blockName, scope.collapsed);
    }
    var storageValue = localStorage.getItem("chat/" + scope.blockName);
    if (storageValue != null)
        scope.collapsed = storageValue == "true";


    scope.scroll;
    scope.folded = true;
    var unfold = function() {
        scope.folded = false;
        scope.unCollapseBlock();
    }
    var fold = function(colapseRequired) {
        scope.folded = true;
        scope.unCollapseBlock();
    }

    scope.toggleFolded = function(event) {
        if (event != undefined && ($(event.target).hasClass("block_controll") || $(event.target).hasClass("unfoldable_element")))
            return;
        if (scope.folded)
            unfold();
        else
            fold();
        scope.scroll.overflowy = !scope.folded;
        if (scope.folded)
            scope.scroll.scrollTop(scope.scroll.scrollTop())

    }
    scope.scroll = $($(element).find(".scroll"));
    scope.scroll.overflowy = !scope.folded;
}

function studentsBlock($http, mySettings, RoomsFactory, UserFactory, ChannelFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/students_block.html',
        link: function(scope, element, attributes) {
            scope.blockName = lgPack.blockNames.students;
            scope.students = [];
            scope.groupRooms = [];
            updateModelForStudents();
            initFolded(scope, element);
            initRoomsFunctions(scope, ChannelFactory, UserFactory, RoomsFactory);

            function updateModelForStudents() {
                if (scope.groupRooms.length > 0) return;
                updateModelGet($http, "chat/get_students/", function(responseObj) {
                    scope.students = responseObj.data || [];
                });
            };

            function updateModelForGroups() {
                if (scope.groupRooms.length > 0) return;
                updateModelGet($http, "get_group_rooms_by_trainer?trainerChatId={0}".format(UserFactory.getChatUserId()), function(responseObj) {
                    scope.groupRooms = responseObj.data || [];
                });
            }
            scope.isUserOnline = UserFactory.isUserOnline;
            scope.goToPrivateDialog = RoomsFactory.goToPrivateDialog;
            scope.participantsSort = UserFactory.participantsSort;
            scope.isGroupMode = false;
            scope.toggleGroupMode = function() {
                if (scope.isGroupMode) {
                    disableGroupMode();
                } else {
                    enableGroupMode();
                }
            }
            var enableGroupMode = function() {
                scope.blockName = lgPack.blockNames.groups;
                scope.isGroupMode = true;
                updateModelForGroups();
            }
            var disableGroupMode = function() {
                scope.blockName = lgPack.blockNames.students;
                scope.isGroupMode = false;
                updateModelForStudents();
            }

        }

    };
};

function trainersBlock($http, mySettings, RoomsFactory, UserFactory) {
    return {
        restrict: 'EA',
        scope: {

        },
        templateUrl: 'static_templates/trainers_block.html',
        link: function(scope, element, attributes) {
            scope.blockName = lgPack.blockNames.trainers;
            scope.students = UserFactory.getStudentTrainerList;
            initFolded(scope, element);
            scope.isUserOnline = UserFactory.isUserOnline;
            scope.goToPrivateDialog = RoomsFactory.goToPrivateDialog;
            scope.participantsSort = UserFactory.participantsSort;
        }
    };
};

angular.module('springChat.directives').directive('participantsBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', participantsBlock]);

function participantsBlock($http, mySettings, RoomsFactory, UserFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/participants_block.html',
        scope: {},
        link: function(scope, element, attributes) {
            function updateModelForParticipants() {

            };
            var checkPrivateRelations = function(room, user) {
                if (room == null) return;
                if (room.type == 1 && room.privateUserIds != undefined) {

                    if (room.privateUserIds[0] == user.chatUserId || room.privateUserIds[1] == user.chatUserId)
                        return true;
                }
                return false;
            }
            scope.participants = RoomsFactory.getParticipants;
            scope.blockName = lgPack.blockNames.participants;
            scope.currentRoom = RoomsFactory.getCurrentRoom;
            scope.hideEmpty = false;
            scope.getChatUserId = UserFactory.getChatUserId;
            scope.participantsSort = UserFactory.participantsSort;
            scope.checkUserAdditionPermission = function() {
                return RoomsFactory.checkUserAdditionPermission(UserFactory.getChatUserId());
            }
            scope.isUserOnline = UserFactory.isUserOnline;
            scope.removeUserFromRoom = RoomsFactory.removeUserFromRoom;
            scope.checkUserRemovingPermission = function(participant) {
                return !checkPrivateRelations(scope.currentRoom(), participant) &&
                    scope.checkUserAdditionPermission() && participant.chatUserId &&
                    scope.currentRoom().roomAuthorId != participant.chatUserId;
            }
            var toggleNewUser = false;
            scope.toggleNewUser = function() {
                toggleNewUser = !toggleNewUser;
                if (toggleNewUser)
                    scope.$root.$emit('rootScope:roomsBlockModeChange', 2);
                else
                    scope.$root.$emit('rootScope:roomsBlockModeChange', 1);
                return toggleNewUser;
            }
            initFolded(scope, element);
        }

    };
};



angular.module('springChat.directives').directive('messagesBlock', ['$http', 'RoomsFactory', messagesBlock]);

function messagesBlock($http, RoomsFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/messages_block.html',
        link: function($scope, element, attributes) {
            $scope.messages = RoomsFactory.getMessages;
            var nice = $(".scroll");
        }

    };
};

angular.module('springChat.directives').directive('messageInput', ['$http', 'RoomsFactory', 'ChatSocket', '$timeout',
    'UserFactory', 'ChannelFactory', '$interval', messageInput
]);

function messageInput($http, RoomsFactory, ChatSocket, $timeout, UserFactory, ChannelFactory, $interval) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/message_input.html',
        link: function($scope, element, attributes) {
            var sendingMessage = null;
            var typing = undefined;
            var getParticipants = RoomsFactory.getParticipants;
            $scope.isAnyOneWriting = function() {
                var participants = getParticipants();
                for (var i = 0; i < participants.length; i++) {
                    if (participants[i].typing) return true;
                }
                return false;
            }
            $scope.getWritingUsersInfo = function() {
                var writingUsersNames = [];
                var participants = getParticipants();
                for (var i = 0; i < participants.length; i++) {
                    var participant = participants[i];
                    if (participant.typing) writingUsersNames.push(participant.username || participant.nickName);
                }
                //var notificationTemplate = writingUsersNames.length == 1 ? "{0} набирає повідомлення..." : "Користувачі {0}..."
                // return notificationTemplate.format(writingUsersNames.join(","));
                return writingUsersNames.join(",");
            }
            $scope.sendMessage = function(message, attaches, clearMessageInput) {
                var isClearMessageInputNeeded = clearMessageInput == null || clearMessageInput === true ? true : false;
                if (!UserFactory.isMessageSended())
                    return;
                var textOfMessage;
                if (typeof message === "undefined") textOfMessage = $scope.newMessage;
                else textOfMessage = message;
                if ((typeof textOfMessage === "undefined" || textOfMessage.length < 1) && attaches == undefined) {
                    return;
                }
                if (isClearMessageInputNeeded) {
                    $scope.newMessage.value = '';
                    $scope.files = [];
                    //$("#newMessageInput")[0].value  = '';
                }
                var destination = "/app/{0}/chat.message".format(RoomsFactory.getCurrentRoom().roomId);
                UserFactory.setMessageSended(false);
                if (attaches == null)
                    attaches = [];

                var msgObj = { message: textOfMessage, username: UserFactory.getChatUserNickname(), attachedFiles: attaches, chatUserAvatar: UserFactory.getChatuserAvatar() };
                if (ChannelFactory.isSocketSupport() == true) {
                    ChatSocket.send(destination, {}, JSON.stringify(msgObj));
                    var myFunc = function() {
                        if (sendingMessage != null) {
                            $timeout.cancel(sendingMessage);
                            sendingMessage = null;
                        }
                        if (UserFactory.isMessageSended()) return;
                        $scope.messageError();
                        UserFactory.setMessageSended(true);

                    };
                    sendingMessage = $timeout(myFunc, 2000);
                } else {
                    $http.post(serverPrefix + "/{0}/chat/message".format(RoomsFactory.getCurrentRoom().roomId), msgObj).
                    success(function(data, status, headers, config) {
                        UserFactory.setMessageSended(true);
                    }).
                    error(function(data, status, headers, config) {
                        $scope.messageError();
                        UserFactory.setMessageSended(true);
                    });
                };
                if (message === undefined)
                    $scope.newMessage.value = '';
                //set focus
                $scope.$$postDigest(function() {

                    $(".transparent_input.message_input").click();
                    $(".transparent_input.message_input").focus();
                });

                //.hasAttr()


            };

            $scope.clearFiles = function() {
                $scope.files = [];
            }
            $scope.selectFiles = function(files) {
                $scope.files = files;
            }

            $scope.removeFileFromUpload = function(index) {
                $scope.files.splice(index, 1);
            }


            $scope.sendMessageAndFiles = function() {
                var files = $scope.files;
                var textOfMessage = $scope.newMessage.value == null || $scope.newMessage.value.length < 1 ? " " : $scope.newMessage.value;
                if (files != null && files.length > 0) {
                    uploadXhr(files, "upload_file/" + RoomsFactory.getCurrentRoom().roomId,
                        function successCallback(data) {
                            $scope.uploadProgress = 0;
                            $scope.sendMessage(textOfMessage, JSON.parse(data), true);
                            $scope.$apply();
                        },
                        function(xhr) {
                            $scope.messageError();
                            $scope.uploadProgress = 0;
                            $scope.$apply();
                        },
                        function(event, loaded) {
                            $scope.uploadProgress = Math.floor((event.loaded / event.totalSize) * 100);
                            $scope.$apply();

                        });
                } else {
                    $scope.sendMessage(textOfMessage, undefined, true);
                }
                return false;
            }

            $scope.keyPress = function(event) {
                $scope.startTyping(event);
                if (event.keyCode == 13 && !event.shiftKey) {
                    event.stopPropagation();
                    event.target.blur();
                    setTimeout(function() {
                        $scope.sendMessageAndFiles();
                    }, 0);

                }
            }


            $scope.startTyping = function(event) {
                //var keyCode = event.which || event.keyCode;
                //var typedChar = String.fromCharCode(keyCode);
                //if(typedChar==' ')$scope.onMessageInputClick();       
                /*switch (specialInputMode) {
                    case INPUT_MODE.DOG_MODE:
                        processDogInput();
                        break;
                    case INPUT_MODE.COMMAND_MODE:
                        processCommandInput();
                        break;
                    case INPUT_MODE.TILDA_MODE:
                        processTildaInput();
                        break;

                }*/
                //      Don't send notification if we are still typing or we are typing a private message
                var needSend = true;
                if (typeof typing != "undefined") {
                    $timeout.cancel(typing);
                    needSend = false;
                }

                typing = $timeout(function() {
                    $scope.stopTyping();
                }, 1500);
                if (needSend)
                    ChatSocket.send("/topic/{0}/chat.typing".format(RoomsFactory.getCurrentRoom().roomId), {}, JSON.stringify({ username: UserFactory.getChatUserId(), typing: true }));
            };

            $scope.stopTyping = function() {
                if (angular.isDefined(typing)) {
                    $timeout.cancel(typing);
                    typing = undefined;
                    ChatSocket.send("/topic/{0}/chat.typing".format(RoomsFactory.getCurrentRoom().roomId), {}, JSON.stringify({ username: UserFactory.getChatUserId(), typing: false }));

                }
            };

            //$compile(element.contents())(scope);
        }

    };
};

angular.module('springChat.directives').directive('emHeightSource', function() {
    return {
        scope: {
            callback: '&callback'
        },
        link: function(scope, elem, attrs) {
            elem.on("resize", function() {
                if (scope.__height != elem.height()) {
                    scope.callback({ 'oldSize': scope.__height, 'newSize': elem.height() });
                }
                scope.__height = elem.height();
            });
        }
    }

});
angular.module('springChat.directives').directive('roomsBlockMini', ['$http', 'RoomsFactory', 'ChannelFactory', 'UserFactory', roomsBlockMini]);
var roomsBlockFilter = function(RoomsFactory) {
    return function(fields, state) {
        if (fields) { // added check for safe code
            var arrayFields = [];
            if (state == "LastContacts") {
                return fields;
            }
            /**
             MAX COUNT OF USERS
             */

            for (var i = 0; i < fields.length; i++) {
                if (fields[i].type == 1) {
                    arrayFields.push(fields[i]);
                }
            }
            return arrayFields;
        }
    };
};
angular.module('springChat.directives').directive('roomsBlock', ['$http', 'RoomsFactory', 'ChannelFactory', 'UserFactory', '$timeout', roomsBlock]).filter('roomsBlockFilter', ['RoomsFactory', roomsBlockFilter]);

var roomsBlockLinkFunction;
var initRoomsFunctions;

function roomsBlockMini($http, RoomsFactory, ChannelFactory, UserFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/rooms_block_mini.html',
        link: function($scope, element, attributes) {
            roomsBlockLinkFunction($scope, element, attributes, $http, RoomsFactory, ChannelFactory, UserFactory)
        }

    };
};

function roomsBlock($http, RoomsFactory, ChannelFactory, UserFactory, $timeout) {
    //TODO finish rooms search
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/rooms_block.html',
        link: function($scope, element, attributes) {
            roomsBlockLinkFunction($scope, element, attributes, $http, RoomsFactory, ChannelFactory, UserFactory);
            $scope.usersListSearched = [];
            $scope.isUserOnline = UserFactory.isUserOnline;


            /*$scope.getUsersByEmail = function(query, deferred) {
                var url = serverPrefix + "/get_users_like?login=" + query;
                $http.get(url).success((function(deferred, data) { // send request
                    var results = data;
                    deferred.resolve({ results: results });
                }).bind(this, deferred));
            };*/
            $scope.searchingRunning = false;

            $scope.participantsSort = UserFactory.participantsSort;
            $scope.updateChatUsersByEmail = function(email, delay) {
                $timeout.cancel($scope.updatingUsersByEmailPromise);
                if (email == null || email.length < 1) {
                    $scope.updatingUsersByEmailPromise = undefined;
                    $scope.usersListSearched = [];
                    return;
                }
                $scope.searchingRunning = true;
                $scope.updatingUsersByEmailPromise = $timeout(function() {
                    var url = serverPrefix + "/get_users_log_events_like?login=" + email;
                    return $http.get(url, {}).success(function(data) { // send request
                        $scope.searchingRunning = false;
                        $scope.usersListSearched = data;
                    }).finally(function() {
                        $scope.searchingRunning = false;
                    });
                }, delay);
            };
            $scope.updateRoomsByQuery = function(query, delay) {
                $timeout.cancel($scope.updatingUsersByEmailPromise);
                if (query == null || query.length < 1) {
                    $scope.updatingUsersByEmailPromise = undefined;
                    $scope.roomsListSearched = [];
                    return;
                }
                $scope.searchingRunning = true;
                $scope.updatingUsersByEmailPromise = $timeout(function() {
                    var url = serverPrefix + "/get_rooms_containing_string?query=" + query;
                    return $http.get(url, {}).success(function(data) { // send request
                        $scope.searchingRunning = false;
                        $scope.roomsListSearched = data;
                    }).finally(function() {
                        $scope.searchingRunning = false;
                    });
                }, delay);
            };
        }
    };
};



angular.module('springChat.directives').directive('notificable', ['$templateRequest', '$sce', '$compile', '$parse', 'UserFactory', notificable]);

function notificable($templateRequest, $sce, $compile, $parse, UserFactory) {
    //TODO finish rooms search
    return {
        restrict: 'EA',
        link: function(scope, element, attrs) {
            if (attrs.template == null) {
                console.error('Template must be set');
                return;
            }

            scope.getItems = UserFactory.getNotifications;
            scope.notificationClick = function(item) {
                switch (item.type) {
                    case 'user_wait_tenant':
                        userWaitTenantHandler(item);
                        break;
                    case 'alert':
                        alertHandler();
                        break;
                }
            }

            scope.cancelEventClick = function(event, item) {
                event.stopPropagation();
                UserFactory.removeNotificationByValue(item);
            }

            function userWaitTenantHandler(item) {
                UserFactory.confirmToHelp(item.roomId);
            }

            function alertHandler() {

            }


            var templatePath = 'static_templates/' + attrs.template + '.html';
            var templateUrl = $sce.getTrustedResourceUrl(templatePath);
            $templateRequest(templateUrl).then(function(template) {
                // template is the HTML template as a string

                // Let's put it into an HTML element and parse any directives and expressions
                // in the code. (Note: This is just an example, modifying the DOM from within
                // a controller is considered bad style.)
                var container = $('#' + attrs.container);
                scope.$parent.toggleVisible = function() {
                    container.toggleClass('shown');
                }
                $compile(container.html(template).contents())(scope);
            }, function() {
                // An error has occurred
            });

        }
    };
};


initRoomsFunctions = function($scope, ChannelFactory, UserFactory, RoomsFactory) {
    function addNewUser(chatUserId) {
        RoomsFactory.addUserToRoom(chatUserId);
    }
    $scope.getOpponentIdFromRoom = function getOpponentIdFromRoom(room) {
        if (room.privateUserIds == null) return null;
        var currentUserId = UserFactory.getChatUserId();
        if (room.privateUserIds[0] == currentUserId)
            return room.privateUserIds[1];
        if (room.privateUserIds[1] == currentUserId)
            return room.privateUserIds[0];
    }

    $scope.clickToRoomEvent = function(room) {
        if ($scope.createEnabled) //if ($scope.searchEnabled || $scope.createEnabled)
            return;
        room.nums = 0;
        switch ($scope.mode) {
            case 1:
                $scope.doGoToRoom(room.roomId);
                break;
            case 2:
                var opponentUserId = $scope.getOpponentIdFromRoom(room);
                if (opponentUserId != null)
                    addNewUser(opponentUserId);
                else
                    console.warn('opponentUserId is null');
                $scope.mode = 1;
                break;
        }
        $scope.$root.hideMenu();
    }
    $scope.clickToUserEvent = function(user) {
        switch ($scope.mode) {
            case 1:
                $scope.onFriendClick(user);
                break;
            case 2:
                addNewUser(user.chatUserId);
                $scope.mode = 1;
                break;
        }
        $scope.$root.hideMenu();
    }

    $scope.doGoToRoom = function(roomId) {
        if ($scope.createEnabled) //if ($scope.searchEnabled || $scope.createEnabled)
            return;
        $scope.loadOnlyFilesInfiniteScrollMode = false;
        ChannelFactory.changeLocation('/dialog_view/' + roomId);
    }

    $scope.mode = 1;
}
roomsBlockLinkFunction = function($scope, element, attributes, $http, RoomsFactory, ChannelFactory, UserFactory) {
    $scope.isRoomPrivate = RoomsFactory.isRoomPrivate;
    //$scope.isRoomConsultation = RoomsFactory.isRoomConsultation;

    var userListForAddedToNewRoom = [];
    $scope.isUserOnline = UserFactory.isUserOnline;
    $scope.rooms = RoomsFactory.getRooms;
    $scope.searchEnabled = false;
    $scope.createEnabled = false;
    $scope.getCurrentRoom = RoomsFactory.getCurrentRoom;
    $scope.tabState = "Contacts";
    $scope.canBeAddedToRoom = function(room) {
        //TODO check if user can be added to room
        var opponentUserId = $scope.getOpponentIdFromRoom(room);
        if (opponentUserId == null) return null;
        return $scope.mode == 2 && !RoomsFactory.containsUserId(opponentUserId);
    };
    /* $scope.stripHtml = function(html)
     {
         var tmp = document.createElement("DIV");
         tmp.innerHTML = html;
         return tmp.textContent || tmp.innerText || "";
     }*/
    /****
     * 1 - default
     * 2 - add new user
     */
    initRoomsFunctions($scope, ChannelFactory, UserFactory, RoomsFactory);

    var roomsBlockModeChangeSubscription;
    $scope.$on('$destroy', function() {
        roomsBlockModeChangeSubscription();
    });

    roomsBlockModeChangeSubscription = $scope.$root.$on('rootScope:roomsBlockModeChange', function(event, data) {
        $scope.mode = data;
        switch ($scope.mode) {
            case 1:
                break;
            case 2:
                $scope.showContacts();
                break;
        }

    });
    $scope.sortBy = ['date', 'string'];
    $scope.displayLetters = false;
    $scope.room_create_input = "";
    $scope.isInterlocutorOnline = function(room) {
        if (room == null || room.privateUserIds == null || room.privateUserIds.length < 1) return false;
        if (UserFactory.getChatUserId() != room.privateUserIds[0] && $scope.isUserOnline(room.privateUserIds[0])) return true;
        if (UserFactory.getChatUserId() != room.privateUserIds[1] && $scope.isUserOnline(room.privateUserIds[1])) return true;
        return false;
    }

    $scope.goToPrivateDialog = RoomsFactory.goToPrivateDialog;

    $scope.toggleSearch = function() {
        $scope.searchEnabled = !$scope.searchEnabled;
        return $scope.searchEnabled;
    }
    $scope.createNewRoom = function($event) {
        RoomsFactory.addDialog($scope.room_create_input, userListForAddedToNewRoom).success(function(data, status, headers, config) {
            $scope.$root.hideMenu();
            ChannelFactory.changeLocation("/dialog_view/" + data);
        });
        return false;
    }

    $scope.toggleCreate = function() {
        $scope.createEnabled = !$scope.createEnabled;
        if ($scope.createEnabled == false) {
            userListForAddedToNewRoom = [];
            $scope.room_create_input = "";
        } else {
            $scope.showContacts();
        }
    }

    $scope.canBeRemoved = function(room) {
        if (room.type == 1)
            return false;
        return true;
    }
    $scope.showLastContacts = function() {
        if ($scope.mode == 2)
            return;

        $scope.tabState = "LastContacts";
        $scope.sortBy = ['-nums', '-date'];
        $scope.displayLetters = false;
    }
    $scope.showContacts = function() {
        $scope.tabState = "Contacts";
        $scope.sortBy = ['string'];
        $scope.displayLetters = true;
    }

    $scope.returnAvatar = function(room) {
        if (room.avatars == null) return "noname.png";
        if (room.avatars.length > 1)
            return room.avatars[1];
        return room.avatars[0];
    }
    $scope.$on('dialog_view_route', function() {
        $scope.showLastContacts();
    });
    /* $scope.showLastContactItem = function(roomId) {
         $scope.showLastContacts();
         //TODO select specific last contact item
     }*/

    $scope.toggleForCreatRoom = function(room) {
        // alert(chatUserId);
        if (room.type == 1 && room.privateUserIds != undefined) {
            if (room.privateUserIds[0] == UserFactory.getChatUserId())
            //userListForAddedToNewRoom.push(room.privateUserIds[1]);
                addOrRemove(userListForAddedToNewRoom, room.privateUserIds[1]);
            if (room.privateUserIds[1] == UserFactory.getChatUserId())
            //userListForAddedToNewRoom.push(room.privateUserIds[0]);
                addOrRemove(userListForAddedToNewRoom, room.privateUserIds[0]);
        }

    }
    $scope.getRooms = function() {
        if (!$scope.searchEnabled)
            return RoomsFactory.getRooms();
        else return $scope.roomsListSearched;
    }

    var nice = $(".scroll");
    $scope.showLastContacts();
};

var fileMiniatureDirective = angular.module('springChat.directives').directive('fileMiniature', ['$http', 'RoomsFactory', 'ChannelFactory', '$parse', fileMiniature]);

function fileMiniature($http, RoomsFactory, ChannelFactory, $parse) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/file_miniature.html',
        link: function($scope, element, attributes) {
            //TODO filesMiniature
            var supportedExtensions = ['aac',
                'ai', , 'aiff',
                'asp',
                'avi',
                'bmp',
                'c',
                'cpp',
                'css',
                'dat',
                'dmg',
                'doc',
                'docx',
                'dot',
                'dotx',
                'dwg',
                'dxf',
                'eps',
                'exe',
                'flv',
                'gif',
                'h',
                'html',
                'ics',
                'iso',
                'java',
                'jpg',
                'js',
                'key',
                'less',
                'm4v',
                'mid',
                'mov',
                'mp3',
                'mp4',
                'mpg',
                'odp',
                'ods',
                'odt',
                'otp',
                'ots',
                'ott',
                'pdf',
                'php',
                'png',
                'pps',
                'ppt',
                'psd',
                'py',
                'qt',
                'rar',
                'rb',
                'rtf',
                'sass',
                'scss',
                'sql',
                'tga',
                'tgz',
                'tiff',
                'txt',
                'wav',
                'xls',
                'xlsx',
                'xml',
                'yml',
                'zip'
            ];
            var getFileExtensionByName = function(name) {
                return name.split('.').pop();
            }
            var isExtensionSupported = function(extension) {
                if (supportedExtensions.indexOf(extension.toLowerCase()) != -1) return true;
                else return false;
            }
            var getImageByExtension = function(ext) {
                var lowerCaseExtension = ext.toLowerCase();
                var urlTemplate = "images/svg-file-icons/{0}.svg";
                if (isExtensionSupported(lowerCaseExtension)) return urlTemplate.format(lowerCaseExtension);
                else return urlTemplate.format('nopreview');
            }

            var link = $parse(attributes.link)($scope);
            if (attributes.removeCallback != null) {
                $scope.removeItemCallback = $parse(attributes.removeCallback)($scope);
                $scope.removable = true;
            }
            //$scope.fileIndex = $parse(attributes.fileIndex)($scope);
            //use only name (not link)
            var nameOnly = typeof attributes.nameonly == "undefined" || attributes.nameonly == 'false' ? false : true;
            var derandomaziedName = nameOnly ? link : $scope.getNameFromRandomizedUrl(link);

            var extension = getFileExtensionByName(derandomaziedName);
            $scope.fileName = derandomaziedName;
            if (!nameOnly)
                $scope.link = link;
            $scope.imageSrc = getImageByExtension(extension);
        }


    };
};

fileMiniatureDirective.filter('fileNamesFilter', function() {
    return function(files) {
        if (files == null) return null;
        var names = [];
        for (var i = 0; i < files.length; i++) {
            names.push(files[i].name);
        }
        return names;
    }
});


var compilable = function($compile, $parse) {
    return {
        restrict: 'E',
        link: function(scope, element, attr) {
            scope.$watch(attr.content, function() {
                var content = ($parse(attr.content)(scope)).replace(new RegExp("compilable", 'g'), "div");
                content = content.replace(new RegExp("ng-bind", 'g'), "ha");

                element.html(content);
                if (typeof attr.callback != 'undefined') {
                    var callBackFunction = new Function("return " + attr.callback)();
                    if (typeof callBackFunction != 'undefined')
                        callBackFunction(element);
                }
                $compile(element.contents())(scope);
                //element.html =$parse(attr.content)(scope);

            }, true);
        }
    }
};
angular.module('springChat.directives').directive('compilable', ['$compile', '$parse', compilable]);

var ngDraggable = function($document) {
    return {
        restrict: 'A',
        scope: {
            dragOptions: '=ngDraggable'
        },
        link: function(scope, elem, attr) {
            var startX, startY, x = 0,
                y = 0,
                start, stop, drag, container;




            var dragElement;
            var containerElm;
            // Obtain drag options
            if (scope.dragOptions) {
                start = scope.dragOptions.start;
                drag = scope.dragOptions.drag;
                stop = scope.dragOptions.stop;
                var id = scope.dragOptions.container;
                if (id) {
                    container = document.getElementById(id).getBoundingClientRect();
                    containerElm = angular.element(document.getElementById(id));
                }
                dragElement = angular.element(document.getElementById(scope.dragOptions.dragElement));

            }

            // Bind mousedown event
            elem.on('mousedown', function(e) {

                if ($(dragElement).hasClass("drag-disable") || e.target != elem[0])
                    return true;

                e.preventDefault();
                startX = e.clientX - dragElement[0].offsetLeft;
                startY = e.clientY - dragElement[0].offsetTop;
                $document.on('mousemove', mousemove);
                $document.on('mouseup', mouseup);
                if (start) start(e);
            });

            // Handle drag event
            function mousemove(e) {

                y = e.clientY - startY;
                x = e.clientX - startX;
                setPosition();
                if (drag) drag(e);
            }

            // Unbind drag events
            function mouseup(e) {
                $document.unbind('mousemove', mousemove);
                $document.unbind('mouseup', mouseup);
                if (stop) stop(e);
            }

            // Move element, within container if provided
            function setPosition() {
                var width = dragElement[0].offsetWidth,
                    height = dragElement[0].offsetHeight;

                if (container) {
                    if (x < container.left) {
                        x = container.left;
                    } else if (x > container.right - width) {
                        x = container.right - width;
                    }
                    if (y < container.top) {
                        y = container.top;
                    } else if (y > container.bottom - height) {
                        y = container.bottom - height;
                    }
                }

                dragElement.css({
                    top: y + 'px',
                    left: x + 'px'
                });
            }
        }
    }
};

angular.module('springChat.directives').directive('ngDraggable', ['$document', ngDraggable]);


angular.module('springChat.directives').directive('checkbox', function() {
    return {
        restrict: 'EA',
        replace: true,
        template: '<a class="g-checkbox"><input id="{{id}}" type="checkbox" style="display: none" ng-checked="testModel"/></a>',
        scope: {
            id: '@',
            testModel: '=value',
            callback: '&callback'
        },
        transclude: false,
        link: function(scope, element, attrs, ngModelCtrl) {
            element.removeAttr('id');
            scope.$on('$destroy', function() {
                unregister();
            });

            var unregister = scope.$watch('testModel', function(neww, old) {
                console.log(neww);
                if (neww == undefined && old == undefined)
                    return;

                if (neww)
                    element.addClass('checked');
                else
                    element.removeClass('checked');
                if (scope.callback != undefined)
                    scope.callback();
            });

            element.bind('click', function() {
                scope.$apply(function() {
                    scope.testModel = !scope.testModel;
                });
            })
        }

    };
});

function audioVideoRP($http, RoomsFactory) { //avpr - Audio/Video player/recorder
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/audio_video_player.html',
        link: function($scope, element, attributes) {
            var mediaConstraints = {
                audio: true
            };

            navigator.getUserMedia(mediaConstraints, onMediaSuccess, onMediaError);

            function onMediaSuccess(stream) {
                $scope.$apply(function() {
                    $scope.stream = $sce.trustAsResourceUrl(URL.createObjectURL(stream));
                })
                var mediaRecorder = new MediaStreamRecorder(stream);
                mediaRecorder.mimeType = 'audio/webm';

                mediaRecorder.ondataavailable = function(blob) {
                    // POST/PUT "Blob" using FormData/XHR2
                    var blobURL = URL.createObjectURL(blob);
                    //  document.write('<a href="' + blobURL + '">' + blobURL + '</a>');
                };
                mediaRecorder.start(30000);
            }

            function onMediaError(e) {
                console.error('media error', e);
            }
        }

    };
}

angular.module('springChat.directives').directive('audioVideoRP', ['$http', 'RoomsFactory', audioVideoRP]); //
