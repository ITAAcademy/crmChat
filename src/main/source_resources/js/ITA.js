(function(t,e){function i(e,i){var n,o,a,r=e.nodeName.toLowerCase();return"area"===r?(n=e.parentNode,o=n.name,e.href&&o&&"map"===n.nodeName.toLowerCase()?(a=t("img[usemap=#"+o+"]")[0],!!a&&s(a)):!1):(/input|select|textarea|button|object/.test(r)?!e.disabled:"a"===r?e.href||i:i)&&s(e)}function s(e){return t.expr.filters.visible(e)&&!t(e).parents().andSelf().filter(function(){return"hidden"===t.css(this,"visibility")}).length}var n=0,o=/^ui-id-\d+$/;t.ui=t.ui||{},t.ui.version||(t.extend(t.ui,{version:"1.9.2",keyCode:{BACKSPACE:8,COMMA:188,DELETE:46,DOWN:40,END:35,ENTER:13,ESCAPE:27,HOME:36,LEFT:37,NUMPAD_ADD:107,NUMPAD_DECIMAL:110,NUMPAD_DIVIDE:111,NUMPAD_ENTER:108,NUMPAD_MULTIPLY:106,NUMPAD_SUBTRACT:109,PAGE_DOWN:34,PAGE_UP:33,PERIOD:190,RIGHT:39,SPACE:32,TAB:9,UP:38}}),t.fn.extend({_focus:t.fn.focus,focus:function(e,i){return"number"==typeof e?this.each(function(){var s=this;setTimeout(function(){t(s).focus(),i&&i.call(s)},e)}):this._focus.apply(this,arguments)},scrollParent:function(){var e;return e=t.ui.ie&&/(static|relative)/.test(this.css("position"))||/absolute/.test(this.css("position"))?this.parents().filter(function(){return/(relative|absolute|fixed)/.test(t.css(this,"position"))&&/(auto|scroll)/.test(t.css(this,"overflow")+t.css(this,"overflow-y")+t.css(this,"overflow-x"))}).eq(0):this.parents().filter(function(){return/(auto|scroll)/.test(t.css(this,"overflow")+t.css(this,"overflow-y")+t.css(this,"overflow-x"))}).eq(0),/fixed/.test(this.css("position"))||!e.length?t(document):e},zIndex:function(i){if(i!==e)return this.css("zIndex",i);if(this.length)for(var s,n,o=t(this[0]);o.length&&o[0]!==document;){if(s=o.css("position"),("absolute"===s||"relative"===s||"fixed"===s)&&(n=parseInt(o.css("zIndex"),10),!isNaN(n)&&0!==n))return n;o=o.parent()}return 0},uniqueId:function(){return this.each(function(){this.id||(this.id="ui-id-"+ ++n)})},removeUniqueId:function(){return this.each(function(){o.test(this.id)&&t(this).removeAttr("id")})}}),t.extend(t.expr[":"],{data:t.expr.createPseudo?t.expr.createPseudo(function(e){return function(i){return!!t.data(i,e)}}):function(e,i,s){return!!t.data(e,s[3])},focusable:function(e){return i(e,!isNaN(t.attr(e,"tabindex")))},tabbable:function(e){var s=t.attr(e,"tabindex"),n=isNaN(s);return(n||s>=0)&&i(e,!n)}}),t(function(){var e=document.body,i=e.appendChild(i=document.createElement("div"));i.offsetHeight,t.extend(i.style,{minHeight:"100px",height:"auto",padding:0,borderWidth:0}),t.support.minHeight=100===i.offsetHeight,t.support.selectstart="onselectstart"in i,e.removeChild(i).style.display="none"}),t("<a>").outerWidth(1).jquery||t.each(["Width","Height"],function(i,s){function n(e,i,s,n){return t.each(o,function(){i-=parseFloat(t.css(e,"padding"+this))||0,s&&(i-=parseFloat(t.css(e,"border"+this+"Width"))||0),n&&(i-=parseFloat(t.css(e,"margin"+this))||0)}),i}var o="Width"===s?["Left","Right"]:["Top","Bottom"],a=s.toLowerCase(),r={innerWidth:t.fn.innerWidth,innerHeight:t.fn.innerHeight,outerWidth:t.fn.outerWidth,outerHeight:t.fn.outerHeight};t.fn["inner"+s]=function(i){return i===e?r["inner"+s].call(this):this.each(function(){t(this).css(a,n(this,i)+"px")})},t.fn["outer"+s]=function(e,i){return"number"!=typeof e?r["outer"+s].call(this,e):this.each(function(){t(this).css(a,n(this,e,!0,i)+"px")})}}),t("<a>").data("a-b","a").removeData("a-b").data("a-b")&&(t.fn.removeData=function(e){return function(i){return arguments.length?e.call(this,t.camelCase(i)):e.call(this)}}(t.fn.removeData)),function(){var e=/msie ([\w.]+)/.exec(navigator.userAgent.toLowerCase())||[];t.ui.ie=e.length?!0:!1,t.ui.ie6=6===parseFloat(e[1],10)}(),t.fn.extend({disableSelection:function(){return this.bind((t.support.selectstart?"selectstart":"mousedown")+".ui-disableSelection",function(t){t.preventDefault()})},enableSelection:function(){return this.unbind(".ui-disableSelection")}}),t.extend(t.ui,{plugin:{add:function(e,i,s){var n,o=t.ui[e].prototype;for(n in s)o.plugins[n]=o.plugins[n]||[],o.plugins[n].push([i,s[n]])},call:function(t,e,i){var s,n=t.plugins[e];if(n&&t.element[0].parentNode&&11!==t.element[0].parentNode.nodeType)for(s=0;n.length>s;s++)t.options[n[s][0]]&&n[s][1].apply(t.element,i)}},contains:t.contains,hasScroll:function(e,i){if("hidden"===t(e).css("overflow"))return!1;var s=i&&"left"===i?"scrollLeft":"scrollTop",n=!1;return e[s]>0?!0:(e[s]=1,n=e[s]>0,e[s]=0,n)},isOverAxis:function(t,e,i){return t>e&&e+i>t},isOver:function(e,i,s,n,o,a){return t.ui.isOverAxis(e,s,o)&&t.ui.isOverAxis(i,n,a)}}))})(jQuery);(function(t,e){var i=0,s=Array.prototype.slice,n=t.cleanData;t.cleanData=function(e){for(var i,s=0;null!=(i=e[s]);s++)try{t(i).triggerHandler("remove")}catch(o){}n(e)},t.widget=function(i,s,n){var o,a,r,h,l=i.split(".")[0];i=i.split(".")[1],o=l+"-"+i,n||(n=s,s=t.Widget),t.expr[":"][o.toLowerCase()]=function(e){return!!t.data(e,o)},t[l]=t[l]||{},a=t[l][i],r=t[l][i]=function(t,i){return this._createWidget?(arguments.length&&this._createWidget(t,i),e):new r(t,i)},t.extend(r,a,{version:n.version,_proto:t.extend({},n),_childConstructors:[]}),h=new s,h.options=t.widget.extend({},h.options),t.each(n,function(e,i){t.isFunction(i)&&(n[e]=function(){var t=function(){return s.prototype[e].apply(this,arguments)},n=function(t){return s.prototype[e].apply(this,t)};return function(){var e,s=this._super,o=this._superApply;return this._super=t,this._superApply=n,e=i.apply(this,arguments),this._super=s,this._superApply=o,e}}())}),r.prototype=t.widget.extend(h,{widgetEventPrefix:a?h.widgetEventPrefix:i},n,{constructor:r,namespace:l,widgetName:i,widgetBaseClass:o,widgetFullName:o}),a?(t.each(a._childConstructors,function(e,i){var s=i.prototype;t.widget(s.namespace+"."+s.widgetName,r,i._proto)}),delete a._childConstructors):s._childConstructors.push(r),t.widget.bridge(i,r)},t.widget.extend=function(i){for(var n,o,a=s.call(arguments,1),r=0,h=a.length;h>r;r++)for(n in a[r])o=a[r][n],a[r].hasOwnProperty(n)&&o!==e&&(i[n]=t.isPlainObject(o)?t.isPlainObject(i[n])?t.widget.extend({},i[n],o):t.widget.extend({},o):o);return i},t.widget.bridge=function(i,n){var o=n.prototype.widgetFullName||i;t.fn[i]=function(a){var r="string"==typeof a,h=s.call(arguments,1),l=this;return a=!r&&h.length?t.widget.extend.apply(null,[a].concat(h)):a,r?this.each(function(){var s,n=t.data(this,o);return n?t.isFunction(n[a])&&"_"!==a.charAt(0)?(s=n[a].apply(n,h),s!==n&&s!==e?(l=s&&s.jquery?l.pushStack(s.get()):s,!1):e):t.error("no such method '"+a+"' for "+i+" widget instance"):t.error("cannot call methods on "+i+" prior to initialization; "+"attempted to call method '"+a+"'")}):this.each(function(){var e=t.data(this,o);e?e.option(a||{})._init():t.data(this,o,new n(a,this))}),l}},t.Widget=function(){},t.Widget._childConstructors=[],t.Widget.prototype={widgetName:"widget",widgetEventPrefix:"",defaultElement:"<div>",options:{disabled:!1,create:null},_createWidget:function(e,s){s=t(s||this.defaultElement||this)[0],this.element=t(s),this.uuid=i++,this.eventNamespace="."+this.widgetName+this.uuid,this.options=t.widget.extend({},this.options,this._getCreateOptions(),e),this.bindings=t(),this.hoverable=t(),this.focusable=t(),s!==this&&(t.data(s,this.widgetName,this),t.data(s,this.widgetFullName,this),this._on(!0,this.element,{remove:function(t){t.target===s&&this.destroy()}}),this.document=t(s.style?s.ownerDocument:s.document||s),this.window=t(this.document[0].defaultView||this.document[0].parentWindow)),this._create(),this._trigger("create",null,this._getCreateEventData()),this._init()},_getCreateOptions:t.noop,_getCreateEventData:t.noop,_create:t.noop,_init:t.noop,destroy:function(){this._destroy(),this.element.unbind(this.eventNamespace).removeData(this.widgetName).removeData(this.widgetFullName).removeData(t.camelCase(this.widgetFullName)),this.widget().unbind(this.eventNamespace).removeAttr("aria-disabled").removeClass(this.widgetFullName+"-disabled "+"ui-state-disabled"),this.bindings.unbind(this.eventNamespace),this.hoverable.removeClass("ui-state-hover"),this.focusable.removeClass("ui-state-focus")},_destroy:t.noop,widget:function(){return this.element},option:function(i,s){var n,o,a,r=i;if(0===arguments.length)return t.widget.extend({},this.options);if("string"==typeof i)if(r={},n=i.split("."),i=n.shift(),n.length){for(o=r[i]=t.widget.extend({},this.options[i]),a=0;n.length-1>a;a++)o[n[a]]=o[n[a]]||{},o=o[n[a]];if(i=n.pop(),s===e)return o[i]===e?null:o[i];o[i]=s}else{if(s===e)return this.options[i]===e?null:this.options[i];r[i]=s}return this._setOptions(r),this},_setOptions:function(t){var e;for(e in t)this._setOption(e,t[e]);return this},_setOption:function(t,e){return this.options[t]=e,"disabled"===t&&(this.widget().toggleClass(this.widgetFullName+"-disabled ui-state-disabled",!!e).attr("aria-disabled",e),this.hoverable.removeClass("ui-state-hover"),this.focusable.removeClass("ui-state-focus")),this},enable:function(){return this._setOption("disabled",!1)},disable:function(){return this._setOption("disabled",!0)},_on:function(i,s,n){var o,a=this;"boolean"!=typeof i&&(n=s,s=i,i=!1),n?(s=o=t(s),this.bindings=this.bindings.add(s)):(n=s,s=this.element,o=this.widget()),t.each(n,function(n,r){function h(){return i||a.options.disabled!==!0&&!t(this).hasClass("ui-state-disabled")?("string"==typeof r?a[r]:r).apply(a,arguments):e}"string"!=typeof r&&(h.guid=r.guid=r.guid||h.guid||t.guid++);var l=n.match(/^(\w+)\s*(.*)$/),c=l[1]+a.eventNamespace,u=l[2];u?o.delegate(u,c,h):s.bind(c,h)})},_off:function(t,e){e=(e||"").split(" ").join(this.eventNamespace+" ")+this.eventNamespace,t.unbind(e).undelegate(e)},_delay:function(t,e){function i(){return("string"==typeof t?s[t]:t).apply(s,arguments)}var s=this;return setTimeout(i,e||0)},_hoverable:function(e){this.hoverable=this.hoverable.add(e),this._on(e,{mouseenter:function(e){t(e.currentTarget).addClass("ui-state-hover")},mouseleave:function(e){t(e.currentTarget).removeClass("ui-state-hover")}})},_focusable:function(e){this.focusable=this.focusable.add(e),this._on(e,{focusin:function(e){t(e.currentTarget).addClass("ui-state-focus")},focusout:function(e){t(e.currentTarget).removeClass("ui-state-focus")}})},_trigger:function(e,i,s){var n,o,a=this.options[e];if(s=s||{},i=t.Event(i),i.type=(e===this.widgetEventPrefix?e:this.widgetEventPrefix+e).toLowerCase(),i.target=this.element[0],o=i.originalEvent)for(n in o)n in i||(i[n]=o[n]);return this.element.trigger(i,s),!(t.isFunction(a)&&a.apply(this.element[0],[i].concat(s))===!1||i.isDefaultPrevented())}},t.each({show:"fadeIn",hide:"fadeOut"},function(e,i){t.Widget.prototype["_"+e]=function(s,n,o){"string"==typeof n&&(n={effect:n});var a,r=n?n===!0||"number"==typeof n?i:n.effect||i:e;n=n||{},"number"==typeof n&&(n={duration:n}),a=!t.isEmptyObject(n),n.complete=o,n.delay&&s.delay(n.delay),a&&t.effects&&(t.effects.effect[r]||t.uiBackCompat!==!1&&t.effects[r])?s[e](n):r!==e&&s[r]?s[r](n.duration,n.easing,o):s.queue(function(i){t(this)[e](),o&&o.call(s[0]),i()})}}),t.uiBackCompat!==!1&&(t.Widget.prototype._getCreateOptions=function(){return t.metadata&&t.metadata.get(this.element[0])[this.widgetName]})})(jQuery);(function(t){var e=!1;t(document).mouseup(function(){e=!1}),t.widget("ui.mouse",{version:"1.9.2",options:{cancel:"input,textarea,button,select,option",distance:1,delay:0},_mouseInit:function(){var e=this;this.element.bind("mousedown."+this.widgetName,function(t){return e._mouseDown(t)}).bind("click."+this.widgetName,function(i){return!0===t.data(i.target,e.widgetName+".preventClickEvent")?(t.removeData(i.target,e.widgetName+".preventClickEvent"),i.stopImmediatePropagation(),!1):undefined}),this.started=!1},_mouseDestroy:function(){this.element.unbind("."+this.widgetName),this._mouseMoveDelegate&&t(document).unbind("mousemove."+this.widgetName,this._mouseMoveDelegate).unbind("mouseup."+this.widgetName,this._mouseUpDelegate)},_mouseDown:function(i){if(!e){this._mouseStarted&&this._mouseUp(i),this._mouseDownEvent=i;var s=this,n=1===i.which,o="string"==typeof this.options.cancel&&i.target.nodeName?t(i.target).closest(this.options.cancel).length:!1;return n&&!o&&this._mouseCapture(i)?(this.mouseDelayMet=!this.options.delay,this.mouseDelayMet||(this._mouseDelayTimer=setTimeout(function(){s.mouseDelayMet=!0},this.options.delay)),this._mouseDistanceMet(i)&&this._mouseDelayMet(i)&&(this._mouseStarted=this._mouseStart(i)!==!1,!this._mouseStarted)?(i.preventDefault(),!0):(!0===t.data(i.target,this.widgetName+".preventClickEvent")&&t.removeData(i.target,this.widgetName+".preventClickEvent"),this._mouseMoveDelegate=function(t){return s._mouseMove(t)},this._mouseUpDelegate=function(t){return s._mouseUp(t)},t(document).bind("mousemove."+this.widgetName,this._mouseMoveDelegate).bind("mouseup."+this.widgetName,this._mouseUpDelegate),i.preventDefault(),e=!0,!0)):!0}},_mouseMove:function(e){return!t.ui.ie||document.documentMode>=9||e.button?this._mouseStarted?(this._mouseDrag(e),e.preventDefault()):(this._mouseDistanceMet(e)&&this._mouseDelayMet(e)&&(this._mouseStarted=this._mouseStart(this._mouseDownEvent,e)!==!1,this._mouseStarted?this._mouseDrag(e):this._mouseUp(e)),!this._mouseStarted):this._mouseUp(e)},_mouseUp:function(e){return t(document).unbind("mousemove."+this.widgetName,this._mouseMoveDelegate).unbind("mouseup."+this.widgetName,this._mouseUpDelegate),this._mouseStarted&&(this._mouseStarted=!1,e.target===this._mouseDownEvent.target&&t.data(e.target,this.widgetName+".preventClickEvent",!0),this._mouseStop(e)),!1},_mouseDistanceMet:function(t){return Math.max(Math.abs(this._mouseDownEvent.pageX-t.pageX),Math.abs(this._mouseDownEvent.pageY-t.pageY))>=this.options.distance},_mouseDelayMet:function(){return this.mouseDelayMet},_mouseStart:function(){},_mouseDrag:function(){},_mouseStop:function(){},_mouseCapture:function(){return!0}})})(jQuery);(function(t){t.widget("ui.draggable",t.ui.mouse,{version:"1.9.2",widgetEventPrefix:"drag",options:{addClasses:!0,appendTo:"parent",axis:!1,connectToSortable:!1,containment:!1,cursor:"auto",cursorAt:!1,grid:!1,handle:!1,helper:"original",iframeFix:!1,opacity:!1,refreshPositions:!1,revert:!1,revertDuration:500,scope:"default",scroll:!0,scrollSensitivity:20,scrollSpeed:20,snap:!1,snapMode:"both",snapTolerance:20,stack:!1,zIndex:!1},_create:function(){"original"!=this.options.helper||/^(?:r|a|f)/.test(this.element.css("position"))||(this.element[0].style.position="relative"),this.options.addClasses&&this.element.addClass("ui-draggable"),this.options.disabled&&this.element.addClass("ui-draggable-disabled"),this._mouseInit()},_destroy:function(){this.element.removeClass("ui-draggable ui-draggable-dragging ui-draggable-disabled"),this._mouseDestroy()},_mouseCapture:function(e){var i=this.options;return this.helper||i.disabled||t(e.target).is(".ui-resizable-handle")?!1:(this.handle=this._getHandle(e),this.handle?(t(i.iframeFix===!0?"iframe":i.iframeFix).each(function(){t('<div class="ui-draggable-iframeFix" style="background: #fff;"></div>').css({width:this.offsetWidth+"px",height:this.offsetHeight+"px",position:"absolute",opacity:"0.001",zIndex:1e3}).css(t(this).offset()).appendTo("body")}),!0):!1)},_mouseStart:function(e){var i=this.options;return this.helper=this._createHelper(e),this.helper.addClass("ui-draggable-dragging"),this._cacheHelperProportions(),t.ui.ddmanager&&(t.ui.ddmanager.current=this),this._cacheMargins(),this.cssPosition=this.helper.css("position"),this.scrollParent=this.helper.scrollParent(),this.offset=this.positionAbs=this.element.offset(),this.offset={top:this.offset.top-this.margins.top,left:this.offset.left-this.margins.left},t.extend(this.offset,{click:{left:e.pageX-this.offset.left,top:e.pageY-this.offset.top},parent:this._getParentOffset(),relative:this._getRelativeOffset()}),this.originalPosition=this.position=this._generatePosition(e),this.originalPageX=e.pageX,this.originalPageY=e.pageY,i.cursorAt&&this._adjustOffsetFromHelper(i.cursorAt),i.containment&&this._setContainment(),this._trigger("start",e)===!1?(this._clear(),!1):(this._cacheHelperProportions(),t.ui.ddmanager&&!i.dropBehaviour&&t.ui.ddmanager.prepareOffsets(this,e),this._mouseDrag(e,!0),t.ui.ddmanager&&t.ui.ddmanager.dragStart(this,e),!0)},_mouseDrag:function(e,i){if(this.position=this._generatePosition(e),this.positionAbs=this._convertPositionTo("absolute"),!i){var s=this._uiHash();if(this._trigger("drag",e,s)===!1)return this._mouseUp({}),!1;this.position=s.position}return this.options.axis&&"y"==this.options.axis||(this.helper[0].style.left=this.position.left+"px"),this.options.axis&&"x"==this.options.axis||(this.helper[0].style.top=this.position.top+"px"),t.ui.ddmanager&&t.ui.ddmanager.drag(this,e),!1},_mouseStop:function(e){var i=!1;t.ui.ddmanager&&!this.options.dropBehaviour&&(i=t.ui.ddmanager.drop(this,e)),this.dropped&&(i=this.dropped,this.dropped=!1);for(var s=this.element[0],n=!1;s&&(s=s.parentNode);)s==document&&(n=!0);if(!n&&"original"===this.options.helper)return!1;if("invalid"==this.options.revert&&!i||"valid"==this.options.revert&&i||this.options.revert===!0||t.isFunction(this.options.revert)&&this.options.revert.call(this.element,i)){var o=this;t(this.helper).animate(this.originalPosition,parseInt(this.options.revertDuration,10),function(){o._trigger("stop",e)!==!1&&o._clear()})}else this._trigger("stop",e)!==!1&&this._clear();return!1},_mouseUp:function(e){return t("div.ui-draggable-iframeFix").each(function(){this.parentNode.removeChild(this)}),t.ui.ddmanager&&t.ui.ddmanager.dragStop(this,e),t.ui.mouse.prototype._mouseUp.call(this,e)},cancel:function(){return this.helper.is(".ui-draggable-dragging")?this._mouseUp({}):this._clear(),this},_getHandle:function(e){var i=this.options.handle&&t(this.options.handle,this.element).length?!1:!0;return t(this.options.handle,this.element).find("*").andSelf().each(function(){this==e.target&&(i=!0)}),i},_createHelper:function(e){var i=this.options,s=t.isFunction(i.helper)?t(i.helper.apply(this.element[0],[e])):"clone"==i.helper?this.element.clone().removeAttr("id"):this.element;return s.parents("body").length||s.appendTo("parent"==i.appendTo?this.element[0].parentNode:i.appendTo),s[0]==this.element[0]||/(fixed|absolute)/.test(s.css("position"))||s.css("position","absolute"),s},_adjustOffsetFromHelper:function(e){"string"==typeof e&&(e=e.split(" ")),t.isArray(e)&&(e={left:+e[0],top:+e[1]||0}),"left"in e&&(this.offset.click.left=e.left+this.margins.left),"right"in e&&(this.offset.click.left=this.helperProportions.width-e.right+this.margins.left),"top"in e&&(this.offset.click.top=e.top+this.margins.top),"bottom"in e&&(this.offset.click.top=this.helperProportions.height-e.bottom+this.margins.top)},_getParentOffset:function(){this.offsetParent=this.helper.offsetParent();var e=this.offsetParent.offset();return"absolute"==this.cssPosition&&this.scrollParent[0]!=document&&t.contains(this.scrollParent[0],this.offsetParent[0])&&(e.left+=this.scrollParent.scrollLeft(),e.top+=this.scrollParent.scrollTop()),(this.offsetParent[0]==document.body||this.offsetParent[0].tagName&&"html"==this.offsetParent[0].tagName.toLowerCase()&&t.ui.ie)&&(e={top:0,left:0}),{top:e.top+(parseInt(this.offsetParent.css("borderTopWidth"),10)||0),left:e.left+(parseInt(this.offsetParent.css("borderLeftWidth"),10)||0)}},_getRelativeOffset:function(){if("relative"==this.cssPosition){var t=this.element.position();return{top:t.top-(parseInt(this.helper.css("top"),10)||0)+this.scrollParent.scrollTop(),left:t.left-(parseInt(this.helper.css("left"),10)||0)+this.scrollParent.scrollLeft()}}return{top:0,left:0}},_cacheMargins:function(){this.margins={left:parseInt(this.element.css("marginLeft"),10)||0,top:parseInt(this.element.css("marginTop"),10)||0,right:parseInt(this.element.css("marginRight"),10)||0,bottom:parseInt(this.element.css("marginBottom"),10)||0}},_cacheHelperProportions:function(){this.helperProportions={width:this.helper.outerWidth(),height:this.helper.outerHeight()}},_setContainment:function(){var e=this.options;if("parent"==e.containment&&(e.containment=this.helper[0].parentNode),("document"==e.containment||"window"==e.containment)&&(this.containment=["document"==e.containment?0:t(window).scrollLeft()-this.offset.relative.left-this.offset.parent.left,"document"==e.containment?0:t(window).scrollTop()-this.offset.relative.top-this.offset.parent.top,("document"==e.containment?0:t(window).scrollLeft())+t("document"==e.containment?document:window).width()-this.helperProportions.width-this.margins.left,("document"==e.containment?0:t(window).scrollTop())+(t("document"==e.containment?document:window).height()||document.body.parentNode.scrollHeight)-this.helperProportions.height-this.margins.top]),/^(document|window|parent)$/.test(e.containment)||e.containment.constructor==Array)e.containment.constructor==Array&&(this.containment=e.containment);else{var i=t(e.containment),s=i[0];if(!s)return;i.offset();var n="hidden"!=t(s).css("overflow");this.containment=[(parseInt(t(s).css("borderLeftWidth"),10)||0)+(parseInt(t(s).css("paddingLeft"),10)||0),(parseInt(t(s).css("borderTopWidth"),10)||0)+(parseInt(t(s).css("paddingTop"),10)||0),(n?Math.max(s.scrollWidth,s.offsetWidth):s.offsetWidth)-(parseInt(t(s).css("borderLeftWidth"),10)||0)-(parseInt(t(s).css("paddingRight"),10)||0)-this.helperProportions.width-this.margins.left-this.margins.right,(n?Math.max(s.scrollHeight,s.offsetHeight):s.offsetHeight)-(parseInt(t(s).css("borderTopWidth"),10)||0)-(parseInt(t(s).css("paddingBottom"),10)||0)-this.helperProportions.height-this.margins.top-this.margins.bottom],this.relative_container=i}},_convertPositionTo:function(e,i){i||(i=this.position);var s="absolute"==e?1:-1,n=(this.options,"absolute"!=this.cssPosition||this.scrollParent[0]!=document&&t.contains(this.scrollParent[0],this.offsetParent[0])?this.scrollParent:this.offsetParent),o=/(html|body)/i.test(n[0].tagName);return{top:i.top+this.offset.relative.top*s+this.offset.parent.top*s-("fixed"==this.cssPosition?-this.scrollParent.scrollTop():o?0:n.scrollTop())*s,left:i.left+this.offset.relative.left*s+this.offset.parent.left*s-("fixed"==this.cssPosition?-this.scrollParent.scrollLeft():o?0:n.scrollLeft())*s}},_generatePosition:function(e){var i=this.options,s="absolute"!=this.cssPosition||this.scrollParent[0]!=document&&t.contains(this.scrollParent[0],this.offsetParent[0])?this.scrollParent:this.offsetParent,n=/(html|body)/i.test(s[0].tagName),o=e.pageX,a=e.pageY;if(this.originalPosition){var r;if(this.containment){if(this.relative_container){var l=this.relative_container.offset();r=[this.containment[0]+l.left,this.containment[1]+l.top,this.containment[2]+l.left,this.containment[3]+l.top]}else r=this.containment;e.pageX-this.offset.click.left<r[0]&&(o=r[0]+this.offset.click.left),e.pageY-this.offset.click.top<r[1]&&(a=r[1]+this.offset.click.top),e.pageX-this.offset.click.left>r[2]&&(o=r[2]+this.offset.click.left),e.pageY-this.offset.click.top>r[3]&&(a=r[3]+this.offset.click.top)}if(i.grid){var h=i.grid[1]?this.originalPageY+Math.round((a-this.originalPageY)/i.grid[1])*i.grid[1]:this.originalPageY;a=r?h-this.offset.click.top<r[1]||h-this.offset.click.top>r[3]?h-this.offset.click.top<r[1]?h+i.grid[1]:h-i.grid[1]:h:h;var c=i.grid[0]?this.originalPageX+Math.round((o-this.originalPageX)/i.grid[0])*i.grid[0]:this.originalPageX;o=r?c-this.offset.click.left<r[0]||c-this.offset.click.left>r[2]?c-this.offset.click.left<r[0]?c+i.grid[0]:c-i.grid[0]:c:c}}return{top:a-this.offset.click.top-this.offset.relative.top-this.offset.parent.top+("fixed"==this.cssPosition?-this.scrollParent.scrollTop():n?0:s.scrollTop()),left:o-this.offset.click.left-this.offset.relative.left-this.offset.parent.left+("fixed"==this.cssPosition?-this.scrollParent.scrollLeft():n?0:s.scrollLeft())}},_clear:function(){this.helper.removeClass("ui-draggable-dragging"),this.helper[0]==this.element[0]||this.cancelHelperRemoval||this.helper.remove(),this.helper=null,this.cancelHelperRemoval=!1},_trigger:function(e,i,s){return s=s||this._uiHash(),t.ui.plugin.call(this,e,[i,s]),"drag"==e&&(this.positionAbs=this._convertPositionTo("absolute")),t.Widget.prototype._trigger.call(this,e,i,s)},plugins:{},_uiHash:function(){return{helper:this.helper,position:this.position,originalPosition:this.originalPosition,offset:this.positionAbs}}}),t.ui.plugin.add("draggable","connectToSortable",{start:function(e,i){var s=t(this).data("draggable"),n=s.options,o=t.extend({},i,{item:s.element});s.sortables=[],t(n.connectToSortable).each(function(){var i=t.data(this,"sortable");i&&!i.options.disabled&&(s.sortables.push({instance:i,shouldRevert:i.options.revert}),i.refreshPositions(),i._trigger("activate",e,o))})},stop:function(e,i){var s=t(this).data("draggable"),n=t.extend({},i,{item:s.element});t.each(s.sortables,function(){this.instance.isOver?(this.instance.isOver=0,s.cancelHelperRemoval=!0,this.instance.cancelHelperRemoval=!1,this.shouldRevert&&(this.instance.options.revert=!0),this.instance._mouseStop(e),this.instance.options.helper=this.instance.options._helper,"original"==s.options.helper&&this.instance.currentItem.css({top:"auto",left:"auto"})):(this.instance.cancelHelperRemoval=!1,this.instance._trigger("deactivate",e,n))})},drag:function(e,i){var s=t(this).data("draggable"),n=this;t.each(s.sortables,function(){var o=!1,a=this;this.instance.positionAbs=s.positionAbs,this.instance.helperProportions=s.helperProportions,this.instance.offset.click=s.offset.click,this.instance._intersectsWith(this.instance.containerCache)&&(o=!0,t.each(s.sortables,function(){return this.instance.positionAbs=s.positionAbs,this.instance.helperProportions=s.helperProportions,this.instance.offset.click=s.offset.click,this!=a&&this.instance._intersectsWith(this.instance.containerCache)&&t.ui.contains(a.instance.element[0],this.instance.element[0])&&(o=!1),o})),o?(this.instance.isOver||(this.instance.isOver=1,this.instance.currentItem=t(n).clone().removeAttr("id").appendTo(this.instance.element).data("sortable-item",!0),this.instance.options._helper=this.instance.options.helper,this.instance.options.helper=function(){return i.helper[0]},e.target=this.instance.currentItem[0],this.instance._mouseCapture(e,!0),this.instance._mouseStart(e,!0,!0),this.instance.offset.click.top=s.offset.click.top,this.instance.offset.click.left=s.offset.click.left,this.instance.offset.parent.left-=s.offset.parent.left-this.instance.offset.parent.left,this.instance.offset.parent.top-=s.offset.parent.top-this.instance.offset.parent.top,s._trigger("toSortable",e),s.dropped=this.instance.element,s.currentItem=s.element,this.instance.fromOutside=s),this.instance.currentItem&&this.instance._mouseDrag(e)):this.instance.isOver&&(this.instance.isOver=0,this.instance.cancelHelperRemoval=!0,this.instance.options.revert=!1,this.instance._trigger("out",e,this.instance._uiHash(this.instance)),this.instance._mouseStop(e,!0),this.instance.options.helper=this.instance.options._helper,this.instance.currentItem.remove(),this.instance.placeholder&&this.instance.placeholder.remove(),s._trigger("fromSortable",e),s.dropped=!1)})}}),t.ui.plugin.add("draggable","cursor",{start:function(){var e=t("body"),i=t(this).data("draggable").options;e.css("cursor")&&(i._cursor=e.css("cursor")),e.css("cursor",i.cursor)},stop:function(){var e=t(this).data("draggable").options;e._cursor&&t("body").css("cursor",e._cursor)}}),t.ui.plugin.add("draggable","opacity",{start:function(e,i){var s=t(i.helper),n=t(this).data("draggable").options;s.css("opacity")&&(n._opacity=s.css("opacity")),s.css("opacity",n.opacity)},stop:function(e,i){var s=t(this).data("draggable").options;s._opacity&&t(i.helper).css("opacity",s._opacity)}}),t.ui.plugin.add("draggable","scroll",{start:function(){var e=t(this).data("draggable");e.scrollParent[0]!=document&&"HTML"!=e.scrollParent[0].tagName&&(e.overflowOffset=e.scrollParent.offset())},drag:function(e){var i=t(this).data("draggable"),s=i.options,n=!1;i.scrollParent[0]!=document&&"HTML"!=i.scrollParent[0].tagName?(s.axis&&"x"==s.axis||(i.overflowOffset.top+i.scrollParent[0].offsetHeight-e.pageY<s.scrollSensitivity?i.scrollParent[0].scrollTop=n=i.scrollParent[0].scrollTop+s.scrollSpeed:e.pageY-i.overflowOffset.top<s.scrollSensitivity&&(i.scrollParent[0].scrollTop=n=i.scrollParent[0].scrollTop-s.scrollSpeed)),s.axis&&"y"==s.axis||(i.overflowOffset.left+i.scrollParent[0].offsetWidth-e.pageX<s.scrollSensitivity?i.scrollParent[0].scrollLeft=n=i.scrollParent[0].scrollLeft+s.scrollSpeed:e.pageX-i.overflowOffset.left<s.scrollSensitivity&&(i.scrollParent[0].scrollLeft=n=i.scrollParent[0].scrollLeft-s.scrollSpeed))):(s.axis&&"x"==s.axis||(e.pageY-t(document).scrollTop()<s.scrollSensitivity?n=t(document).scrollTop(t(document).scrollTop()-s.scrollSpeed):t(window).height()-(e.pageY-t(document).scrollTop())<s.scrollSensitivity&&(n=t(document).scrollTop(t(document).scrollTop()+s.scrollSpeed))),s.axis&&"y"==s.axis||(e.pageX-t(document).scrollLeft()<s.scrollSensitivity?n=t(document).scrollLeft(t(document).scrollLeft()-s.scrollSpeed):t(window).width()-(e.pageX-t(document).scrollLeft())<s.scrollSensitivity&&(n=t(document).scrollLeft(t(document).scrollLeft()+s.scrollSpeed)))),n!==!1&&t.ui.ddmanager&&!s.dropBehaviour&&t.ui.ddmanager.prepareOffsets(i,e)}}),t.ui.plugin.add("draggable","snap",{start:function(){var e=t(this).data("draggable"),i=e.options;e.snapElements=[],t(i.snap.constructor!=String?i.snap.items||":data(draggable)":i.snap).each(function(){var i=t(this),s=i.offset();this!=e.element[0]&&e.snapElements.push({item:this,width:i.outerWidth(),height:i.outerHeight(),top:s.top,left:s.left})})},drag:function(e,i){for(var s=t(this).data("draggable"),n=s.options,o=n.snapTolerance,a=i.offset.left,r=a+s.helperProportions.width,l=i.offset.top,h=l+s.helperProportions.height,c=s.snapElements.length-1;c>=0;c--){var u=s.snapElements[c].left,d=u+s.snapElements[c].width,p=s.snapElements[c].top,f=p+s.snapElements[c].height;if(a>u-o&&d+o>a&&l>p-o&&f+o>l||a>u-o&&d+o>a&&h>p-o&&f+o>h||r>u-o&&d+o>r&&l>p-o&&f+o>l||r>u-o&&d+o>r&&h>p-o&&f+o>h){if("inner"!=n.snapMode){var m=o>=Math.abs(p-h),g=o>=Math.abs(f-l),v=o>=Math.abs(u-r),_=o>=Math.abs(d-a);m&&(i.position.top=s._convertPositionTo("relative",{top:p-s.helperProportions.height,left:0}).top-s.margins.top),g&&(i.position.top=s._convertPositionTo("relative",{top:f,left:0}).top-s.margins.top),v&&(i.position.left=s._convertPositionTo("relative",{top:0,left:u-s.helperProportions.width}).left-s.margins.left),_&&(i.position.left=s._convertPositionTo("relative",{top:0,left:d}).left-s.margins.left)}var b=m||g||v||_;if("outer"!=n.snapMode){var m=o>=Math.abs(p-l),g=o>=Math.abs(f-h),v=o>=Math.abs(u-a),_=o>=Math.abs(d-r);m&&(i.position.top=s._convertPositionTo("relative",{top:p,left:0}).top-s.margins.top),g&&(i.position.top=s._convertPositionTo("relative",{top:f-s.helperProportions.height,left:0}).top-s.margins.top),v&&(i.position.left=s._convertPositionTo("relative",{top:0,left:u}).left-s.margins.left),_&&(i.position.left=s._convertPositionTo("relative",{top:0,left:d-s.helperProportions.width}).left-s.margins.left)}!s.snapElements[c].snapping&&(m||g||v||_||b)&&s.options.snap.snap&&s.options.snap.snap.call(s.element,e,t.extend(s._uiHash(),{snapItem:s.snapElements[c].item})),s.snapElements[c].snapping=m||g||v||_||b}else s.snapElements[c].snapping&&s.options.snap.release&&s.options.snap.release.call(s.element,e,t.extend(s._uiHash(),{snapItem:s.snapElements[c].item})),s.snapElements[c].snapping=!1}}}),t.ui.plugin.add("draggable","stack",{start:function(){var e=t(this).data("draggable").options,i=t.makeArray(t(e.stack)).sort(function(e,i){return(parseInt(t(e).css("zIndex"),10)||0)-(parseInt(t(i).css("zIndex"),10)||0)});if(i.length){var s=parseInt(i[0].style.zIndex)||0;t(i).each(function(t){this.style.zIndex=s+t}),this[0].style.zIndex=s+i.length}}}),t.ui.plugin.add("draggable","zIndex",{start:function(e,i){var s=t(i.helper),n=t(this).data("draggable").options;s.css("zIndex")&&(n._zIndex=s.css("zIndex")),s.css("zIndex",n.zIndex)},stop:function(e,i){var s=t(this).data("draggable").options;s._zIndex&&t(i.helper).css("zIndex",s._zIndex)}})})(jQuery);
(function() {
    var attachEvent = document.attachEvent;
    var isIE = navigator.userAgent.match(/Trident/);
    var requestFrame = (function() {
        var raf = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame ||
            function(fn) {
                return window.setTimeout(fn, 20);
            };
        return function(fn) {
            return raf(fn);
        };
    })();

    var cancelFrame = (function() {
        var cancel = window.cancelAnimationFrame || window.mozCancelAnimationFrame || window.webkitCancelAnimationFrame ||
            window.clearTimeout;
        return function(id) {
            return cancel(id);
        };
    })();

    function resizeListener(e) {
        var win = e.target || e.srcElement;
        if (win.__resizeRAF__) cancelFrame(win.__resizeRAF__);
        win.__resizeRAF__ = requestFrame(function() {
            var trigger = win.__resizeTrigger__;
            trigger.__resizeListeners__.forEach(function(fn) {
                fn.call(trigger, e);
            });
        });
    }

    function objectLoad(e) {
        this.contentDocument.defaultView.__resizeTrigger__ = this.__resizeElement__;
        this.contentDocument.defaultView.addEventListener('resize', resizeListener);
    }

    window.addResizeListener = function(element, fn) {
        if (!element.__resizeListeners__) {
            element.__resizeListeners__ = [];
            if (attachEvent) {
                element.__resizeTrigger__ = element;
                element.attachEvent('onresize', resizeListener);
            } else {
                if (getComputedStyle(element).position == 'static') element.style.position = 'relative';
                var obj = element.__resizeTrigger__ = document.createElement('object');
                obj.setAttribute('style', 'display: block; position: absolute; top: 0; left: 0; height: 100%; width: 100%; overflow: hidden; pointer-events: none; z-index: -1;');
                obj.__resizeElement__ = element;
                obj.onload = objectLoad;
                obj.type = 'text/html';
                if (isIE) element.appendChild(obj);
                obj.data = 'about:blank';
                if (!isIE) element.appendChild(obj);
            }
        }
        element.__resizeListeners__.push(fn);
    };

    window.removeResizeListener = function(element, fn) {
        element.__resizeListeners__.splice(element.__resizeListeners__.indexOf(fn), 1);
        if (!element.__resizeListeners__.length) {
            if (attachEvent) element.detachEvent('onresize', resizeListener);
            else {
                element.__resizeTrigger__.contentDocument.defaultView.removeEventListener('resize', resizeListener);
                element.__resizeTrigger__ = !element.removeChild(element.__resizeTrigger__);
            }
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(event) {
    var elm = $('[ita-messenger]');
    var crm_path = (elm[0].getAttribute('path'));
    var elm = $('[ita-messenger]').load(crm_path + "/static_templates/itaMessegger.html", function() {
        var busy = false;
        var state = 0;
        var setState = function(newVal) {
            var oldVal = state;
            state = newVal;
            localStorage.setItem("chatState", state);
            var res_elem = jQuery('.draggable');
            if (state == 2) {
                res_elem.removeClass("normal");
            } else {
                reinitElemPos();
                setTimeout(function() {
                    reinitElemPos();
                    res_elem.addClass("normal");
                }, 600);
            }
            if (newVal == 1) {
                localStorage.setItem("chatStateFromMinimalizate", oldVal);
            }
            res_elem.removeClass("full");
            res_elem.removeClass("mini");
            
            switch (state) {
                case 0:
                    {
                        break;
                    }
                case 1:
                    {
                        res_elem.addClass("mini");
                        break;
                    }
                case 2:
                    {
                        res_elem.addClass("full");
                        break;
                    }
            }
            

        }
        var iframe = elm.find("iframe");
        iframe.attr('src', crm_path);
        var dragstart = function() {
            busy = true;
            var res_elem = jQuery('.draggable');
            res_elem.addClass("disable-animation");
            jQuery("iframe").addClass("disable-mouse");
        };

        var dragend = function() {
            if (!arguments[0]) this.dropped = false;
            var res_elem = jQuery('.draggable');
            res_elem.removeClass("disable-animation");
            jQuery("iframe").removeClass("disable-mouse");
            var res_elem = jQuery('.draggable');
            localStorage.setItem("chatX", res_elem.css("left"));
            localStorage.setItem("chatY", res_elem.css("top"));
            busy = false;
        };

        var minimizeteMin = function() {
            if (state == 1 && busy == false) {
                var oldVal = parseInt(localStorage.getItem("chatStateFromMinimalizate"));
                if (oldVal == undefined || oldVal == 1)
                    setState(0);
                else
                    setState(oldVal);
                return;
            }
        }
        var minimizete = function() {
            if (state == 1) {
                setState(0);
                return;
            }
            setState(1);
        }
        var fullScreen = function() {
            if (state == 1) {
                setState(0);
                return;
            }
            if (state == 2) {
                setState(0);
                return;
            }
            setState(2);

        }


        elm[0].querySelector('#minimize_btn').addEventListener("click", minimizete)
        elm[0].querySelector('#fullscreen_btn').addEventListener("click", fullScreen)
        elm[0].querySelector('.handle').addEventListener("mouseup", minimizeteMin)
        elm[0].querySelector('.handle').addEventListener("touchstart", minimizeteMin)


        var reinitElemPos = function() {
            console.log("test");
            var elem = jQuery(".dnd-container");
            var res_elem = jQuery('.draggable');
            var x = localStorage.getItem("chatX");
            var y = localStorage.getItem("chatY");
            if (x == undefined || y == undefined) {
                var elem = jQuery(".dnd-container");
                localStorage.setItem("chatX", elem.width());
                localStorage.setItem("chatY", elem.height());
                return reinitElemPos();
            } else {
                /*if($scope.state == 1)
                {
                    x = elem.width();
                    y = elem.height();
                }*/
                var offset = res_elem.position();
                //                    console.log(elem.height() < parseInt(y) + res_elem.height() || parseInt(y) < 0 || elem.width() < parseInt(x) + res_elem.width() || parseInt(x) < 0);
                if (elem.height() < parseInt(y) + res_elem.height() || parseInt(y) < 0 || elem.width() < parseInt(x) + res_elem.width() || parseInt(x) < 0) {
                    if (elem.height() < parseInt(y) + res_elem.height()) {
                        res_elem.css({ top: 'initial', bottom: "0px" });
                    }
                    if (parseInt(y) < 0) {
                        res_elem.css({ bottom: 'initial', top: "0px" });
                    }
                    if (elem.width() < parseInt(x) + res_elem.width() || parseInt(x) < 0) {
                        res_elem.css({ left: 'initial', right: "0px" });
                    }
                    if (parseInt(x) < 0) {
                        res_elem.css({ right: 'initial', left: "0px" });
                    }
                } else {
                    res_elem.css({ left: x });
                    res_elem.css({ top: y });
                }

            }
            return true;
        }

        jQuery(document).ready(reinitElemPos);
        jQuery(window).resize(reinitElemPos);
        window.addResizeListener(elm[0], reinitElemPos);


        jQuery("iframe").on('load', function() {
            setState(1);
            var elem = jQuery(".dnd-container");
            var res_elem = jQuery('.draggable');

            setTimeout(function() { res_elem.removeClass("disable-animation"); }, 100);
            jQuery(".chat").draggable({ /*handle: ".handle"*/ cancel: ".ignore", containment: "parent", start: dragstart, stop: dragend });
            reinitElemPos();
        });
    });
});
