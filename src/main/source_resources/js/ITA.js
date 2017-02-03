var html = "<link href=\"https:\/\/fonts.googleapis.com\/icon?family=Material+Icons\" rel=\"stylesheet\"\/><style type=\"text\/css\">.dnd-container{position:fixed;z-index:99999999;top:0;left:0;width:100%;height:100%;transform:none;pointer-events:none}.dnd-container *{box-sizing:border-box}.dnd-container .chat{width:100%;height:100%;max-height:600px;max-width:400px;top:calc(100% - 600px);left:calc(100% - 400px);bottom:0;right:0;position:absolute;transform:none;pointer-events:all;-webkit-transition:all .5s ease;-moz-transition:all .5s ease;-o-transition:all .5s ease;transition:all .5s ease;-webkit-transition-property:top,bottom,left,right;transition-property:top,bottom,left,right;-webkit-transform:translateZ(0);-moz-transform:translateZ(0);-ms-transform:translateZ(0);-o-transform:translateZ(0);transform:translateZ(0);margin:0;height:600px!important;width:400px!important;max-height:100%}.dnd-container .chat.normal{-webkit-transition-property:all;transition-property:all}.dnd-container .chat.mini{max-height:65px;max-width:300px}.dnd-container .chat.full{max-height:100%;max-width:950px;height:100%!important;width:100%!important;right:0!important;bottom:0!important;top:0!important;left:0!important;margin:auto;-webkit-transition-property:none;transition-property:none}.dnd-container .chat.disable-animation{-webkit-transition:initial;-moz-transition:initial;-o-transition:initial;transition:initial}.dnd-container .chat .logo{background-color:white;background-image:url(https:\/\/qa.intita.com\/images\/mainpage\/Logo_small.png);background-repeat:no-repeat;background-size:contain;width:110px;height:30px;position:absolute;top:18px;left:25px;max-width:0}.dnd-container .chat.mini .logo{max-width:100%}.window_panel>*{display:inline-block;width:24px;height:35px;color:#fff;line-height:35px}.window_panel{position:absolute;top:15px;right:10px;width:60px;height:35px;background:0;color:#fff;text-align:center}.handle{position:absolute;top:15px;left:50px;width:calc(100% - 120px);height:35px;background:0;color:#fff;cursor:move}.dnd-container .chat.mini .handle{cursor:pointer}.dnd-container .chat.full .handle{pointer-events:none}.dnd-container .chat.mini #minimize_btn{display:none}.dnd-container .chat.full .window_panel{top:15px;right:10px}.material-icons{-webkit-transition:color .25s ease-in-out;-moz-transition:color .25s ease-in-out;-o-transition:color .25s ease-in-out;transition:color .25s ease-in-out}.material-icons:hover{cursor:pointer;color:#a3c6e2}.disable-mouse{pointer-events:none}@media only screen and (min-width:900px){.dnd-container .chat.full .window_panel{top:15px;right:25px}}<\/style><div ng-controller=\"chat-controller as main\" class=\"dnd-container\"><div ng-show=\"init\" class=\"draggable chat mini ng-class:{mini: state==1, full: state==2}\" ng-click=\"\"><iframe style=\"width: 100%;height: 100%;border: none;\" src=\"https:\/\/localhost:8080\/crmChat\"><\/iframe><div class=\"window_panel ignore\" style=\"\"><div id=\"minimize_btn\" class=\"material-icons\" ng-click=\"minimizete()\">indeterminate_check_box<\/div><div id=\"fullscreen_btn\" class=\"material-icons\" ng-click=\"fullScreen()\">web_asset<\/div><\/div><div class=\"handle\" ng-mouseup=\"minimizeteMin()\" style=\"\"><\/div><\/div><\/div><\/body><\/html>"
$("body").append(html);
var app = angular.module('mainApp', []);

app.controller('chat-controller', function($scope) {

    var busy = false;
    $scope.dragstart = function() {
        $scope.$apply(function() {
            busy = true;
        });
        console.log('dragstart', arguments);
        var res_elem = $('.draggable');
        res_elem.addClass("disable-animation");
        $("iframe").addClass("disable-mouse");
    };

    $scope.drag = function() {
        console.log('drag', arguments);
    };

    $scope.dragend = function() {
        console.log('dragend', arguments);
        if (!arguments[0]) this.dropped = false;
        var res_elem = $('.draggable');
        res_elem.removeClass("disable-animation");
        $("iframe").removeClass("disable-mouse");
        var res_elem = $('.draggable');
        localStorage.setItem("chatX", res_elem.css("left"));
        localStorage.setItem("chatY", res_elem.css("top"));

        $scope.$apply(function() {
            busy = false;
        });
    };

    $scope.dragenter = function(dropmodel) {
        debugger;
        console.log('dragenter', arguments);
        this.active = dropmodel;
    };

    $scope.dragover = function() {
        console.log('dragover', arguments);
    };

    $scope.dragleave = function() {
        console.log('dragleave', arguments);
        this.active = undefined;
    };

    $scope.drop = function(dragmodel, model) {
        console.log('drop', arguments);
        this.dropped = model;
    };

    $scope.isDropped = function(model) {
        return this.dropped === model;
    };

    $scope.isActive = function(model) {
        return this.active === model;
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

    function resizeFunc(e) {
        var elem = $(".dnd-container");
        var res_elem = $('.draggable');
        var offset = res_elem.position();
        if (elem.height() < offset.top + res_elem.height() || offset.top < 0) {
            res_elem.css({ top: (elem.height() - res_elem.height()) + 'px' });
        }
        if (elem.width() < offset.left + res_elem.width() || offset.left < 0) {
            res_elem.css({ left: (elem.width() - res_elem.width()) + 'px' });
        }
        localStorage.setItem("chatX", res_elem.css("left"));
        localStorage.setItem("chatY", res_elem.css("top"));
    }
    $(window).resize(resizeFunc);


    //  $(document).ready(resizeFunc);

    $(document).ready(function() {
        $scope.state = 0;
        var res_elem = $('.draggable');
        var x = localStorage.getItem("chatX");
        var y = localStorage.getItem("chatY");
        if (x == undefined || y == undefined) {
            var elem = $(".dnd-container");
            res_elem.css({ top: (elem.height() - 600) + 'px' });
            res_elem.css({ left: (elem.width() - 400) + 'px' });
        } else {
            res_elem.css({ top: y });
            res_elem.css({ left: x });
        }

        $scope.$apply(function() {

            $scope.$watch('state', function() {
                localStorage.setItem("chatState", $scope.state);
                var res_elem = $('.draggable');
                if ($scope.state == 2) {
                    res_elem.removeClass("normal");
                } else {
                    setTimeout(function() {
                        resizeFunc();
                        res_elem.addClass("normal");
                    }, 600);
                }
            })

            if (localStorage.getItem("chatState") == undefined) {
                $scope.state = 0;
                $(".chat").removeClass("mini");
            } else {
                $scope.state = parseInt(localStorage.getItem("chatState"));
                if ($scope.state != 1) {
                    $(".chat").removeClass("mini");
                }
            }
            $(".chat").draggable({ /*handle: ".handle"*/ cancel: ".ignore", containment: "parent", start: $scope.dragstart, stop: $scope.dragend });

            $scope.init = true;
        });

    });
});

