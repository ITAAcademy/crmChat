var BOT_ELEMENTS_MODULE = function() {
    var publicData = {};

    //Help to prevent mistakes
    var BotElementTypes = ["botinput", "botcheckgroup", "radiogroup", "text", "bot-list", "button", "bot-container", "botlink", "botsubmit", "botClose"];
    var BotGlobalProperties = ["name", "value"];
    var BotElementProperties = {
        "bot-container": ["time", "content", "callback"],
        "bot-list": ["callback"],
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
            object.getHTML = function(scope, ignoreAddedProperties) {
                var have_content = false;
                scope.elementsListForLink.push(this);
                var index = scope.elementsListForLink.length - 1;
                
                var childrensStr = "";
                for (var i = 0; i < this.childrens.length; i++) {
                    var currentStr = this.childrens[i].getHTML(scope, ignoreAddedProperties); 
                    /*var nv = "elementsListForLink[{0}]".format(scope.elementsListForLink.length - 1);//update external file
                    childrensStr += currentStr.split("this").join(nv);*/
                    childrensStr += currentStr;
                }

                var propertiesStr = "";
                for (var key in this.properties) {
                    var value = this.properties[key];
                    if (value == "content") {
                        var escapedValue;
                        if (typeof value === "string") escapedValue =  "'" + childrensStr.escapeHtml() + "'";
                        else escapedValue = "'" + value + "'";
                        propertiesStr += value + '="{0}" '.format(escapedValue);
                    } else
                    if (typeof value != 'undefined') {
                        var escapedValue;
                        if (typeof value === "string") escapedValue = value.escapeHtml();
                        else escapedValue = value;
                        
                    }
                }
                var addedPropertyFinal = ' dnd-placeholder-body = "' + this.type + '" dnd-dragover="$root.dragoverCallback(event, index, external, type, $root.this)" dnd-dragstart = "$root.dragStart($root.this)" dnd-drop="$root.dropCallback(event, index, item, external, type, $root.this)" dnd-list="$root.this.childrens"';
                if(ignoreAddedProperties)
                    addedPropertyFinal = "";
                
                var template = '<li dnd-draggable="$root.this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this">' + 
                '<ul {0} = " " {1}>{2}</ul>'.format(this.type, propertiesStr + " " + addedPropertyFinal, childrensStr) 
                 + "</li>";

                var nv = "elementsListForLink[{0}]".format(index);
                template = template.split("this").join(nv);
                console.log(scope.elementsListForLink[index]);
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
