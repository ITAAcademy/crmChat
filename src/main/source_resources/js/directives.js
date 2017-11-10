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
                    restart: function() {
                        toggle = false
                    }
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
        template: `<div class="rating label-right star-svg" ng-class="containerClass">
    <div class="label-value">{{ratingValue}}</div>
    <div class="star-container" ng-class="getColor()">
        <div ng-repeat="star in stars" ng-class="star.class" ng-click="toggle($event, $index)">
            <svg class="star-filled">
                <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="images/star-rating.icons.svg#star-filled"></use>
            </svg>
             <svg class="star-half">
                <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="images/star-rating.icons.svg#star-half"></use>
            </svg>
             <svg class="star-empty">
                <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="images/star-rating.icons.svg#star-empty"></use>
            </svg>
        </div>
    </div>
</div>` /*<textarea ng-if="ratingValue < marks.good" placeholder="{{::placeholder}}" />`*/ ,
        scope: {
            ratingValue: '=ngModel',
            max: '=?', // optional (default is 10)
            starsCount: '=?',
            halfEnable: "=?",
            placeholder: "@?",
            marks: "=?",
            onRatingSelect: '&?',
            readonly: '=?',
            ngClass: '@'
        },
        link: function(scope, element, attributes) {

            if (attributes.onratingselect == "angular.noop")
                attributes.onratingselect = function() {};


            if (scope.marks == undefined) {
                scope.marks = {};
            }


            if (scope.max == undefined) {
                scope.max = 10;
            }
            scope.marks.bad = scope.marks.bad || Math.floor(scope.max * 0.4);
            scope.marks.good = scope.marks.good || Math.floor(scope.max * 0.8);
            scope.getColor = function(index) {
                /*if (index > scope.ratingValue)
                    return '';

                if (index > scope.marks.good)
                    return 'good';
                if (index > scope.marks.bad)
                    return 'bad';
                 return 'horrible';*/
                if (scope.ratingValue == 0)
                    return '';
                if (scope.ratingValue >= scope.marks.good)
                    return 'good';
                if (scope.ratingValue >= scope.marks.bad)
                    return 'bad';
                return 'horrible';
            }

            if (scope.starsCount == undefined) {
                scope.starsCount = 5;
            }
            if (scope.halfEnable == undefined) {
                scope.halfEnable = false;
            }
            scope.stars = [];
            scope.containerClass = scope.ngClass + ' value-0';
            for (var i = 0; i < scope.starsCount; i++) {
                scope.stars.push({
                    class: i < scope.ratingValue ? 'star empty' : 'star filled '
                });
            }

            scope.$watch('ratingValue', function() {
                updateStars();
            })

            function updateStars() {
                var value = (scope.ratingValue * scope.starsCount) / scope.max;
                var full = scope.ratingValue % 2 == 0 || !scope.halfEnable;

                for (var i = 0; i < scope.starsCount; i++) {
                    scope.stars[i].class = i < value ? !full && i + 1 > value ? 'star half' : 'star filled' : 'star empty';
                    //scope.stars[i].class += ' ' + scope.getColor(i + 1);
                }
                scope.containerClass = scope.ngClass + ' value-' + Math.round(value);
            };
            scope.toggle = function(event, index) {
                if (scope.readonly === true)
                    return;
                var target = $(event.currentTarget);
                var posX = target.offset().left,
                    posY = target.offset().top;
                //alert( (event.pageX - posX) + ' , ' + (event.pageY - posY));
                var full = true;
                if (event.pageX - posX < target.width() / 2 && scope.halfEnable)
                    full = false;


                if (scope.readonly == undefined || scope.readonly === false) {
                    if (full == false)
                        index -= 0.5;
                    scope.ratingValue = ((index + 1) * scope.max) / scope.starsCount;

                    //
                    if (scope.onRatingSelect != undefined)
                        attributes.onratingselect({
                            rating: scope.ratingValue
                        });
                    updateStars(full);
                }
            };
            if (scope.ratingValue == undefined)
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
                scope.checked = {
                    size: 0
                };
            }
            cleanChecked();
            scope.selectForAsk = false;
            scope.canSelectForAsk = false;

            scope.toggleAll = {
                fs: false
            };

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
angular.module('springChat.directives').directive('userBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', 'ChannelFactory', '$rootScope', 'ngDialog', userBlock]);
angular.module('springChat.directives').directive('studentsBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', 'ChannelFactory', 'StateFactory', studentsBlock]);
angular.module('springChat.directives').directive('trainersBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', trainersBlock]);


function initFolded(scope, element) {
    var storageValue = localStorage.getItem("chat/" + scope.blockName);
    if (storageValue != null)
        scope.collapsed = storageValue == "true";


    scope.scroll;
    var foldedLevel = 0;

    scope.toggleFolded = function(event) {
        if (event != undefined && ($(event.target).hasClass("block_controll") || $(event.target).hasClass("unfoldable_element")))
            return;
        
        var newFoldedLevel = (foldedLevel + 1) % 3 ;
        scope.scroll.overflowy = foldedLevel != 0;
        if (scope.folded) {
            scope.scroll.scrollTop(scope.scroll.scrollTop())

        }

            foldedLevel = newFoldedLevel;

    }
    scope.scroll = $($(element).find(".scroll"));
    scope.scroll.overflowy = foldedLevel != 0;


    scope.isHalfUnfolded = function(){
        return foldedLevel == 1;
    }
    scope.isFullUnfolded = function(){
        return foldedLevel == 2;
    }
    scope.isUnfolded = function(){
        return scope.isHalfUnfolded() || scope.isFullUnfolded();
    }
}

function studentsBlock($http, mySettings, RoomsFactory, UserFactory, ChannelFactory, StateFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/students_block.html',
        link: function(scope, element, attributes) {
            scope.blockName = lgPack.blockNames.students;
            scope.students = [];
            scope.groupRooms = [];
            updateModelForStudents();
            initFolded(scope, element);
            initRoomsFunctions(scope, ChannelFactory, UserFactory, RoomsFactory, StateFactory);

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


function userBlock($http, mySettings, RoomsFactory, UserFactory, ChannelFactory, $rootScope, ngDialog) {
    return {
        restrict: 'EA',
        scope: {

        },
        templateUrl: 'static_templates/user_block.html',
        link: function(scope, element, attributes) {
            scope.blockName = lgPack.blockNames.user;
            initFolded(scope, element);
            scope.getUser = UserFactory.getUser;
            scope.mySettings = mySettings;
            scope.getCurrentLocation = function() {
                var location = window.location.href;
                location = replaceAll(location, '#', '%23');
                return location;
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

angular.module('springChat.directives').directive('participantsBlock', ['$http', 'mySettings', 'RoomsFactory', 'UserFactory', 'StateFactory', participantsBlock]);

function participantsBlock($http, mySettings, RoomsFactory, UserFactory, StateFactory) {
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
            scope.isNewUserMode = StateFactory.isAddUserToDialogRoomBlockMode;

            scope.toggleNewUser = function($event) {
                $event.stopPropagation();
                StateFactory.toggleAddUserToDialogRoomBlockMode();
                if (!StateFactory.isAddUserToDialogRoomBlockMode()) {
                    var usersIdsList = StateFactory.getUserListForAddedToCurrentRoom();
                    RoomsFactory.addUsersToRoom(usersIdsList);
                }
                return StateFactory.isAddUserToDialogRoomBlockMode();
            }
            scope.getUserProfileLink = function(id) {
                return mySettings.baseUrl + '/profile/' + id;
            }
            initFolded(scope, element);
        }

    };
};



angular.module('springChat.directives').directive('messagesBlock', ['$timeout', '$http', 'RoomsFactory', 'UserFactory', '$rootScope', 'StateFactory', 'MessageInputService', messagesBlock]);

function messagesBlock($timeout, $http, RoomsFactory, UserFactory, $rootScope, StateFactory, MessageInputService) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/messages_block.html',
        link: function($scope, element, attributes) {
            $scope.UserFactory = UserFactory;
            $scope.RoomsFactory = RoomsFactory;
            $scope.messages = RoomsFactory.getMessages;
            $scope.isMessageLiked = RoomsFactory.isMessageLiked;
            $scope.isMessageDisliked = RoomsFactory.isMessageDisliked;
            $scope.likeMessage = RoomsFactory.likeMessage;
            $scope.dislikeMessage = RoomsFactory.dislikeMessage;

            $rootScope.retreiveMessageBody = function(message) {
                if (message.active) {
                    return $rootScope.parseMsg(message.body);
                }
                return "<i>Повідомлення видалено " + $rootScope.formatDateWithLast(message.updateat, false, true) + "</i>";

            }

            $scope.toggleMessageSelected = function(message) {
                console.log('toggleMessageSelected:' + message.id);
                if (getSelectedText() == null || getSelectedText().length < 1) {
                    message.selected = message.selected == null ? true : !message.selected;
                }
            }

            $scope.isMessageRemovable = function(message) {
                if (message.author.id != UserFactory.getChatUserId())
                    return false;
                var MS_IN_MINUTE = 60 * 1000;
                if ((new Date) - message.date > MS_IN_MINUTE * 5)
                    return false;

                return true;
            }
            $scope.isMessageEditable = function(message) {
                return $scope.isMessageRemovable(message);
            }
            $scope.editMessage = function(message) {
                StateFactory.setMessageInputOldMessageEditingMode(message.id);
                MessageInputService.setMessageBody(message.body);
                MessageInputService.setMessageAttaches(message.attachedFiles);

            }

            $scope.bookmarkMessage = function(message) {
                $http.post(serverPrefix + '/chat/messages/bookmark_toggle', message.id).then(function(response) {
                    var added = response.data;
                    if (added) {
                        message.bookmarked = true;
                    } else {
                        message.bookmarked = false;
                    }
                });
            }

            $scope.removeMessage = function(message) {
                var url = '/chat/messages/remove/' + message.id;
                $http.post(serverPrefix + url).
                success(function(data, status, headers, config) {

                }).
                error(function(data, status, headers, config) {
                    console.log('message removing failed');
                });
            }
            /*
                Likes
            */
            let lastChoise = {};
            let hideWhoLikeOrDisLikeCancel = null;
            let hideWhoLikeOrDisLikeFunc = function() {
                $('#who-like-' + lastChoise.msg.id).removeClass('active');
            }
            let showWhoLikeOrDisLikeFunc = function() {
                $('#who-like-' + lastChoise.msg.id).addClass('active');
            }

            let prepareForShowWhoLikeOrDisLikeFunc = function(type, message) {
                whoLikeUsersPage = 1;
                if (lastChoise.type == type && lastChoise.msg == message) {
                    showWhoLikeOrDisLikeFunc();
                    return false;
                }
                $scope.whoLikeUsersByMessage[message.id] = [];
                if (hideWhoLikeOrDisLikeCancel != null) {
                    hideWhoLikeOrDisLikeFunc();
                    $timeout.cancel(hideWhoLikeOrDisLikeCancel);
                    hideWhoLikeOrDisLikeCancel = null;
                }
                lastChoise.type = type;
                lastChoise.msg = message;

                showWhoLikeOrDisLikeFunc();
                return true;
            };

            $scope.whoLikeBusy = false;
            var whoLikeUsersPage = 1;
            $scope.whoLikeUsersByMessage = {};

            $scope.showWhoLike = function($event, message) {
                if (message.likes == 0)
                    return;
                $event.stopPropagation();
                if (prepareForShowWhoLikeOrDisLikeFunc('like', message))
                    RoomsFactory.getWhoLikesByMessage(message, 1).then(function(res) {
                        $scope.whoLikeUsersByMessage[message.id] = res.data;
                    }, function(res) {
                        hideWhoLikeOrDisLikeFunc();
                    })

            }

            $scope.loadOtherWhoLikeUsers = function(message) {
                console.log('loadOtherWhoLikeUsers');
                $scope.whoLikeBusy = true;
                RoomsFactory.getWhoLikesByMessage(message, whoLikeUsersPage).then(function(res) {
                    $scope.whoLikeUsersByMessage[message.id] =
                        res.data.concat($scope.whoLikeUsersByMessage[message.id]);
                    $scope.whoLikeBusy = false;
                    whoLikeUsersPage++;
                }, function(res) {
                    $scope.whoLikeBusy = false;
                    //hideWhoLikeOrDisLikeFunc();
                })
            }

            $scope.showWhoDisLike = function($event, message) {
                if (message.dislikes == 0)
                    return;
                $event.stopPropagation();
                if (prepareForShowWhoLikeOrDisLikeFunc('disLike', message))
                    RoomsFactory.getWhoDisLikesByMessage(message).then(function(res) {
                        $scope.whoLikeUsersByMessage[message.id] = res.data;
                    }, function(res) {
                        hideWhoLikeOrDisLikeFunc();
                    })
            }

            $scope.hideWhoLikeOrDisLike = function() {
                if (lastChoise != undefined && lastChoise.msg != undefined)
                    hideWhoLikeOrDisLikeCancel = $timeout(hideWhoLikeOrDisLikeFunc, 500);
            };
        }

    };
};

angular.module('springChat.directives').directive('messageInput', ['$http', 'RoomsFactory', 'ChatSocket', '$timeout',
    'UserFactory', 'ChannelFactory', '$interval', 'ngDialog', 'toaster', '$window', 'StateFactory', 'MessageInputService', messageInput
]);

function messageInput($http, RoomsFactory, ChatSocket, $timeout, UserFactory, ChannelFactory, $interval, ngDialog, toaster, $window, StateFactory, MessageInputService) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/message_input.html',
        link: function($scope, element, attributes) {


            MessageInputService.setMessageBodySetter(function(text) {
                $scope.newMessage.value = text;
            });
            MessageInputService.setMessageAttaches(function(attaches) {
                $scope.files = attaches;
            });

            $scope.cancelMessageEditing = function() {
                StateFactory.setMessageInputDefaultMode();
                $scope.newMessage.value = "";
            }

            $scope.files = [];
            /*$("[contenteditable='true']").on('focus', function() {
                var $this = $(this);
                $this.html($this.html() + '<br>'); // firefox hack
            });

  $("[contenteditable='true']").on('blur', function(){
    var $this = $(this);
    var text = $this.html();
    text = text.replace(/<br>$/, "");
    $this.html(text);
  });*/

            //DOM has finished rendering
            $('#message_input_editable').on('paste', function(e) {
                e.preventDefault();
                var text = (e.originalEvent || e).clipboardData.getData('text/plain');
                //var escapedHtml = htmlEscape(text);

                var linkified = linkifyStr(text, {});
                pasteHtmlAtCaret(linkified);
                //$scope.newMessage = text;

            });

            var sendingMessage = null;
            var typing = undefined;
            var getParticipants = RoomsFactory.getParticipants;
            $scope.getSkypeContacts = RoomsFactory.getSkypeContacts;

            var clearMessagesAndFiles = function() {
                $scope.newMessage.value = '';
                $scope.files = [];
            }

            $scope.onClick = function() {
                RoomsFactory.updateLastActivity();
            }
            $scope.isCurrentRoomPrivate = RoomsFactory.isCurrentRoomPrivate;
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
                var notificationTemplate = writingUsersNames.length == 1 ? "{0} набирає" : "{0} набирають"
                return notificationTemplate.format(writingUsersNames.join(","));
                // return writingUsersNames.join(",");
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
                    clearMessagesAndFiles();
                    //$("#newMessageInput")[0].value  = '';
                }
                var destination = "/app/{0}/chat.message".format(RoomsFactory.getCurrentRoom().roomId);
                UserFactory.setMessageSended(false);
                if (attaches == null)
                    attaches = [];

                var author = {
                    avatar: UserFactory.getChatuserAvatar(),
                    id: UserFactory.getChatUserId(),
                    nickName: UserFactory.getChatUserNickname()
                }
                var msgObj = {
                    body: textOfMessage,
                    author: author,
                    attachedFiles: attaches,
                    date: new Date()
                };
                if (ChannelFactory.isSocketSupport() == true) {
                    ChatSocket.send(destination, {}, JSON.stringify(msgObj));
                    var myFunc = function() {
                        if (sendingMessage != null) {
                            $timeout.cancel(sendingMessage);
                            sendingMessage = null;
                        }
                        if (UserFactory.isMessageSended()) return;
                        //$scope.messageError();
                        $scope.newMessage.value = message;
                        $scope.files = attaches;
                        UserFactory.setMessageSended(true);

                    };
                    sendingMessage = $timeout(myFunc, 10000);
                } else {
                    $http.post(serverPrefix + "/{0}/chat/message".format(RoomsFactory.getCurrentRoom().roomId), msgObj).
                    success(function(data, status, headers, config) {
                        UserFactory.setMessageSended(true);
                    }).
                    error(function(data, status, headers, config) {
                        $scope.messageError();
                        $scope.newMessage.value = message;
                        $scope.files = attaches;
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
            document.onpaste = function(event) {
                var items = (event.clipboardData || event.originalEvent.clipboardData).items;
                var files = [];
                for (var index = 0; index < items.length; index++) {
                    var item = items[index];
                    if (item.kind === 'file' && item.type.indexOf('image/') != -1) {
                        var blob = item.getAsFile();
                        files.push(new File([blob], "file" + $scope.files.length + '.png', {
                            type: "image/png"
                        }));
                    }
                    $scope.selectFiles(files, true);
                }
            }

            $scope.selectFiles = function(files, ignoreClean) {
                if (ignoreClean === true) {

                } else {
                    $scope.files = [];
                }
                for (var i = 0; i < files.length; i++) {
                    (function() {
                        var file = files[i];
                        var reader = new FileReader();

                        function onload() {
                            file.data = reader.result;
                            $scope.files.push(file);
                            $scope.$apply();
                        }
                        reader.onloadend = onload
                        if (file.type.indexOf('image/') != -1) {
                            reader.readAsDataURL(file);
                        } else
                            $scope.files.push(file);
                    })()

                }
            }

            $scope.removeFileFromUpload = function(index) {
                $scope.files.splice(index, 1);
            }

            var updateMessage = function(msgId, body, files) {
                var messageObj = {
                    id: msgId,
                    body: body,
                    attachedFiles: files
                }
                $http.post(serverPrefix + "/chat/messages/update", messageObj);
                clearMessagesAndFiles();
            }

            var resetMessageInputToDefaultMode = function() {
                StateFactory.setMessageInputDefaultMode();
            }

            var sendMessageResFunction = function(files, textOfMessage, messageIdToUpdate) {
                var needUpdateMessage = messageIdToUpdate != null;
                if (files != null && files.length > 0) {
                    uploadXhr(files, "upload_file/" + RoomsFactory.getCurrentRoom().roomId,
                        function successCallback(data) {
                            $scope.uploadProgress = 0;
                            if (needUpdateMessage) {
                                updateMessage(messageIdToUpdate, textOfMessage, JSON.parse(data))
                            } else {
                                $scope.sendMessage(textOfMessage, JSON.parse(data), true);
                            }

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
                    if (needUpdateMessage) {
                        updateMessage(messageIdToUpdate, textOfMessage, undefined)
                    } else {
                        $scope.sendMessage(textOfMessage, undefined, true);
                    }
                }
                resetMessageInputToDefaultMode();
            }

            $scope.isMessageEditingMode = function(){
                 return StateFactory.isMessageInputOldMessageEditingMode();
            }

            $scope.sendMessageAndFiles = function() {

                var files = $scope.files;
                var textOfMessage = $scope.newMessage.value == null || $scope.newMessage.value.length < 1 ? " " : $scope.newMessage.value;


                if (UserFactory.isTemporaryGuest()) {
                    $http.post(serverPrefix + "/chat/persist_temporary_guest", textOfMessage).then(function(response) {
                        UserFactory.initStompClient(function() {
                            sendMessageResFunction(files, textOfMessage);
                        });
                    }, function() {
                        console.log('temporary guest user activation failed');
                    });
                    return;
                }
                if (StateFactory.isMessageInputOldMessageEditingMode()) {
                    sendMessageResFunction(files, textOfMessage, StateFactory.getEditingMessageId());
                } else {
                    sendMessageResFunction(files, textOfMessage);
                }
                return false;
            }

            $scope.keyPress = function(event) {
                $scope.startTyping(event);
                var isPhone = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
                if (event.keyCode == 13 && !event.shiftKey && !isPhone) {
                    event.stopPropagation();
                    event.target.blur();
                    setTimeout(function() {
                        $scope.sendMessageAndFiles();
                    }, 0);
                }
                if (event.keyCode == 13 && isPhone) {
                    if (getSelection().modify) { /* chrome */
                        var selection = window.getSelection(),
                            range = selection.getRangeAt(0),
                            br = document.createElement('br');
                        range.deleteContents();
                        range.insertNode(br);
                        range.setStartAfter(br);
                        range.setEndAfter(br);
                        range.collapse(false);
                        selection.removeAllRanges();
                        selection.addRange(range); /* end chrome */
                    } else {
                        document.createTextNode('\n'); /* internet explorer */
                        var range = getSelection().getRangeAt(0);
                        range.surroundContents(newline);
                        range.selectNode(newline.nextSibling); /* end Internet Explorer 11 */
                    }
                }

            }

            function loadRatings() {
                return $http.get(serverPrefix + "/getRatings");
            }
            $scope.addRatingsButtonShowed = true;
            $scope.addRatings = function(close) {
                //close();
                $scope.addRatingsButtonShowed = false;
                var roomObj = RoomsFactory.getCurrentRoom();
                var chatUserId = UserFactory.getChatUserId();
                var ratings = $scope.ratings;
                var results = {};
                for (var rating of ratings) {
                    if (rating.value == undefined)
                        ratings.value = 0;
                    results[rating.id] = rating.value;
                }
                $http.post(serverPrefix + "/addRatingByRoom/" + roomObj.roomId, results).then(function() {
                    toaster.pop('success', lgPack.ratingModal.successTitle, lgPack.ratingModal.success, 6000);
                }, function() {
                    toaster.pop('error', lgPack.ratingModal.errorTitle, lgPack.ratingModal.error, 6000);
                });
            }
            $scope.askForRatingEnabled = true;
            $scope.$on("RoomChanged", function(event, args) {
                $scope.askForRatingEnabled = true;
                $scope.addRatingsButtonShowed = true;
            });
            $scope.askForRating = function() {
                $scope.askForRatingEnabled = false;
                loadRatings().then(function(response) {

                    $scope.ratings = response.data;
                    for (var i = 0; i < $scope.ratings.length; i++) {
                        $scope.ratings[i].value = 10;
                    }
                    $http.get('askForRatingModal.html').then(function(response) {
                        var messageObj = {};
                        messageObj.body = response.data;
                        messageObj.active = true;
                        messageObj.author = {
                            id: 1,
                            nickName: "Server",
                            avatar: "noname.png"
                        };
                        messageObj.attachedFiles = [];
                        messageObj.date = new Date().getTime();
                        RoomsFactory.calcPositionPush(messageObj);
                    }, function(error) {});

                }, function() {})
                /*loadRatings().then(function(response) {
                    $scope.ratings = response.data;
                    ngDialog.open({
                        template: 'askForRatingModal.html',
                        scope: $scope
                    });
                }, function() {})*/

            }


            $scope.startTyping = function(event) { //@Deprecated functionality
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
                if (needSend) {
                    ChatSocket.send("/topic/{0}/chat.typing".format(RoomsFactory.getCurrentRoom().roomId), {}, JSON.stringify({
                        username: UserFactory.getChatUserId(),
                        typing: true
                    }));
                }
            };

            $scope.stopTyping = function() {
                if (angular.isDefined(typing)) {
                    $timeout.cancel(typing);
                    typing = undefined;
                    ChatSocket.send("/topic/{0}/chat.typing".format(RoomsFactory.getCurrentRoom().roomId), {}, JSON.stringify({
                        username: UserFactory.getChatUserId(),
                        typing: false
                    }));

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
                    scope.callback({
                        'oldSize': scope.__height,
                        'newSize': elem.height()
                    });
                }
                scope.__height = elem.height();
            });
        }
    }

});
angular.module('springChat.directives').directive('roomsBlockMini', ['$http', 'RoomsFactory', 'ChannelFactory', 'UserFactory', 'StateFactory', roomsBlockMini]);
var roomsBlockFilter = function(RoomsFactory, RoomsBlockTabState) {
    return function(fields, state) {
        if (fields) { // added check for safe code
            var arrayFields = [];
            if (state == RoomsBlockTabState.lastrooms) {
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
angular.module('springChat.directives').directive('roomsBlock', ['$http', 'RoomsFactory', 'ChannelFactory', 'UserFactory', '$timeout', 'StateFactory', roomsBlock]).filter('roomsBlockFilter', ['RoomsFactory', 'RoomsBlockTabState', roomsBlockFilter]);

var roomsBlockLinkFunction;
var initRoomsFunctions;

function roomsBlockMini($http, RoomsFactory, ChannelFactory, UserFactory, StateFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/rooms_block_mini.html',
        link: function($scope, element, attributes) {
            roomsBlockLinkFunction($scope, element, attributes, $http, RoomsFactory, ChannelFactory, UserFactory, StateFactory)
        }

    };
};

function roomsBlock($http, RoomsFactory, ChannelFactory, UserFactory, $timeout, StateFactory) {
    //TODO finish rooms search
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/rooms_block.html',
        link: function($scope, element, attributes) {
            roomsBlockLinkFunction($scope, element, attributes, $http, RoomsFactory, ChannelFactory, UserFactory, StateFactory);
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
            var updatingUsersByEmailPromise = undefined;
            $scope.updateChatUsersByEmail = function(email, delay) {
                $timeout.cancel(updatingUsersByEmailPromise);
                $scope.searchingRunning = false;
                if (email == null || email.length < 1) {
                    updatingUsersByEmailPromise = undefined;
                    $scope.usersListSearched = [];
                    return;
                }

                updatingUsersByEmailPromise = $timeout(function() {
                    $scope.searchingRunning = true;
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
                $timeout.cancel(updatingUsersByEmailPromise);
                if (query == null || query.length < 1) {
                    updatingUsersByEmailPromise = undefined;
                    $scope.roomsListSearched = [];
                    return;
                }
                updatingUsersByEmailPromise = $timeout(function() {
                    $scope.searchingRunning = true;
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



angular.module('springChat.directives').directive('notificable', ['$timeout', '$templateRequest', '$sce', '$compile', '$parse', 'UserFactory', 'RoomsFactory', notificable]);

function notificable($timeout, $templateRequest, $sce, $compile, $parse, UserFactory, RoomsFactory) {
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
                    case 'BIRTHDAY':

                        RoomsFactory.goToPrivateDialog(item.params.chatId, true);
                        break;
                    case 'alert':
                        alertHandler();
                        break;
                }
                $timeout(function() {
                    UserFactory.removeNotificationByValue(item);
                }, 500)

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
                    $(element).toggleClass('shown');
                }
                $compile(container.html(template).contents())(scope);
            }, function() {
                // An error has occurred
            });

        }
    };
};


initRoomsFunctions = function($scope, ChannelFactory, UserFactory, RoomsFactory, StateFactory) {

    function addNewUser(chatUserId) {
        RoomsFactory.addUserToRoom(chatUserId);
    }
    $scope.addNewUser = addNewUser;

    $scope.getOpponentIdFromRoom = function getOpponentIdFromRoom(room) {
        if (room.privateUserIds == null) return null;
        var currentUserId = UserFactory.getChatUserId();
        if (room.privateUserIds[0] == currentUserId)
            return room.privateUserIds[1];
        if (room.privateUserIds[1] == currentUserId)
            return room.privateUserIds[0];
    }

    $scope.clickToRoomEvent = function(room) {
        if ($scope.isDefaultRoomBlockMode()) {
            $scope.doGoToRoom(room.roomId);
        }
    }
    $scope.clickToUserEvent = function(user) {
        switch ($scope.getRoomBlockMode()) {
            case 1:
                $scope.onFriendClick(user);
                break;
            case 2:
                StateFactory.setDefaultRoomBlockMode();
                return;
                /*addNewUser(user.chatUserId);
                $scope.mode = 1;
                break;*/
        }
        $scope.$root.hideMenu();
    }

    $scope.doGoToRoom = function(roomId) {
        if (StateFactory.isCreateRoomBlockMode())
            return;
        $scope.loadOnlyFilesInfiniteScrollMode = false;
        $scope.loadOnlyBookmarkedInfiniteScrollMode = false;
        ChannelFactory.changeLocation('/dialog_view/' + roomId);
    }

    StateFactory.setDefaultRoomBlockMode();
}
roomsBlockLinkFunction = function($scope, element, attributes, $http, RoomsFactory, ChannelFactory, UserFactory, StateFactory) {
    $scope.isRoomPrivate = RoomsFactory.isRoomPrivate;
    $scope.getSearchLocalizationForCurrentTab = function() {
       return StateFactory.isTabStateContacts() ? lgPack.tooltip.search.contact :  lgPack.tooltip.search.dialogname;
    }
    //$scope.isRoomConsultation = RoomsFactory.isRoomConsultation;
    $scope.lgPack = lgPack;
    $scope.otherRoomsLoaded = false;
    $scope.isRoomCreateEnabled = StateFactory.isCreateRoomBlockMode;
    $scope.isTabStateContacts = StateFactory.isTabStateContacts;
    $scope.isTabStateLastRooms = StateFactory.isTabStateLastRooms;
    $scope.getTabState = StateFactory.getTabState;

    $scope.getRoomBlockMode = StateFactory.getRoomBlockMode;
    $scope.isDefaultRoomBlockMode = StateFactory.isDefaultRoomBlockMode;
    $scope.isCreateRoomBlockMode = StateFactory.isCreateRoomBlockMode;
    //$scope.setRoomBlockMode = StateFactory.setRoomBlockMode;

    $scope.loadOtherRooms = function() {
        var url = serverPrefix + "/chat/rooms/all";
        $http.get(url).success(function(data) {
            RoomsFactory.setRooms(data);
            $scope.otherRoomsLoaded = true;
        });
    }
    $scope.myValueFunction = function(room) {
        if (StateFactory.isTabStateContacts()) {
            return !$scope.isInterlocutorOnline(room) && room.string;
        } else {
            if (room.nums != 0)
                return -Number.MAX_VALUE + room.nums;

            if (room.lastMessageDate == null)
                return 99999999999999;
            return room.lastMessageDate * -1;

            //return new Date(room.date).getTime() * -1;
        }

    };


    $scope.isUserOnline = UserFactory.isUserOnline;
    $scope.rooms = RoomsFactory.getRooms;
    $scope.searchEnabled = false;
    $scope.getCurrentRoom = RoomsFactory.getCurrentRoom;
    $scope.isAddUserToDialogRoomBlockMode = StateFactory.isAddUserToDialogRoomBlockMode;
    $scope.canBeAddedToRoom = function(room) {
        //TODO check if user can be added to room
        if (StateFactory.isCreateRoomBlockMode()) return true;
        var opponentUserId = $scope.getOpponentIdFromRoom(room);
        if (opponentUserId == null) return null;
        return (StateFactory.isAddUserToDialogRoomBlockMode()) && !RoomsFactory.containsUserId(opponentUserId);
    };
    $scope.canBeUserAddedToRoom = function(userId) {
        var currentUserId = UserFactory.getChatUserId();
        if (userId == null || userId == currentUserId) return false;
        if (StateFactory.isCreateRoomBlockMode()) return true;
        return (StateFactory.isAddUserToDialogRoomBlockMode()) && !RoomsFactory.containsUserId(userId);
    }
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
    initRoomsFunctions($scope, ChannelFactory, UserFactory, RoomsFactory, StateFactory);

    var roomsBlockModeChangeSubscription;
    $scope.$on('$destroy', function() {
        roomsBlockModeChangeSubscription();
    });

    roomsBlockModeChangeSubscription = $scope.$root.$on('rootScope:roomsBlockModeChange', function(event, data) {
        StateFactory.setRoomBlockMode(data);
        switch (data) {
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
        RoomsFactory.addDialog($scope.room_create_input, StateFactory.getUserListForAddedToCurrentRoom()).then(function(resp, status, headers, config) {
            $scope.$root.hideMenu();
            ChannelFactory.changeLocation("/dialog_view/" + resp.data);
        });
        return false;
    }

    $scope.toggleCreate = function() {
        StateFactory.toggleCreateRoomBlockMode();
        if (StateFactory.isCreateRoomBlockMode()) {
            $scope.showContacts();
        } else {
            var newUsersArr = StateFactory.getUserListForAddedToCurrentRoom();
            newUsersArr.splice(0, newUsersArr.length);
            $scope.room_create_input = "";
        }
    }

    $scope.canBeRemoved = function(room) {
        if (room.type == 1)
            return false;
        return true;
    }
    $scope.showLastContacts = function() {
        /*if ($scope.mode == 2)
            return;*/
        StateFactory.setTabStateLastRooms();
        $scope.displayLetters = false;
    }
    $scope.showContacts = function() {
        StateFactory.setTabStateContacts();
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

    $scope.toggleForAddUser = function(room) {
        if (room.privateUserIds != undefined) {
            if (room.privateUserIds[0] == UserFactory.getChatUserId())
                addOrRemove(StateFactory.getUserListForAddedToCurrentRoom(), room.privateUserIds[1]);
            if (room.privateUserIds[1] == UserFactory.getChatUserId())
                addOrRemove(StateFactory.getUserListForAddedToCurrentRoom(), room.privateUserIds[0]);
        }
    }
    $scope.cancelAddProcess = function() {
        StateFactory.toggleAddUserToDialogRoomBlockMode();
    }

    $scope.toggleNewUser = function() {
        StateFactory.toggleAddUserToDialogRoomBlockMode();
        if (!StateFactory.isAddUserToDialogRoomBlockMode()) {
            var usersIdsList = StateFactory.getUserListForAddedToCurrentRoom();
            RoomsFactory.addUsersToRoom(usersIdsList);
        }
        return StateFactory.isAddUserToDialogRoomBlockMode();
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

    return {
        restrict: 'EA',
        templateUrl: 'static_templates/file_miniature.html',
        link: function($scope, element, attributes) {
            //TODO filesMiniature
            var link = $parse(attributes.link)($scope);
            if (attributes.removeCallback != null) {
                $scope.removeItemCallback = $parse(attributes.removeCallback)($scope);
                $scope.removable = true;
            }
            //$scope.fileIndex = $parse(attributes.fileIndex)($scope);
            //use only name (not link)
            var nameOnly = typeof attributes.nameonly == "undefined" || attributes.nameonly == 'false' ? false : true;
            var derandomaziedName = nameOnly ? link : getNameFromRandomizedUrl(link);

            var fileName = $parse(attributes.name)($scope);
            if (fileName != undefined) {
                derandomaziedName = fileName;
            }
            var getImageByExtension = function(ext, link) {
                var lowerCaseExtension = ext.toLowerCase();
                var urlTemplate = "images/svg-file-icons/{0}.svg";
                if (isImageExtensions(lowerCaseExtension)) return link;
                if (isExtensionSupported(lowerCaseExtension)) return urlTemplate.format(lowerCaseExtension);
                else return urlTemplate.format('nopreview');
            }
            var isExtensionSupported = function(extension) {
                if (supportedExtensions.indexOf(extension.toLowerCase()) != -1) return true;
                else return false;
            }
            var extension = getFileExtensionByName(derandomaziedName);
            $scope.fileName = derandomaziedName;
            if (!nameOnly && isImageExtensions(extension) == false)
                $scope.link = link;
            $scope.imageSrc = getImageByExtension(extension, link);
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
                var parsedAttrContent = ($parse(attr.content)(scope));
                if (parsedAttrContent == null) return;
                var content = parsedAttrContent.replace(new RegExp("compilable", 'g'), "div");
                content = content.replace(new RegExp("ng-bind", 'g'), "ha");
                //$.ajaxSetup({cache:true});
                element.html(content);
                //$.ajaxSetup({});
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
        replace: true,
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



angular.module('springChat.directives').directive('imgPrev', ['$http', function($http) { //avpr - Audio/Video player/recorder
    return {
        restrict: 'EA',
        link: function($scope, element, attributes) {

            var link = attributes.src;
            if (link == undefined)
                link = attributes.ngSrc;

            var nameOnly = typeof attributes.nameonly == "undefined" || attributes.nameonly == 'false' ? false : true;
            var derandomaziedName = nameOnly ? link : getNameFromRandomizedUrl(link);
            var ext = getFileExtensionByName(derandomaziedName);
            if (isImageExtensions(ext)) {
                $(element[0]).viewer({
                    navbar: false,
                    title: false,
                    toolbar: false,
                    tooltop: false,
                    shown: function() {
                        var viewer = $(this);
                        $(viewer.parent().find(".viewer-canvas")).click(function(event) {
                            if (event.currentTarget != event.target)
                                return;
                            viewer.viewer('hide');
                        })
                    }
                });

                element[0].onclick = function(event) {
                    event.preventDefault();

                    /*
                    var elm = $(".image-directive-preview-container");
                    var img = $(".image-directive-preview-container img");
                    elm.show();
                    img.viewer();
                    img.attr("src", link);
                    setTimeout(function() { elm.addClass("show"); }, 0);
*/
                }
            }
        }
    }
}]); //

angular.module('springChat.directives').directive("skypeUi", ['$parse', function($parse) {
    return {
        restrict: "EA",
        scope: {
            getcontacts: '&'
        },
        replace: true,
        template: '<a ng-show="getContactsString().length>0" href="tel://{{getContactsString()}}?chat"><i class="material-icons">mic</i></a>',
        link: function($scope, element, attrs) {


            $scope.$watch(function() {
                return $scope.getcontacts().length;
            }, function(newValue, oldValue) {
                $scope.contacts = $scope.getContactsString();
            });

            $scope.getContactsString = function() {
                var getContactsFunctionGetter = $scope.getcontacts();
                var getContacts = getContactsFunctionGetter();
                return getContacts.join();
            }



        }
    };
}]);