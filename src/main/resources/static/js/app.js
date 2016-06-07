'use strict';

/* App Module */
var springChat = angular.module('springChat', ['springChat.controllers', //'springChat.controllers',
    'springChat.services',
    'springChat.directives'
]);

var longPollChat = angular.module('longPollChat', ['longPollChat.controllers', 'springChat.services',
    'springChat.directives'
]);
var springChatControllers = angular.module('springChat.controllers', ['ngTagsInput', 'dndLists', 'monospaced.elastic', 'ui.bootstrap', 'infinite-scroll', 'toaster', 'ngRoute', 'ngAnimate', 'ngResource', 'ngCookies', 'ngSanitize']);

springChat.filter('unique', function() {
    return function(input, key) {
        var unique = {};
        var uniqueList = [];
        for(var i = 0; i < input.length; i++){
            if(typeof unique[input[i][key]] == "undefined"){
                unique[input[i][key]] = "";
                uniqueList.push(input[i]);
            }
        }
        return uniqueList;
    };
});
if (!String.prototype.format) {
	  String.prototype.format = function() {
	    var args = arguments;
	    return this.replace(/{(\d+)}/g, function(match, number) { 
	      return typeof args[number] != 'undefined'
	        ? args[number]
	        : match
	      ;
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
	     '&amp;' : '&',
	     '&lt;' : '<',
	    '&gt;' : '>',
	    '&quot;' : '"' ,
	    '&#039;' : "'",
	    '&#123;' : '{',
	    '&#125;' : '}'
	  };

	  return this.replace(/&amp;|&lt;|&gt;|&quot;|&#039;|&#123;|&#125;/g, function(m) { 
	  	return map[m]; 
	  });
	}
}
if (!String.prototype.escapeQuotes){
	String.prototype.escapeQuotes = function(){
		var s1 = this.replace(/\"/g,'\\"');
		return s1.replace(/\'/g,"\\'");
	}
}
if (!String.prototype.unescapeQuotes){
	String.prototype.unescapeQuotes = function(){
		var s1 = this.replace(/\\\"/g,'\"');
		return s1.replace(/\\\'/g,"\'");
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
	    '&quot;' : '"' ,
	    '&#039;' : "'"
	  };

	  return this.replace(/&quot;|&#039;/g, function(m) { 
	  	return map[m]; 
	  });
	}
}