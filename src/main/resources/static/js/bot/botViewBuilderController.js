'use strict';

springChatControllers.controller('ChatBotViewBuilderController', ['$routeParams', '$rootScope', '$scope', '$window', '$uibModal', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $window, $uibModal, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    BOT_ELEMENTS_MODULE.ElementTypes = ["botrating","botarray", "botinput", "botcheckgroup", "botradiogroup", "bottext", "bot-list", "button", "bot-container", "botlink", "botsubmit", "bot-close"];
    angular.extend(this, $controller('ChatViewBuilderController', { $scope: $scope }));

}]);
