function getPropertyByValue(obj, value ) {
	for( var prop in obj ) {
		if( obj.hasOwnProperty( prop ) ) {
			if( obj[ prop ] === value )
				return prop;
		}
	}
}


function getIdInArrayFromObjectsMap(roomNameMap,propertyName,valueToFind){

	for (var item in roomNameMap)
		if(roomNameMap[item][propertyName]==valueToFind) return item;
	return undefined;
}

function getRoomById(rooms,id){

	for(var i =0; i < rooms.length; i++){
		if (rooms[i].roomId==id) return rooms[i];
	}
	return undefined;
}

var Operations = Object.freeze({"send_message_to_all":"SEND_MESSAGE_TO_ALL",
	"send_message_to_user":"SEND_MESSAGE_TO_USER",
	"add_user_to_room":"ADD_USER_TO_ROOM",
	"add_room":"ADD_ROOM"});

var serverPrefix = "/crmChat";
var DEFAULT_FILE_PREFIX_LENGTH = 15;