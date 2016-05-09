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

function addBotLinkItem(){
	var codeAreaElement = document.getElementById('output_code');
	var index = document.getElementById('botLinkIndex').value;
	var href = document.getElementById('botLinkIndex').value;
	var text = document.getElementById('botLinkText').value;

	var content = "";
	var template = '<div botlink="" href="{0}" ispost="true" classes="btn btn-default" linkindex="{1}" text="{2}"> </div><br>\n';
	content = template.format(href,index,text);
	codeAreaElement.value += content;
}
function addBotInputItem(){
	var codeAreaElement = document.getElementById('output_code');
	var index = document.getElementById('botInputIndex').value;
	var text = document.getElementById('botInputText').value;
	var content = "";
	var template = '<div botinput="" itemIndex="{0}" text="{1}"> </div><br>\n';
	content = template.format(index,text);
	codeAreaElement.value += content;
}