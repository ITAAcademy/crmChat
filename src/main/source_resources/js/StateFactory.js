springChatServices.factory('StateFactory', [ function() {
var roomBlockMode = 1;
var setDefaultRoomBlockMode = function(){
roomBlockMode = 1;
}
var setCreateRoomBlockMode = function(){
roomBlockMode = 2;
}
var setAddUserToDialogRoomBlockMode = function (){
	roomBlockMode = 3;
}
var setRoomBlockMode = function(val){
roomBlockMode = val;
}
var getRoomBlockMode = function(){
	return roomBlockMode;
}
var isDefaultRoomBlockMode = function() {
	return roomBlockMode == 1;
}
var isCreateRoomBlockMode = function() {
	return roomBlockMode == 2;
}
var isAddUserToDialogRoomBlockMode = function (){
	return roomBlockMode == 3;
}

var StateFactory = {
setDefaultRoomBlockMode,
setCreateRoomBlockMode,
setRoomBlockMode,
getRoomBlockMode,
isDefaultRoomBlockMode,
isCreateRoomBlockMode
}
return StateFactory;

}]);