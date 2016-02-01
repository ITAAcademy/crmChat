chatroomsView = angular.module('springChat.chatrooms_view', ['toaster','ngRoute','ngResource','ngCookies']);
chatroomsView.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/chatrooms_view', {
    templateUrl: 'js/chatrooms_view/chatrooms_view.html',
    controller: 'chatroomsViewCtrl'
  });
}])