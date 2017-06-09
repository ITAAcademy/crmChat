springChatServices.factory('StateFactory', ['RoomsBlockTabState', function(RoomsBlockTabState) {
	var roomBlockMode = 1;
	var userListForAddedToCurrentRoom = [];
	var roomBlockTabState = RoomsBlockTabState.contacts;

	function setTabStateContacts(){
		roomBlockTabState = RoomsBlockTabState.contacts;
	}
	function setTabStateLastRooms(){
		roomBlockTabState = RoomsBlockTabState.lastrooms
	}
	function isTabStateContacts(){
		return roomBlockTabState == RoomsBlockTabState.contacts;
	}
	function isTabStateLastRooms(){
		return roomBlockTabState == RoomsBlockTabState.lastrooms;
	}
	function getTabState(){
		return roomBlockTabState;
	}

	function getUserListForAddedToCurrentRoom(){
		return userListForAddedToCurrentRoom;
	}

	var setDefaultRoomBlockMode = function(){
		roomBlockMode = 1;
	}
	var setCreateRoomBlockMode = function(){
		roomBlockMode = 2;
		userListForAddedToCurrentRoom = [];
	}
	var toggleCreateRoomBlockMode = function(){
		if (isCreateRoomBlockMode()){
			setDefaultRoomBlockMode();
		}
		else {
			setCreateRoomBlockMode();
		}
	}
	var setAddUserToDialogRoomBlockMode = function (){
		roomBlockMode = 3;
		userListForAddedToCurrentRoom = [];
		setTabStateContacts();
	}
	var toggleAddUserToDialogRoomBlockMode = function(){
		if (isAddUserToDialogRoomBlockMode()){
			setDefaultRoomBlockMode();
		}
		else {
			setAddUserToDialogRoomBlockMode();
		}
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
		getUserListForAddedToCurrentRoom,
		setDefaultRoomBlockMode,
		setCreateRoomBlockMode,
		setAddUserToDialogRoomBlockMode,
		setRoomBlockMode,
		getRoomBlockMode,
		isDefaultRoomBlockMode,
		isCreateRoomBlockMode,
		isAddUserToDialogRoomBlockMode,
		toggleCreateRoomBlockMode,
		toggleAddUserToDialogRoomBlockMode,
		setTabStateContacts,
		setTabStateLastRooms,
		isTabStateContacts,
		isTabStateLastRooms,
		getTabState

	}
	return StateFactory;

}]);