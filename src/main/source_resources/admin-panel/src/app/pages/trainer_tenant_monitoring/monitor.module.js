/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring', ['ui.select', 'ngSanitize', 'Intita.monitor.servises'])
        .config(routeConfig);

    /** @ngInject */
    function routeConfig($stateProvider) {
        $stateProvider
            .state('monitor', {
                url: '/monitor',
                title: 'Monitor',
                templateUrl: 'app/pages/trainer_tenant_monitoring/monitor.html',
                controller: 'MonitorPageCtrl',
                sidebarMeta: {
                    icon: 'fa fa fa-bar-chart-o',
                    order: 0,
                },
            });
    }

})();
