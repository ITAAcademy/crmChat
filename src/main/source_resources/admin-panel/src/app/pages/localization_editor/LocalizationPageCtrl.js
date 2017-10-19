/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function() {


  angular.module('BlurAdmin.pages.localization_editor')
    .controller('LocalizationPageCtrl', LocalizationPageCtrl);

  function objectToArray(obj) {
    var array = [];
    for (var prop in obj) {
      array.push({
        key: prop,
        value: obj[prop]
      });
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
  function LocalizationPageCtrl($scope, $http) {
    $scope.editableTableData = [];
    $scope.rowCollection = [];
    $scope.smartTablePageSize = 10;

    // $scope.editableTableData = [];

    $http.get(serverPrefix + "/chat/user/localization?lang=ua").then(function(payload, status, headers, config) {
      var localizationObj = payload.data;
      var dottedObject = DotObject.dot(localizationObj);
      $scope.editableTableData = objectToArray(dottedObject);
      $scope.rowCollection = [].concat($scope.editableTableData);
    });

    $scope.selected = {locale: 'ua'};
    $scope.locals = ['ua','ru','en'];
    loadLang($scope.locals[0]);

    $scope.saveChanges = function(lang) {
      lang = lang || $scope.selected.locale;
      var objToSave = arrayToObject($scope.editableTableData);
      var undottedData = DotObject.object(objToSave);
      var dataStr = JSON.stringify(undottedData);
      $http.post(serverPrefix + "/chat/user/localization?lang="+lang, dataStr);
    }

    $scope.addRecord = function(){
      $scope.inserted = {
        /*id: $scope.editableTableData.length+1,*/
        key: null,
        value: null
      };
      $scope.editableTableData.push($scope.inserted);
    }
    $scope.removeRecord = function(record) {
      var index = $scope.editableTableData.indexOf(record);
      $scope.editableTableData.splice(index, 1);
    };
    $scope.langLoading = false;

     function loadLang(lang) {
      $scope.langLoading = true;
      lang = lang || $scope.selected.locale;
      $http.get(serverPrefix + "/chat/user/localization?lang="+lang).then(function(payload, status, headers, config) {
      var localizationObj = payload.data;
      var dottedObject = DotObject.dot(localizationObj);
      $scope.editableTableData = objectToArray(dottedObject);
      $scope.rowCollection = [].concat($scope.editableTableData);
      $scope.langLoading = false;
    });
    }

    $scope.onLangSelect = function(lang){
      loadLang(lang);
    }


  }



})();