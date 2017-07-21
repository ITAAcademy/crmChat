'use strict';

/* App Module */
var springChat = angular.module('springChat', ['springChat.controllers', //'springChat.controllers',
    'springChat.services',
    'springChat.directives', 'ngFileUpload', 'angular-content-editable', 'angular-szn-autocomplete', 'ngTouch', 'ngTinyScrollbar'
]);

var springChatControllers = angular.module('springChat.controllers', ['ngTinyScrollbar', 'tooltips', 'ngDialog', 'ngTagsInput', 'infinite-scroll', 'toaster', 'ngRoute', 'ngAnimate', 'ngResource', 'ngCookies', 'ngSanitize']);

springChat.config(['$compileProvider', 'scrollbarProvider', function($compileProvider, scrollbarProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https|http|tel|ftp|mailto|callto|skype):/);
    scrollbarProvider.defaults = {
        axis: 'y', // Vertical or horizontal scrollbar? ( x || y ).
        wheel: true, // Enable or disable the mousewheel;
        wheelSpeed: 40, // How many pixels must the mouswheel scroll at a time.
        wheelLock: true, // Lock default scrolling window when there is no more content.
        scrollInvert: false, // Enable invert style scrolling
        trackSize: false, // Set the size of the scrollbar to auto or a fixed number.
        thumbSize: false, // Set the size of the thumb to auto or a fixed number.
        alwaysVisible: true, // Set to false to hide the scrollbar if not being used
        autoUpdate: true // Autoupdate the scrollbar if DOM changes. Needs MutationObser
    };
}]);

springChat.filter('unique', function($parse) {
    return function(collection, property) {

        collection = angular.isObject(collection) ? toArray(collection) : collection;

        if (!angular.isArray(collection)) {
            return collection;
        }

        //store all unique identifiers
        var uniqueItems = [],
            get = $parse(property);

        return (angular.isUndefined(property))
            //if it's kind of primitive array
            ?
            collection.filter(function(elm, pos, self) {
                return self.indexOf(elm) === pos;
            })
            //else compare with equals
            :
            collection.filter(function(elm) {
                var prop = get(elm);
                if (some(uniqueItems, prop)) {
                    return false;
                }
                uniqueItems.push(prop);
                return true;
            });

        //checked if the unique identifier is already exist
        function some(array, member) {
            if (angular.isUndefined(member)) {
                return false;
            }
            return array.some(function(el) {
                return angular.equals(el, member);
            });
        }
    }
});
if (!String.prototype.format) {
    String.prototype.format = function() {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function(match, number) {
            return typeof args[number] != 'undefined' ?
                args[number] :
                match;
        });
    };
}
if (!String.prototype.escapeHtml) {
    String.prototype.escapeHtml = function() {
        var map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };

        return this.replace(/[&<>"']/g, function(m) { return map[m]; });
    }
}

if (!String.prototype.unescapeHtml) {
    String.prototype.unescapeHtml = function() {
        var map = {
            '&amp;': '&',
            '&lt;': '<',
            '&gt;': '>',
            '&quot;': '"',
            '&#039;': "'",
            '&#123;': '{',
            '&#125;': '}'
        };

        return this.replace(/&amp;|&lt;|&gt;|&quot;|&#039;|&#123;|&#125;/g, function(m) {
            return map[m];
        });
    }
}
if (!String.prototype.escapeQuotes) {
    String.prototype.escapeQuotes = function() {
        var s1 = this.replace(/\"/g, '\\"');
        return s1.replace(/\'/g, "\\'");
    }
}
if (!String.prototype.unescapeQuotes) {
    String.prototype.unescapeQuotes = function() {
        var s1 = this.replace(/\\\"/g, '\"');
        return s1.replace(/\\\'/g, "\'");
    }
}
if (!String.prototype.escapeQuotesHtml) {
    String.prototype.escapeQuotesHtml = function() {
        var map = {
            '"': '&quot;',
            "'": '&#039;'
        };

        return this.replace(/["']/g, function(m) { return map[m]; });
    }
}

if (!String.prototype.unescapeQuotesHtml) {
    String.prototype.unescapeQuotesHtml = function() {
        var map = {
            '&quot;': '"',
            '&#039;': "'"
        };

        return this.replace(/&quot;|&#039;/g, function(m) {
            return map[m];
        });
    }
}