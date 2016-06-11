'use strict';

springChatControllers.controller('ChatBotFormBuilderController', ['$routeParams', '$rootScope', '$scope', '$window', '$uibModal', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $window, $uibModal, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    BOT_ELEMENTS_MODULE.ElementTypes = ["botinput", "botcheckgroup", "botradiogroup", "bottext", "botsubmit", "bot-close"];
    angular.extend(this, $controller('ChatViewBuilderController', { $scope: $scope }));

}]);
