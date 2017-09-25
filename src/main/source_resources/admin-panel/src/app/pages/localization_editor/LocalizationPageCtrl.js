/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {
    

    angular.module('BlurAdmin.pages.localization_editor')
        .controller('LocalizationPageCtrl', LocalizationPageCtrl);

    function objectToArray(obj){
      var array = [];
      for (var prop in obj) {
        array.push({key:prop,value:obj[prop]});
      }
      return array;
    }

    function arrayToObject(arr) {
      var obj = {};
      for (var item of arr) {
        obj[item.key] = item.value;
      }
      return obj;
    }
    /** @ngInject */
    function LocalizationPageCtrl($scope,$http) {
     

     // $scope.editableTableData = [];

          $http.get(serverPrefix + "/chat/user/localization?lang=ua").then(function(payload, status, headers, config) {
          var localizationObj = payload.data;
          var dottedObject = DotObject.dot(localizationObj);
            $scope.editableTableData = objectToArray(dottedObject);
          });
    
                $scope.saveChanges = function() {
      var objToSave = arrayToObject( $scope.editableTableData );
      var undottedData = DotObject.object(objToSave);
      var dataStr = JSON.stringify(undottedData);
       $http.post(serverPrefix + "/chat/user/localization?lang=ua",dataStr);
    }

    }



})();
