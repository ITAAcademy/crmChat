'use strict';

/* App Module */
var springChat = angular.module('springChat', ['springChat.controllers', //'springChat.controllers',
    'springChat.services',
    'springChat.directives'
]);

var longPollChat = angular.module('longPollChat', ['longPollChat.controllers', 'springChat.services',
    'springChat.directives'
]);

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
