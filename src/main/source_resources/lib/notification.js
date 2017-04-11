"use strict";

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

/*! HTML5 Notification - v3.0.0 - 2016-09-19

Copyright 2016 Tsvetan Tsvetkov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

!function () {
     function a() {
          var a = document.createElement("div");this.addEventListener = function (b, c) {
               a.addEventListener(b, c.bind(this));
          }, this.removeEventListener = function (b, c) {
               a.removeEventListener(b, c.bind(this));
          }, this.dispatchEvent = function (b) {
               if ("string" == typeof b) try {
                    a.dispatchEvent(new Event(b));
               } catch (d) {
                    var c = document.createEvent("Event");c.initEvent(b, !1, !0), a.dispatchEvent(c);
               }
          };
     }function b(b, c) {
          a.call(this);var d = k;Object.defineProperties(this, { close: { value: function (a) {
                         d === k && (window.external.msSiteModeClearIconOverlay(), l.forEach(function (a) {
                              window.removeEventListener(a, this.close);
                         }.bind(this)), this.dispatchEvent("click"), this.dispatchEvent("close"), d = null);
                    }.bind(this) } }), this.close(), this.icon && window.external.msSiteModeSetIconOverlay(m(this.icon), this.description || this.title), window.external.msSiteModeActivate(), this.dispatchEvent("show"), l.forEach(function (a) {
               window.addEventListener(a, this.close);
          }.bind(this)), d = ++k;
     }function c() {}function d(a, b) {
          var c, d;if (!arguments.length) throw TypeError('Failed to construct "Notification": 1 argument required, but only 0 present.');if ("" === a && (a = "\b"), arguments.length > 1 && "object" != (typeof b === "undefined" ? "undefined" : _typeof(b))) throw TypeError('Failed to construct "Notification": parameter 2 ("options") is not an object.');if (c = Object(b).dir, void 0 !== c && j.indexOf(c) === -1) throw TypeError('Failed to construct "Notification": The provided value "' + c + '" is not a valid enum value of type NotificationDirection.');return b = Object(b), d = new n(a, b), d.body || Object.defineProperty(d, "body", { value: String(b.body || "") }), d.data || Object.defineProperty(d, "data", { value: b.data || null }), d.dir || Object.defineProperty(d, "dir", { value: c || j[0] }), d.icon || Object.defineProperty(d, "icon", { value: String(b.icon || "") }), d.lang || Object.defineProperty(d, "lang", { value: String(b.lang || "") }), d.requireInteraction || Object.defineProperty(d, "requireInteraction", { value: Boolean(b.requireInteraction) }), d.silent || Object.defineProperty(d, "silent", { value: Boolean(b.silent) }), d.tag || Object.defineProperty(d, "tag", { value: String(b.tag || "") }), d.title || Object.defineProperty(d, "title", { value: String(a) }), d.timestamp || Object.defineProperty(d, "timestamp", { value: new Date().getTime() }), d;
     }var e = "default",
         f = "granted",
         g = "denied",
         h = "notsupported",
         i = [f, e, g, h],
         j = ["auto", "ltr", "rtl"],
         k = -1,
         l = ["click", "scroll", "focus"],
         m = function m(a) {
          var b = a.lastIndexOf(".");return (b !== -1 ? a.substr(0, b) : a) + ".ico";
     },
         n = window.Notification || window.webkitNotifications && c || "external" in window && "msIsSiteMode" in window.external && void 0 !== window.external.msIsSiteMode() && b || a;Object.defineProperty(a, "permission", { enumerable: !0, get: function get() {
               return h;
          } }), Object.defineProperty(a, "requestPermission", { enumerable: !0, writable: !0, value: function value(a) {
               a(this.permission);
          } }), Object.defineProperty(b, "permission", { enumerable: !0, get: function get() {
               var a = window.external.msIsSiteMode();return a ? f : g;
          } }), Object.defineProperty(b, "requestPermission", { enumerable: !0, writable: !0, value: function value(a) {
               return new Promise(function (a, b) {
                    a(this.permission);
               }.bind(this));
          } }), Object.defineProperty(b, "PERMISSION_REQUEST_MESSAGE", { writable: !0, value: "IE supports notifications in pinned mode only. Pin this page on your taskbar to receive notifications." }), Object.defineProperty(c, "permission", { enumerable: !0, get: function get() {
               return i[window.webkitNotifications.checkPermission()];
          } }), Object.defineProperty(c, "requestPermission", { enumerable: !0, writable: !0, value: function value(a) {
               return new Promise(function (a, b) {
                    window.webkitNotifications.requestPermission(function (b) {
                         a(b);
                    });
               });
          } }), n.permission || Object.defineProperty(n, "permission", { enumerable: !0, get: function get() {
               return n.permissionLevel && n.permissionLevel();
          } }), Object.defineProperty(d, "permission", { enumerable: !0, get: function get() {
               return n.permission;
          } }), Object.defineProperty(d, "requestPermission", { enumerable: !0, value: function value() {
               return new Promise(function (a, b) {
                    var c = n.requestPermission(function (b) {
                         a(b);
                    });c instanceof Promise && a(c);
               });
          } }), window.Notification = d;
}();