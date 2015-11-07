// Copyright (c) Microsoft. All rights reserved.  Licensed under the MIT license. See LICENSE file in the project root for full license information.

(function () {
	'use strict';

	angular.module('xPlat', ['xPlat.services', 'xPlat.controllers', 'xPlat.directives']);
	angular.module('xPlat.directives', []);
	angular.module('xPlat.controllers', []);
	angular.module('xPlat.services', ['ngResource']);
        
        document.addEventListener("deviceready", function() {
            console.log("The device is ready");
            
        }, false);
        
        document.addEventListener("pause", function() {
            console.log("The device is paused");
        }, false);
        
        document.addEventListener("resume", function() {
            console.log("The device has resumed");
        }, false);
})();