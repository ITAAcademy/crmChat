'use strict';

/* App Module */
var springChat = angular.module('springChat', ['springChat.controllers',//'springChat.controllers',
                                               'springChat.services',
                                               'springChat.directives']);

var longPollChat = angular.module('longPollChat', ['longPollChat.controllers', 'springChat.services',
                                                   'springChat.directives']);


