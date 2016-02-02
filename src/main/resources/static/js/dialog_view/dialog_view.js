dialogView = angular.module('springChat.dialog_view', ['toaster','ngRoute','ngResource','ngCookies']);
dialogView.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/dialog_view/:chatroomId', {
    templateUrl: 'js/dialog_view/dialog_view.html',
    controller: 'dialogViewCtrl'
  });
}])