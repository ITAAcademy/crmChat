/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.localization_editor', ['ui.select', 'ngSanitize', 'springChat.directives'])
        .config(routeConfig);

    /** @ngInject */
    function routeConfig($stateProvider) {


        $stateProvider
            .state('localization', {
                url: '/',
                title: 'Localization editor',
                templateUrl: 'app/pages/localization_editor/localization.html',
                controller: 'LocalizationPageCtrl',
                sidebarMeta: {
                    icon: 'fa fa fa-bar-chart-o',
                    order: 0,
                },
            });

    }
})();
