/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    'use strict';

    angular.module('BlurAdmin.pages.trainer_tenant_monitoring')
        .controller('RatingsPageCtrl', RatingsPageCtrl);

    /** @ngInject */
    function RatingsPageCtrl($scope, $timeout, UserMonitorService, RatingsPageService, $http, baConfig, $window,CommonOperationsService) {
        var fetchUsers = function(info) {
             CommonOperationsService.fetchUsers(info).then(function(payload){
                $scope.usersList.length = 0;
            // $scope.usersList = payload.data == null ? null : payload.data[userIndex];
             $scope.usersList.push.apply($scope.usersList,payload.data);
            });
        }
        $scope.fetchUsers = fetchUsers;
        $scope.test = "test OK";

        function openStart() {
            $scope.opened.start = true;
        }

        function openEnd() {
            $scope.opened.end = true;
        }

        $scope.usersList = [];
        $scope.roomsList = [];
        $scope.selected = { users: [], room: null };


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


        $scope.fetchRooms = function(info) {
           return fetchRoomsInfo(info).then(function(payload){
            var data = payload.data;
                    $scope.roomsList.length=0;
                    var keys = Object.keys(data);
                    for (var i = 0; i < keys.length; i++) {
                        var key = keys[i];
                        $scope.roomsList.push({ key: key, name: data[key] })
                    }
           });
                
            }

        function fetchRoomsInfo(info) {
            return UserMonitorService.fetchRoomsWithNameLike(info);
        }

        $scope.supportedRatings = null;
        RatingsPageService.loadRatings().then(function(response) {
            $scope.supportedRatings = {};
            $scope.labels = [];
            var ratings = response.data;
            for (var i = 0; i < ratings.length; i++) {
                var rating = ratings[i];
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
                        for (var i = 0; i < $scope.selected.users.length; i++) {
                            idList.push( $scope.selected.users[i].chatUserId);
                        }
                        break;
                    }
                case "room":
                    {
                        idList = [$scope.selected.room.key];
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

            for (var i = 0; i < ratings.length; i++) {
                var rating = ratings[i];
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
