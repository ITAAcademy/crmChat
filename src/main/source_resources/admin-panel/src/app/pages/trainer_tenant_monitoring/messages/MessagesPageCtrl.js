/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('MessagesPageCtrl', MessagesPageCtrl);

    /** @ngInject */
    function MessagesPageCtrl($scope, $timeout, UserMonitorService, $http,CommonOperationsService) {

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

        $scope.usersExceptStudentsList = [];
        $scope.usersList = [];


        $scope.selectWithSearchItems = []; //UserMonitorService.findTenants();

        var _infoStudent = '';


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
        $scope.fetchAllExceptStudents = function(info) {
            if (info.trim() != '')
                return CommonOperationsService.fetchUsersInfoExceptRole(info,USER_ROLES.STUDENT).then(function(payload){
                    $scope.usersExceptStudentsList = payload.data;
                });
            return $.Deferred().resolve();
        }

        var _infoTenantAndTrainersList = '';
        var TenantAndTrainersList = [];
        $scope.fetchAll = function(info) {
            if (info.trim() != '')
                return CommonOperationsService.fetchUsers(info).then(function(payload){
                    $scope.usersList = payload.data;
                });
            return $.Deferred().resolve();
        }

        $scope.expandMessage = function(message) {
            message.expanded = !message.expanded;
        }
    }

})();
