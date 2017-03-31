/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('RatingsPageCtrl', RatingsPageCtrl);

    /** @ngInject */
    function RatingsPageCtrl($scope, $timeout, UserMonitorService, RatingsPageService, $http, baConfig, $window) {

        function openStart() {
            $scope.opened.start = true;
        }

        function openEnd() {
            $scope.opened.end = true;
        }

        $scope.usersForSearch = [];
        $scope.roomsForSearch = [];


        $scope.dates = { start: new Date(), end: new Date() }
        $scope.dates.start.setMinutes(0);
        $scope.dates.start.setHours(0);
        $scope.dates.start.setDate($scope.dates.start.getDate() - 7); //week before
        $scope.dates.end.setMinutes(59);
        $scope.dates.end.setHours(23);
        $scope.openEnd = openEnd;
        $scope.openStart = openStart;

        $scope.form = { searchMode: "room" }

        $scope.opened = { start: false, end: false };
        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
        $scope.format = $scope.formats[0];

        var UsersList = [];
        var RoomsList = [];
        var searchBefore = '';
        var wait = false

        $scope.findUsers = function(info) {
            if (searchBefore != info && info.trim() != '' && getUsersInfoWithoutRole(info, function(data) {
                    UsersList = data
                })) {
                searchBefore = info;
            }
            return UsersList;
        }

        $scope.findRooms = function(info) {
            if (searchBefore != info && info.trim() != '' && getRoomsInfo(info, function(data) {
                    RoomsList = [];
                    for (var key of Object.keys(data)) {
                        RoomsList.push({ key: key, name: data[key] })
                    }
                })) {
                searchBefore = info;
            }
            return RoomsList;
        }



        function waitFunction(callBack, succ, error) {
            if (wait == false) {
                callBack.success(function(data, status, headers, config) {
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

        function getUsersInfoWithoutRole(info, succ, error) {
            return waitFunction(UserMonitorService.getChatUserWithNickNameLike(info), succ, error)
        }

        function getRoomsInfo(info, succ, error) {
            return waitFunction(UserMonitorService.getRoomsWithNameLike(info), succ, error);
        }

        $scope.supportedRatings = null;
        RatingsPageService.loadRatings().then(function(response) {
            $scope.supportedRatings = {};
            $scope.labels = [];
            for (var rating of response.data) {
                $scope.supportedRatings[rating.id] = rating.name;
                $scope.labels.push(rating.name); // init graphs labels
            }
        })

        $scope.ratingsInfoByCurrentRoom = [];
        $scope.loadRatingsInfoByRoom = function() {
            var is_user = false;
            var idList = [];
            debugger;
            switch ($scope.form.searchMode) {
                case "user":
                    {
                        is_user = true;
                        for (var i = 0; i < $scope.usersForSearch.length; i++) {
                            idList.push($scope.usersForSearch[i].chatUserId);
                        }
                        break;
                    }
                case "room":
                    {
                        idList = [$scope.roomsForSearch[0].key];
                    }
            }

            RatingsPageService.getRatingsByRoom({ room_user_ids_list: idList, is_user: is_user, before_date: new Date($scope.dates.end).getTime(), after_date: new Date($scope.dates.start).getTime() })
                .then(function(response) {
                    $scope.ratingsInfoByCurrentRoom = response.data;
                    calcAverageRatings(response.data);
                }, function() {});
        }




        var layoutColors = baConfig.colors;



        /************************************************
         ************************************************
         * Average data with calculation functionality
         ************************************************
         ************************************************/
        $scope.data = [];
        $scope.datasetOverride = [{ yAxisID: 'y-axis-1' }, { yAxisID: 'y-axis-2' }];
        $scope.options = {
            scales: {
                yAxes: [{
                    id: 'y-axis-1',
                    type: 'linear',
                    display: true,
                    position: 'left'
                }, {
                    id: 'y-axis-2',
                    type: 'linear',
                    display: true,
                    position: 'right'
                }]
            },
            elements: {
                arc: {
                    borderWidth: 0
                }
            },
            tooltips: {
                enabled: true
            },
            legend: {
                display: true,
                position: 'bottom',
                labels: {
                    fontColor: layoutColors.defaultText
                }
            }
        };
        /************************************************
         ************************************************
         * Average data with calculation functionality
         ************************************************
         ************************************************/
        function calcAverageRatings(ratings) {
            var averageRatingsCount = {};
            var averageRatingsValue = {};
            debugger;
            for (var rating of ratings) {
                for (var id in rating.values) {
                    if (averageRatingsCount[id] == undefined) {
                        averageRatingsCount[id] = 0;
                        averageRatingsValue[id] = rating.values[id];

                    }
                    averageRatingsCount[id] += rating.values[id];
                    averageRatingsValue[id] = (averageRatingsValue[id] + rating.values[id]) / 2;

                }
            }
            $scope.dataAverageCount = [];
            $scope.dataAverageValue = [];
            for (var id in $scope.supportedRatings) {
                $scope.dataAverageCount.push(averageRatingsCount[id]);
                $scope.dataAverageValue.push(averageRatingsValue[id].toFixed(2));
            }
        }

        $scope.labels = [];
        $scope.dataAverageCount = [];
        $scope.dataAverageValue = [];
        $scope.optionsAverageCount = {
            elements: {
                arc: {
                    borderWidth: 0
                }
            },
            legend: {
                display: true,
                position: 'bottom',
                labels: {
                    fontColor: layoutColors.defaultText
                }
            }
        };

        $scope.optionsAverageValue = {
            scales: {
                yAxes: [{
                    id: 'y-axis-1',
                    type: 'linear',
                    display: true,
                    position: 'left',
                    ticks: { min: 0, max: 10 }
                }]
            },
            elements: {
                arc: {
                    borderWidth: 0
                }
            },
            tooltips: {
                enabled: true
            }
        };


        function initTest() {
            $scope.roomsForSearch[0] = { key: 715 };
            $scope.loadRatingsInfoByRoom();
        }
      //  initTest();


    }

})();
