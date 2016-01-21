/* Directives */

angular.module('springChat.directives', [])
	.directive('printMessage', function () {
	    return {
	    	restrict: 'A',
	        template: '<span ng-show="message.priv">[private] </span><strong>{{message.username}}<span ng-show="message.to"> -> {{message.to}}</span>:</strong> {{message.message}}<br/>'
	       
	    };
	    
	    /*.directive('searchUser', function () {
	    	return function($scope, element, attrs) {
	            /*Задаем функцию, которая будет вызываться при изменении переменной word, ее имя находится в attrs.habraHabr
	            $scope.$watch(attrs.searchUserName,function(value){
	                element.text(value+attrs.habra);
	            });
	        }
	    };*/
});



