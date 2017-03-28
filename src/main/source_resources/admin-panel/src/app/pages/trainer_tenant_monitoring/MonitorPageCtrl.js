/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('MonitorPageCtrl', MonitorPageCtrl);

    /** @ngInject */
    function MonitorPageCtrl($scope, $timeout, UserMonitorService, $http) {

        function openStart() {
            $scope.opened.start = true;
        }

        function openEnd() {
            $scope.opened.end = true;
        }

        $scope.dates = { start: new Date(), end: new Date() }
        $scope.dates.start.setMinutes(0); $scope.dates.start.setHours(0);
        $scope.dates.end.setMinutes(59); $scope.dates.end.setHours(23);
        $scope.openEnd = openEnd;
        $scope.openStart = openStart;

        $scope.opened = { start: false, end: false };
        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
        $scope.format = $scope.formats[0];
        $scope.options = {
            showWeeks: false
        };


        $scope.selectWithSearchItems = []; //UserMonitorService.findTenants();

        var wait = false
        var _infoStudent = '';
        $scope.getStudents = function(info) {
            if (_infoStudent != info && wait == false && info.trim() != '') {
                $http.get(serverPrefix + "/chat/findStudent?info=" + info, {}).success(function(data, status, headers, config) {
                    $scope.selectWithSearchItems = data;
                    wait = false;
                }).error(function(data, status, headers, config) {
                    wait = false;
                });
                wait = true;
                _infoStudent = info;
            }

            console.log(info);
            return $scope.selectWithSearchItems;
        }

        var wait = false

        function getUsersInfo(roles, info, succ, error) {
            if (wait == false) {
                $http.get(serverPrefix + "/chat/findUsersWithRoles?info=" + info + "&roles=" + roles, {}).success(function(data, status, headers, config) {
                    if (succ != undefined)
                        succ(data, status, headers, config);
                    wait = false;
                }).error(function(data, status, headers, config) {
                    wait = false;
                    if (error != undefined)
                        error(data, status, headers, config);
                });
                wait = true;
                return true
            }
            return false;
        }
        $scope.selected = { tenant: null, student: null };
        $scope.messages = [];

        $scope.getPrivate = function() {
            debugger;
            var tenant = $scope.selected.tenant;
            var student = $scope.selected.student;
            var msgRequestModel = {
                "user_id_first": tenant.chatUserId,
                "user_id_second": student.chatUserId,
                "before_date": new Date($scope.dates.end).getTime(),
                "after_date": new Date($scope.dates.start).getTime()
            }
            $http.post(serverPrefix + "/chat/msgHistory", msgRequestModel).success(function(data, status, headers, config) {
                $scope.messages = data;
            }).error(function(data, status, headers, config) {});


        }

        var _infoStudent = '';
        var StudentList = [];
        $scope.getStudents = function(info) {
            var roles = 1 << 1
            if (_infoStudent != info && info.trim() != '' && getUsersInfo(roles, info, function(data) { StudentList = data })) {
                _infoStudent = info;
            }
            return StudentList;
        }

        var _infoTenantAndTrainersList = '';
        var TenantAndTrainersList = [];
        $scope.getTrainersAndTenants = function(info) {
            var roles = (1 << 6 | 1 << 7);
            if (_infoTenantAndTrainersList != info && info.trim() != '' && getUsersInfo(roles, info, function(data) { TenantAndTrainersList = data })) {
                _infoTenantAndTrainersList = info;
            }
            return TenantAndTrainersList;
        }

        $scope.expandMessage = function(message) {
            message.expanded = !message.expanded;
        }
    }

})();
