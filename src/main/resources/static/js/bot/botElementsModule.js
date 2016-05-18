var BOT_ELEMENTS_MODULE = function() {
    var publicData = {};

    //Help to prevent mistakes
    var BotElementTypes = ["botinput", "botcheckgroup", "radiogroup", "text", "botList", "button", "bot-container", "botlink", "botsubmit", "botClose"];
    var BotGlobalProperties = ["name", "value"];
    var BotElementProperties = {
        "bot-container": ["time", "content", "callback"],
        "botList": ["content", "callback"],
        "botlink": ["text", "ispost", "linkindex", "href", "classes"],
        "botinput": ["text", "linkindex"],
        "botsubmit": ["text"],
        "botcheckgroup": ["labels", "values", "legend", "isradio"],
        "botClose": []
    };
    publicData.ElementProperties = BotElementProperties;
    publicData.ElementTypes = BotElementTypes;
    publicData.GlobalProperties = BotGlobalProperties;




    publicData.ElementInstance = function(type) {
            var object = new Object();
            object.childrens = [];
            if ($.inArray(type, BotElementTypes) == -1) {
                console.error('Element type not correct');
            }
            object.parent = null;
            object.type = type;
            object.properties = BotElementProperties[type];
            object.addedProperty = "";
            object.getHTML = function() {
                var have_content = false;

                var childrensStr = "";
                for (var i = 0; i < object.childrens.length; i++) {
                    childrensStr += object.childrens[i].getHTML();
                }

                var propertiesStr = "";
                for (var key in object.properties) {
                    var value = object.properties[key];
                    if (value == "content") {
                        var escapedValue;
                        if (typeof value === "string") escapedValue =  "'" + childrensStr.escapeHtml() + "'";
                        else escapedValue = "'" + value + "'";
                        propertiesStr += value + '="{0}" '.format(escapedValue);
                    } /*else
                    if (typeof value != 'undefined') {
                        var escapedValue;
                        if (typeof value === "string") escapedValue = value.escapeHtml();
                        else escapedValue = value;
                        propertiesStr += value + '="{0}" '.format(escapedValue);
                    }*/
                }
                var template = "<{0} {1}>{2}</{0}>".format(object.type, propertiesStr + " " + object.addedProperty, childrensStr);
                return template;
            };
            return object;
        }
        /* var testProperties = { "name": "default" };
         var testElementType = "botcheckgroup";
         var testElementProperties = { "name": "group1", "labels": "myarrvar" };
         var getTestElement = function() {
             return new publicData.ElementInstance(testElementType, testElementProperties);
         }
         var testElm = getTestElement();
         var testElm2 = getTestElement();
         testElm.childrens.push(testElm2);
         console.log('test:' + testElm.getHTML());*/

    return publicData;

}();
