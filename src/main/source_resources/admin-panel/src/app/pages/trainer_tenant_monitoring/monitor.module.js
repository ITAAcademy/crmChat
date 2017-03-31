/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring', ['ui.select', 'ngSanitize', 'Intita.monitor.servises', 'springChat.directives'])
        .config(routeConfig);

    /** @ngInject */
    function routeConfig($stateProvider, ChartJsProvider, baConfigProvider) {

        var layoutColors = baConfigProvider.colors;
        // Configure all charts
        ChartJsProvider.setOptions({
            chartColors: [
                layoutColors.primary, layoutColors.danger, layoutColors.warning, layoutColors.success, layoutColors.info, layoutColors.default, layoutColors.primaryDark, layoutColors.successDark, layoutColors.warningLight, layoutColors.successLight, layoutColors.primaryLight
            ],
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 2500
            },
            scale: {
                gridLines: {
                    color: layoutColors.border
                },
                scaleLabel: {
                    fontColor: layoutColors.defaultText
                },
                ticks: {
                    fontColor: layoutColors.defaultText,
                    showLabelBackdrop: false
                }
            }
        });

        $stateProvider
            .state('monitor', {
                url: '/monitor',
                abstract: true,
                template: '<div ui-view  autoscroll="true" autoscroll-body-top></div>',
                title: 'Monitor',
                sidebarMeta: {
                    icon: 'ion-stats-bars',
                    order: 150,
                },
            });

        $stateProvider
            .state('monitor.msgs', {
                url: '/msgs',
                title: 'Messages',
                templateUrl: 'app/pages/trainer_tenant_monitoring/messages/messages.html',
                controller: 'MessagesPageCtrl',
                sidebarMeta: {
                    icon: 'fa fa fa-bar-chart-o',
                    order: 0,
                },
            });

        $stateProvider
            .state('monitor.ratings', {
                url: '/ratings',
                title: 'Ratings',
                templateUrl: 'app/pages/trainer_tenant_monitoring/ratings/ratings.html',
                controller: 'RatingsPageCtrl',
                sidebarMeta: {
                    icon: 'fa fa fa-bar-chart-o',
                    order: 0,
                },
            });
    }
})();
