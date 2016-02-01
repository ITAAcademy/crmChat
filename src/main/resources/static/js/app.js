'use strict';

/* App Module */

 var springChatApp = angular.module('springChat', ['springChat.chatrooms_view',
												'springChat.dialog_view',
                                               'springChat.services',
                                               'springChat.directives']);

var longPollChat = angular.module('longPollChat', ['longPollChat.controllers', 'longPollChat.services',
                                                   'longPollChat.directives']);
springChatApp.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/dialog_view'});
}]);