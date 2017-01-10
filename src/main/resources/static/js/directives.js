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
angular.module('springChat.directives').directive("imagedrop", function($parse, $document) {
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
});

angular.module('springChat.directives').directive('autoGrow', function() {
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
});

angular.module('springChat.directives').directive('dir', function($compile, $parse) {
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
})


angular.module('springChat.directives').directive('modaleToggle', function($compile, $parse) {
    return {
        restrict: 'EA',
        scope: {
            callback: '&callback',
            ignoreId: '@ignoreId'
        },
        link: function(scope, element, attr) {
            var toggle = false;
            if (scope.callback != null) {
                $(window).click(function(e) {
                    if (e.target.style.pointerEvents == "none")
                        return;
                    var ignoredElement = document.getElementById(scope.ignoreId);
                    
                    if (e.target === element[0] || element[0].contains(e.target) || (toggle && e.target != ignoredElement && !ignoredElement.contains(e.target))) {
                        scope.$apply(function() {
                            scope.callback();
                            toggle = !toggle;
                        });

                    }
                    //alert(e.target === element[0] || element[0].contains(e.target));
                });
            }
        }
    }
})


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
        console.log('updateModelGet():requestUrl:' + requestUrl + " failed");
    });
};





angular.module('springChat.directives').directive('tenantsBlock', function($http, mySettings, UserFactory) {
    return {
        restrict: 'EA',
        scope: {

        },
        templateUrl: 'static_templates/participants_block.html',
        link: function(scope, element, attributes) {
            updateModelForStudents();
            initFolded(scope, element);

            function updateModelForStudents() {
                scope.participants = UserFactory.getTenantsList()
            };
            scope.blockName = "Тенанти";
        }

    };
});


angular.module('springChat.directives').directive('studentsBlock', studentsBlock);


function initFolded(scope, element) {
    scope.scroll;
    scope.folded = true;
    scope.toggleFolded = function() {
        scope.folded = !scope.folded;
        scope.scroll.overflowy = !scope.folded;
        if (scope.folded)
            scope.scroll.scrollTop(scope.scroll.getScrollTop())
    }
    scope.scroll = $($(element).find(".scroll"));
    scope.scroll.overflowy = !scope.folded;
}

function studentsBlock($http, mySettings, RoomsFactory) {
    return {
        restrict: 'EA',
        scope: {

        },
        templateUrl: 'static_templates/students_block.html',
        link: function(scope, element, attributes) {
            updateModelForStudents();
            initFolded(scope, element);

            function updateModelForStudents() {
                updateModelGet($http, "chat/get_students/", function(responseObj) {
                    scope.students = responseObj.data;
                });
            };
            scope.blockName = "Студенти";
            scope.goToPrivateDialog = RoomsFactory.goToPrivateDialog;
        }

    };
};

angular.module('springChat.directives').directive('participantsBlock', participantsBlock);

function participantsBlock($http, mySettings, RoomsFactory, UserFactory) {
    return {
        restrict: 'EA',
        scope: {

        },
        templateUrl: 'static_templates/participants_block.html',
        link: function(scope, element, attributes) {
            function updateModelForParticipants() {

            };
            scope.participants = RoomsFactory.getParticipants;
            scope.blockName = "Учасники розмови";
            scope.currentRoom = RoomsFactory.getCurrentRoom;
            scope.getChatUserId = UserFactory.getChatUserId;
            scope.checkUserAdditionPermission = function() {
                return RoomsFactory.checkUserAdditionPermission(UserFactory.getChatUserId());
            }
            scope.removeUserFromRoom = RoomsFactory.removeUserFromRoom;
            initFolded(scope, element);
        }

    };
};



angular.module('springChat.directives').directive('messagesBlock', messagesBlock);

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

angular.module('springChat.directives').directive('roomsBlock', roomsBlock);

function roomsBlock($http, RoomsFactory, ChannelFactory) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/rooms_block.html',
        link: function($scope, element, attributes) {
            $scope.rooms = RoomsFactory.getRooms;
            $scope.searchEnabled = false;
            $scope.getCurrentRoom = RoomsFactory.getCurrentRoom;
            $scope.toggleSearch = function() {
                $scope.searchEnabled = !$scope.searchEnabled;
            }

            $scope.doGoToRoom = function(roomId) {
                //if ($scope.mouseBusy == false)
                ChannelFactory.changeLocation('/dialog_view/' + roomId);
            }
            var nice = $(".scroll");
        }

    };
};



angular.module('springChat.directives').directive('fileMiniature', fileMiniature);

function fileMiniature($http, RoomsFactory, ChannelFactory,$parse) {
    return {
        restrict: 'EA',
        templateUrl: 'static_templates/file_miniature.html',
        link: function($scope, element, attributes) {
          //TODO filesMiniature
          var supportedExtensions = ['aac',
          'ai',
          ,'aiff',
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
            var getFileExtensionByName = function(name){
                return name.split('.').pop();
            }
            var isExtensionSupported = function (extension){
                if (supportedExtensions.indexOf(extension)!=-1) return true;
                else return false;
            }
            var getImageByExtension = function(ext){
                var urlTemplate = "images/svg-file-icons/{0}.svg";
                if (isExtensionSupported(ext)) return urlTemplate.format(ext);
                else return urlTemplate.format('nopreview');
            }

            var link = $parse(attributes.link)($scope);
            var derandomaziedName = $scope.getNameFromUrl(link);

            var extension = getFileExtensionByName(derandomaziedName);
            $scope.fileName = derandomaziedName;
            $scope.link = link;
            $scope.imageSrc = getImageByExtension(extension);
        }


    };
};

angular.module('springChat.directives').directive('compilable', function($compile, $parse) {
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
})


angular.module('springChat.directives').directive('ngDraggable', function($document) {
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
                debugger;
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
                debugger;
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

})


angular.module('springChat.directives').directive('ngResizeble', function($document) {
    return {
        restrict: 'A',
        scope: {
            resizeOptions: '=ngResizeble'
        },
        link: function(scope, elem, attr) {
            var startX, startY, x = 0,
                y = 0,
                start, stop, resize, container;




            var resizeElement;
            var containerElm;
            // Obtain resize options
            if (scope.resizeOptions) {
                start = scope.resizeOptions.start;
                resize = scope.resizeOptions.resize;
                stop = scope.resizeOptions.stop;
                var id = scope.resizeOptions.container;
                if (id) {
                    container = document.getElementById(id).getBoundingClientRect();
                    containerElm = angular.element(document.getElementById(id));
                }
                resizeElement = angular.element(document.getElementById(scope.resizeOptions.resizeElement));

            }

            // Bind mousedown event
            elem.on('mousedown', function(e) {
                debugger;
                if ($(resizeElement).hasClass("resize-disable") || e.target != elem[0])
                    return true;

                e.preventDefault();
                startX = e.clientX - resizeElement[0].offsetLeft;
                startY = e.clientY - resizeElement[0].offsetTop;
                $document.on('mousemove', mousemove);
                $document.on('mouseup', mouseup);
                if (start) start(e);
            });

            // Handle resize event
            function mousemove(e) {
                debugger;
                y = e.clientY - startY;
                x = e.clientX - startX;
                setPosition();
                if (resize) resize(e);
            }

            // Unbind resize events
            function mouseup(e) {
                $document.unbind('mousemove', mousemove);
                $document.unbind('mouseup', mouseup);
                if (stop) stop(e);
            }

            // Move element, within container if provided
            function setPosition() {
                var width = resizeElement[0].offsetWidth,
                    height = resizeElement[0].offsetHeight;

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

                resizeElement.css({
                    top: y + 'px',
                    left: x + 'px'
                });
            }
        }
    }

})
