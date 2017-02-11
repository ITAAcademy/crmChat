var chatIntITAMessenger = angular.module('chatIntITAMessenger', []).directive('itaMessenger', ['$http', '$sce', function($http, $sce) {
    return {
        restrict: 'EA',
        scope: {
            path: "@"
        },
        /*templateUrl: function(element, attributes) {
         return $sce.trustAsResourceUrl(attributes.path + "/static_templates/itaMessegger.html");
         },*/
        template: '<link href=\"https:\/\/fonts.googleapis.com\/icon?family=Material+Icons\" rel=\"stylesheet\" \/>\r\n<style type=\"text\/css\">\r\n.dnd-container {\r\n    position: fixed;\r\n    z-index: 99999999;\r\n    top: 0px;\r\n    left: 0px;\r\n    width: 100%;\r\n    height: 100%;\r\n    transform: none;\r\n    pointer-events: none;\r\n}\r\n\r\n.dnd-container * {\r\n    box-sizing: border-box;\r\n}\r\n\r\n.dnd-container .chat {\r\n    width: 100%;\r\n    height: 100%;\r\n    max-height: 600px;\r\n    max-width: 400px;\r\n    top: calc(100% - 600px);\r\n    left: calc(100% - 400px);\r\n    bottom: 0;\r\n    right: 0;\r\n    position: absolute;\r\n    transform: none;\r\n    pointer-events: all;\r\n    -webkit-transition: all 1.0s ;\r\n    -moz-transition: all 1.0s ;\r\n    -o-transition: all 1.0s ;\r\n    transition: all 1.0s ;\r\n    -webkit-transition-property: top, bottom, left, right;\r\n    transition-property: top, bottom, left, right;\r\n    -webkit-transform: translateZ(0);\r\n    -moz-transform: translateZ(0);\r\n    -ms-transform: translateZ(0);\r\n    -o-transform: translateZ(0);\r\n    transform: translateZ(0);\r\n    margin: 0;\r\n    height: 600px !important;\r\n    width: 400px !important;\r\n    max-height: 100%;\r\n    max-width: 100%;\r\n}\r\n\r\n.dnd-container .chat.normal {\r\n    -webkit-transition-property: all;\r\n    transition-property: all;\r\n}\r\n\r\n.dnd-container .chat.mini {\r\n    max-height: 65px;\r\n    max-width: 220px;\r\n    \/*top: calc(100% - 53px) !important;\r\n    left: calc(100% - 300px) !important;*\/\r\n}\r\n\r\n.dnd-container .chat.full {\r\n    max-height: 100%;\r\n    max-width: 950px;\r\n    height: 100% !important;\r\n    width: 100% !important;\r\n    right: 0px !important;\r\n    bottom: 0px !important;\r\n    top: 0 !important;\r\n    left: 0 !important;\r\n    margin: auto;\r\n    -webkit-transition-property: none;\r\n    transition-property: none;\r\n}\r\n\r\n.dnd-container .chat.disable-animation {\r\n    -webkit-transition: initial;\r\n    -moz-transition: initial;\r\n    -o-transition: initial;\r\n    transition: initial;\r\n}\r\n\r\n.dnd-container .chat .logo {\r\n    background-color: white;\r\n    background-image: url(https:\/\/qa.intita.com\/images\/mainpage\/Logo_small.png);\r\n    background-repeat: no-repeat;\r\n    background-size: contain;\r\n    width: 110px;\r\n    height: 30px;\r\n    position: absolute;\r\n    top: 18px;\r\n    left: 25px;\r\n    max-width: 0;\r\n}\r\n\r\n.dnd-container .chat.mini .logo {\r\n    max-width: 100%;\r\n}\r\n\r\n.window_panel > * {\r\n    display: inline-block;\r\n    width: 20px;\r\n    height: 35px;\r\n    color: #fff;\r\n    line-height: 35px;\r\n}\r\n\r\n.window_panel {\r\n    position: absolute;\r\n    top: 15px;\r\n    right: 10px;\r\n    width: 60px;\r\n    height: 35px;\r\n    background: none;\r\n    color: #fff;\r\n    text-align: center;\r\n}\r\n\r\n.dnd-container .chat .handle {\r\n    position: absolute;\r\n    top: 15px;\r\n    left: 50px;\r\n    width: calc(100% - 160px);\r\n    height: 35px;\r\n    background: none;\r\n    color: #fff;\r\n    cursor: move;\r\n}\r\n\r\n.dnd-container .chat.mini .handle {\r\n    cursor: pointer;\r\n    width: calc(100% - 5px);\r\n}\r\n\r\n.dnd-container .chat.full .handle {\r\n    pointer-events: none;\r\n}\r\n\r\n.dnd-container .chat.mini .window_panel {\r\n    display: none;\r\n}\r\n\r\n.dnd-container .chat.full .window_panel {\r\n    top: 15px;\r\n    right: 10px;\r\n}\r\n\r\n.dnd-container .material-icons {\r\n    -webkit-transition: color .25s ease-in-out;\r\n    -moz-transition: color .25s ease-in-out;\r\n    -o-transition: color .25s ease-in-out;\r\n    transition: color .25s ease-in-out;\r\n    font-size: 20px;\r\n}\r\n\r\n.dnd-container .material-icons:hover {\r\n    cursor: pointer;\r\n    color: #a3c6e2;\r\n}\r\n\r\n.disable-mouse {\r\n    pointer-events: none;\r\n}\r\n\r\n@media only screen and (min-width:900px) {\r\n    .dnd-container .chat.full .window_panel {\r\n        top: 15px;\r\n        right: 25px;\r\n    }\r\n}\r\n\r\n@media only screen and (max-width:600px) {\r\n  \/*  .dnd-container .chat {\r\n     min-width: 100% !important;\r\n     max-width: 100% !important;\r\n }*\/\r\n  \/*  .dnd-container .chat {\r\n        max-height: 100%;\r\n        max-width: 950px;\r\n        height: 100% !important;\r\n        width: 100% !important;\r\n        right: 0px !important;\r\n        bottom: 0px !important;\r\n        top: 0 !important;\r\n        left: 0 !important;\r\n        margin: auto;\r\n        -webkit-transition-property: none;\r\n        transition-property: none;\r\n    }*\/\r\n}\r\n<\/style>\r\n<div id= \"chat_mini\" ng-show=\"init\" class=\"draggable disable-animation chat mini ng-class:{mini: state==1, full: state==2}\" ng-click=\"\">\r\n    <iframe style=\"width: 100%;height: 100%;border: none;\" ng-src=\"{{crm_path}}\"><\/iframe>\r\n    <div class=\"window_panel ignore\" style=\"\">\r\n        <div id=\"minimize_btn\" class=\"material-icons\" ng-click=\"minimizete()\">remove<\/div>\r\n        <div id=\"fullscreen_btn\" class=\"material-icons\" ng-click=\"fullScreen()\">web_asset<\/div>\r\n    <\/div>\r\n    <div class=\"handle\" ng-mouseup=\"minimizeteMin()\" style=\"\"><\/div>\r\n<\/div>\r\n',
        link: function($scope, element, attributes) {
            $scope.crm_path = $sce.trustAsResourceUrl($scope.path)
            var busy = false;
            $scope.dragstart = function() {
                $scope.$apply(function() {
                    busy = true;
                });
                console.log('dragstart', arguments);
                var res_elem = jQuery('.draggable');
                res_elem.addClass("disable-animation");
                jQuery("iframe").addClass("disable-mouse");
            };

            $scope.dragend = function() {
                console.log('dragend', arguments);
                if (!arguments[0]) this.dropped = false;
                var res_elem = jQuery('.draggable');
                res_elem.removeClass("disable-animation");
                jQuery("iframe").removeClass("disable-mouse");
                var res_elem = jQuery('.draggable');
                localStorage.setItem("chatX", res_elem.css("left"));
                localStorage.setItem("chatY", res_elem.css("top"));

                $scope.$apply(function() {
                    busy = false;
                });
            };

            $scope.minimizeteMin = function() {
                if ($scope.state == 1 && busy == false) {
                    $scope.state = 0;
                    return;
                }
            }
            $scope.minimizete = function() {
                if ($scope.state == 1) {
                    $scope.state = 0;
                    return;
                }
                $scope.state = 1;
            }
            $scope.fullScreen = function() {
                if ($scope.state == 1) {
                    $scope.state = 0;
                    return;
                }
                if ($scope.state == 2) {
                    $scope.state = 0;
                    return;
                }
                $scope.state = 2;

            }

            var elem = jQuery(".dnd-container");
            var res_elem = jQuery('.draggable');



            var reinitElemPos = function() {
                var elem = jQuery(".dnd-container");
                var res_elem = jQuery('.draggable');
                var x = localStorage.getItem("chatX");
                var y = localStorage.getItem("chatY");
                if (x == undefined || y == undefined) {
                    var elem = jQuery(".dnd-container");
                    res_elem.css({ top: (elem.height() - 600) + 'px' });
                    res_elem.css({ left: (elem.width() - 400) + 'px' });
                } else {
                    var offset = res_elem.position();
                    console.log(elem.height() < parseInt(y) + res_elem.height() || parseInt(y) < 0 || elem.width() < parseInt(x) + res_elem.width() || parseInt(x) < 0);
                    if (elem.height() < parseInt(y) + res_elem.height() || parseInt(y) < 0 || elem.width() < parseInt(x) + res_elem.width() || parseInt(x) < 0) {
                        if (elem.height() < parseInt(y) + res_elem.height() || parseInt(y) < 0) {
                            res_elem.css({ top: 'initial', bottom : "0px" });
                        }
                        if (elem.width() < parseInt(x) + res_elem.width() || parseInt(x) < 0) {
                            res_elem.css({ left: 'initial', right : "0px" });
                        }
                    } else {
                        res_elem.css({ left: x });
                        res_elem.css({ top: y });
                    }

                }
                return true;
            }

            jQuery(document).ready(reinitElemPos);
            jQuery(window).resize(reinitElemPos);

            var myElement =  document.getElementById('chat_mini');
            addResizeListener(myElement, reinitElemPos);


            jQuery(document).ready(function() {
                $scope.state = 0;
                var elem = jQuery(".dnd-container");
                var res_elem = jQuery('.draggable');


                $scope.$apply(function() {
                    //   jQuery('.draggable').bind( 'resize', reinitElemPos);
                    $scope.$watch('state', function() {
                        localStorage.setItem("chatState", $scope.state);
                        var res_elem = jQuery('.draggable');
                        if ($scope.state == 2) {
                            res_elem.removeClass("normal");
                        } else {
                            setTimeout(function() {
                                reinitElemPos();
                                res_elem.addClass("normal");
                            }, 600);
                        }
                    })

                    if (localStorage.getItem("chatState") == undefined) {
                        $scope.state = 0;
                        jQuery(".chat").removeClass("mini");
                    } else {
                        $scope.state = parseInt(localStorage.getItem("chatState"));
                        if ($scope.state != 1) {
                            {
                                jQuery(".chat").removeClass("mini");
                                reinitElemPos();
                            }
                        }
                    }

                    setTimeout(function() { res_elem.removeClass("disable-animation"); }, 100);
                    jQuery(".chat").draggable({ /*handle: ".handle"*/ cancel: ".ignore", containment: "parent", start: $scope.dragstart, stop: $scope.dragend });
                    $scope.init = true;
                });
                reinitElemPos();
            });
        }
    };
}]);


(function() {
    var attachEvent = document.attachEvent;
    var isIE = navigator.userAgent.match(/Trident/);
    console.log(isIE);
    var requestFrame = (function() {
        var raf = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame ||
            function(fn) {
                return window.setTimeout(fn, 20); };
        return function(fn) {
            return raf(fn); };
    })();

    var cancelFrame = (function() {
        var cancel = window.cancelAnimationFrame || window.mozCancelAnimationFrame || window.webkitCancelAnimationFrame ||
            window.clearTimeout;
        return function(id) {
            return cancel(id); };
    })();

    function resizeListener(e) {
        var win = e.target || e.srcElement;
        if (win.__resizeRAF__) cancelFrame(win.__resizeRAF__);
        win.__resizeRAF__ = requestFrame(function() {
            var trigger = win.__resizeTrigger__;
            trigger.__resizeListeners__.forEach(function(fn) {
                fn.call(trigger, e);
            });
        });
    }

    function objectLoad(e) {
        this.contentDocument.defaultView.__resizeTrigger__ = this.__resizeElement__;
        this.contentDocument.defaultView.addEventListener('resize', resizeListener);
    }

    window.addResizeListener = function(element, fn) {
        if (!element.__resizeListeners__) {
            element.__resizeListeners__ = [];
            if (attachEvent) {
                element.__resizeTrigger__ = element;
                element.attachEvent('onresize', resizeListener);
            } else {
                if (getComputedStyle(element).position == 'static') element.style.position = 'relative';
                var obj = element.__resizeTrigger__ = document.createElement('object');
                obj.setAttribute('style', 'display: block; position: absolute; top: 0; left: 0; height: 100%; width: 100%; overflow: hidden; pointer-events: none; z-index: -1;');
                obj.__resizeElement__ = element;
                obj.onload = objectLoad;
                obj.type = 'text/html';
                if (isIE) element.appendChild(obj);
                obj.data = 'about:blank';
                if (!isIE) element.appendChild(obj);
            }
        }
        element.__resizeListeners__.push(fn);
    };

    window.removeResizeListener = function(element, fn) {
        element.__resizeListeners__.splice(element.__resizeListeners__.indexOf(fn), 1);
        if (!element.__resizeListeners__.length) {
            if (attachEvent) element.detachEvent('onresize', resizeListener);
            else {
                element.__resizeTrigger__.contentDocument.defaultView.removeEventListener('resize', resizeListener);
                element.__resizeTrigger__ = !element.removeChild(element.__resizeTrigger__);
            }
        }
    }
})();
