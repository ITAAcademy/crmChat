!function(t,r){"use strict";function e(t,r){var e,i;if("function"==typeof r)void 0!==(i=r(t))&&(t=i);else if(Array.isArray(r))for(e=0;e<r.length;e++)void 0!==(i=r[e](t))&&(t=i);return t}function i(t,r){return"-"===t[0]&&Array.isArray(r)&&/^-\d+$/.test(t)?r.length+parseInt(t,10):t}function n(t){return/^\d+/.test(t)}function o(t){return"[object Object]"===Object.prototype.toString.call(t)}function s(t){return Object(t)===t}function f(t){return 0===Object.keys(t).length}function c(t,r){return t.indexOf("[")>=0&&(t=t.replace(/\[/g,".").replace(/]/g,"")),t.split(r)}function p(t,r,e){if(!(this instanceof p))return new p(t,r,e);void 0===r&&(r=!1),void 0===e&&(e=!0),this.seperator=t||".",this.override=r,this.useArray=e,this.keepArray=!1,this.cleanup=[]}function u(t){return function(){return a[t].apply(a,arguments)}}var a=new p(".",!1,!0);p.prototype._fill=function(t,r,i,o){var c=t.shift();if(t.length>0){if(r[c]=r[c]||(this.useArray&&n(t[0])?[]:{}),!s(r[c])){if(!this.override){if(!s(i)||!f(i))throw new Error("Trying to redefine `"+c+"` which is a "+typeof r[c]);return}r[c]={}}this._fill(t,r[c],i,o)}else{if(!this.override&&s(r[c])&&!f(r[c])){if(!s(i)||!f(i))throw new Error("Trying to redefine non-empty obj['"+c+"']");return}r[c]=e(i,o)}},p.prototype.object=function(t,r){var i=this;return Object.keys(t).forEach(function(n){var o=void 0===r?null:r[n],s=c(n,i.seperator).join(i.seperator);-1!==s.indexOf(i.seperator)?(i._fill(s.split(i.seperator),t,t[n],o),delete t[n]):i.override&&(t[n]=e(t[n],o))}),t},p.prototype.str=function(t,r,i,n){return-1!==t.indexOf(this.seperator)?this._fill(t.split(this.seperator),i,r,n):i.hasOwnProperty(t)&&!this.override||(i[t]=e(r,n)),i},p.prototype.pick=function(t,r,e){var n,o,s,f,p;for(o=c(t,this.seperator),n=0;n<o.length;n++){if(f=i(o[n],r),!(r&&"object"==typeof r&&f in r))return;if(n===o.length-1)return e?(s=r[f],delete r[f],Array.isArray(r)&&(p=o.slice(0,-1).join("."),-1===this.cleanup.indexOf(p)&&this.cleanup.push(p)),s):r[f];r=r[f]}return e&&Array.isArray(r)&&(r=r.filter(function(t){return void 0!==t})),r},p.prototype.remove=function(t,r){var e;if(this.cleanup=[],Array.isArray(t)){for(e=0;e<t.length;e++)this.pick(t[e],r,!0);return this._cleanup(r),r}return this.pick(t,r,!0)},p.prototype._cleanup=function(t){var r,e,i,n;if(this.cleanup.length){for(e=0;e<this.cleanup.length;e++)r=(r=(n=(i=this.cleanup[e].split(".")).splice(0,-1).join("."))?this.pick(n,t):t)[i[0]].filter(function(t){return void 0!==t}),this.set(this.cleanup[e],r,t);this.cleanup=[]}},p.prototype.del=p.prototype.remove,p.prototype.move=function(t,r,i,n,o){return"function"==typeof n||Array.isArray(n)?this.set(r,e(this.pick(t,i,!0),n),i,o):(o=n,this.set(r,this.pick(t,i,!0),i,o)),i},p.prototype.transfer=function(t,r,i,n,o,s){return"function"==typeof o||Array.isArray(o)?this.set(r,e(this.pick(t,i,!0),o),n,s):(s=o,this.set(r,this.pick(t,i,!0),n,s)),n},p.prototype.copy=function(t,r,i,n,o,s){return"function"==typeof o||Array.isArray(o)?this.set(r,e(JSON.parse(JSON.stringify(this.pick(t,i,!1))),o),n,s):(s=o,this.set(r,this.pick(t,i,!1),n,s)),n},p.prototype.set=function(t,r,e,i){var n,s,f,p;if(void 0===r)return e;for(f=c(t,this.seperator),n=0;n<f.length;n++){if(p=f[n],n===f.length-1)if(i&&o(r)&&o(e[p]))for(s in r)r.hasOwnProperty(s)&&(e[p][s]=r[s]);else if(i&&Array.isArray(e[p])&&Array.isArray(r))for(var u=0;u<r.length;u++)e[f[n]].push(r[u]);else e[p]=r;else e.hasOwnProperty(p)&&(o(e[p])||Array.isArray(e[p]))||(/^\d+$/.test(f[n+1])?e[p]=[]:e[p]={});e=e[p]}return e},p.prototype.transform=function(t,r,e){return r=r||{},e=e||{},Object.keys(t).forEach(function(i){this.set(t[i],this.pick(i,r),e)}.bind(this)),e},p.prototype.dot=function(t,r,e){return r=r||{},e=e||[],Object.keys(t).forEach(function(i){if(s(t[i])&&(o(t[i])&&!f(t[i])||Array.isArray(t[i])&&!this.keepArray&&0!==t[i].length))return this.dot(t[i],r,e.concat(i));r[e.concat(i).join(this.seperator)]=t[i]}.bind(this)),r},p.pick=u("pick"),p.move=u("move"),p.transfer=u("transfer"),p.transform=u("transform"),p.copy=u("copy"),p.object=u("object"),p.str=u("str"),p.set=u("set"),p.del=p.remove=u("remove"),p.dot=u("dot"),["override","overwrite"].forEach(function(t){Object.defineProperty(p,t,{get:function(){return a.override},set:function(t){a.override=!!t}})}),["useArray","keepArray"].forEach(function(t){Object.defineProperty(p,t,{get:function(){return a[t]},set:function(r){a[t]=r}})}),p._process=e,"function"==typeof define&&define.amd?define(function(){return p}):"undefined"!=typeof module&&module.exports?module.exports=p:t.DotObject=p}(this);