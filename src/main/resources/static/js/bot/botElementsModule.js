var BOT_ELEMENTS_MODULE = (function(){
var publicData = {};

	//Help to prevent mistakes
	var BotElementTypes = ["botinput","botcheckgroup","radiogroup","text","botList","button","botContainer","botlink","botsubmit","botClose"];
	var BotGlobalProperties = ["name","value"];
	var BotElementProperties = [
	{"botContainer":["time","content","callback"]},
	{"botList":["content","callback"]},
	{"botlink":["text","ispost","linkindex","href","classes"]},
	{"botinput":["text","linkindex"]},
	{"botsubmit":["text"]},
	{"botcheckgroup":["labels","values","legend","isradio"]},
	{"botClose":[]}
	];
	publicData.ElementProperties = BotElementTypes;
	publicData.ElementTypes = BotElementTypes;
	publicData.GlobalProperties = BotGlobalProperties;


	
    
    publicData.ElementInstance = function(type,properties){
        this.childrens = [];
        if (!$.inArray(type, BotElementTypes) ){
        	console.error('Element type not correct');
        }
        this.type = type;
        this.properties = properties;
        this.getHTML = function (){
    	var propertiesStr = "";
    	for (var key in this.properties)
    	{
    		var value = this.properties[key];
    		if (typeof value != 'undefined'){
    			var escapedValue;
    			if (typeof value === "string")escapedValue = value.escapeHtml();
    			else escapedValue = value;
    		propertiesStr += key +'="{0}" '.format(escapedValue);
    	}
    	}
    	var childrensStr = "";
    	for (var i = 0; i < this.childrens.length; i++){
    		childrensStr += this.childrens[i].getHTML();
    	}
    	var template = "<{0} {1}>{2}</{0}>".format(this.type,propertiesStr,childrensStr);
    	return template;
    };
    }
    var testProperties = {"name":"default"};
	var testElementType = "botcheckgroup";
	var testElementProperties = {"name":"group1","labels":"myarrvar"};
    var getTestElement = function(){
    	return new publicData.ElementInstance(testElementType,testElementProperties);
    }
    var testElm = getTestElement();
    var testElm2 = getTestElement();
    testElm.childrens.push(testElm2);
    console.log('test:'+testElm.getHTML());
    

}());



    