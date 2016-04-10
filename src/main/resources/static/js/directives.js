/* Directives */

angular.module('springChat.directives', [])
.directive('printMessage', function () {
	return {
		restrict: 'A',
		template: '<span ng-show="message.priv">[private] </span><strong>{{message.username}}<span ng-show="message.to"> -> {{message.to}}</span>:</strong> {{message.message}}<br/>'

	};
});
angular.module('springChat.directives').directive("imagedrop", function ($parse, $document) {
	return {
		restrict: "A",
		link: function (scope, element, attrs) {
			var onImageDrop = $parse(attrs.onImageDrop);

			//When an item is dragged over the document
			var onDragOver = function (e) {
				e.preventDefault();
				angular.element('body').addClass("dragOver");
			};

			//When the user leaves the window, cancels the drag or drops the item
			var onDragEnd = function (e) {
				e.preventDefault();
				angular.element('body').removeClass("dragOver");
			};

			//When a file is dropped
			var loadFile = function (files) {
				scope.uploadedFiles = files;
				scope.$apply(onImageDrop(scope));
			};

			//Dragging begins on the document
			$document.bind("dragover", onDragOver);

			//Dragging ends on the overlay, which takes the whole window
			element.bind("dragleave", onDragEnd)
			.bind("drop", function (e) {
				onDragEnd(e);
				loadFile(e.originalEvent.dataTransfer.files);
			});
		}
	};
});

angular.module('springChat.directives').directive('dir', function($compile, $parse) {
    return {
        restrict: 'E',
        link: function(scope, element, attr) {
          scope.$watch(attr.content, function() {
            element.html($parse(attr.content)(scope));
            //element.html =$parse(attr.content)(scope);
            $compile(element.contents())(scope);
          }, true);
        }
      }
    })
angular.module('springChat.directives').directive('starRating', starRating);
function starRating() {
    return {
      restrict: 'EA',
      template:
        '<ul class="star-rating" ng-class="{readonly: readonly}">' +
        '  <li ng-repeat="star in stars" class="star" ng-class="{filled: star.filled}" ng-click="toggle($index)">' +
        '    <i class="fa fa-star"></i>' + // or &#9733
        '  </li>' +
        '</ul>',
      scope: {
        ratingValue: '=ngModel',
        max: '=?', // optional (default is 5)
        onRatingSelect: '&?',
        readonly: '=?'
      },
      link: function(scope, element, attributes) {
        if (scope.max == undefined) {
          scope.max = 5;
        }
        function updateStars() {
          scope.stars = [];
          for (var i = 0; i < scope.max; i++) {
            scope.stars.push({
              filled: i < scope.ratingValue
            });
          }
        };
        scope.toggle = function(index) {
          if (scope.readonly == undefined || scope.readonly === false){
            scope.ratingValue = index + 1;
            scope.onRatingSelect({
              rating: index + 1
            });
            updateStars();
          }
        };
        scope.ratingValue = 0;
        /*scope.$watch('ratingValue', function(oldValue, newValue) {
          if (newValue) {
            updateStars();
          }
        });*/
        updateStars();
      }
    };
  };




