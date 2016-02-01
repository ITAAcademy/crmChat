'use strict';

/* App Module */

var springChat = angular.module('springChat', ['springChat.chatrooms_view',
												'springChat.dialog_view',
                                               'springChat.services',
                                               'springChat.directives']);

var longPollChat = angular.module('longPollChat', ['longPollChat.controllers', 'longPollChat.services',
                                                   'longPollChat.directives']);
springChat.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/dialog_view'});
}]);