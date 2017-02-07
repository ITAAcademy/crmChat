'use strict';

forumBuilderControllers.controller('ChatBotFormBuilderController', ['$routeParams', '$rootScope', '$scope', '$window', '$uibModal', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore', '$q', '$controller', function($routeParams, $rootScope, $scope, $window, $uibModal, $http, $location, $interval, $cookies, $timeout, toaster, $cookieStore, $q, $controller) {

    BOT_ELEMENTS_MODULE.ElementTypes = ["botinput", "botcheckgroup", "botradiogroup", "bottext", "botsubmit", "bot-close"];
    angular.extend(this, $controller('ChatViewBuilderController', { $scope: $scope }));

}]);
