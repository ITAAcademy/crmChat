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

/*
 * FILE UPLOAD
 */
function uploadXhr(files, urlpath, successCallback, onProgress){

	 var xhr = getXmlHttp();
	
// обработчик для закачки
xhr.upload.onprogress = function(event) {
  //console.log(event.loaded + ' / ' + event.total);
  onProgress(event);
}

// обработчики успеха и ошибки
// если status == 200, то это успех, иначе ошибка
xhr.onload = xhr.onerror  = function() {
  if (this.status == 200) {
    console.log("SUCCESS:"+xhr.responseText);
    successCallback(xhr.responseText);
  } else {
    console.log("error " + this.status);
  }
};

xhr.open("POST", urlpath);
var boundary = String(Math.random()).slice(2);
//xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
var formData=new FormData();

	for (var i = 0; i < files.length; i++){
		formData.append("file"+i,files[i]);
	}
xhr.send(formData);

}

function upload($http,files,urlpath){
	var formData=new FormData();
	for (var i = 0; i < files.length; i++){
		formData.append("file"+i,files[i]);
	}

	return $http.post(urlpath, formData, {
		transformRequest: function(data, headersGetterFunction) {
			return data;
		},
		headers: { 'Content-Type': undefined }
	}).error(function(data, status) {
		console.log("Error ... " + status);
	});
}


function getXmlHttp(){
		var xmlhttp;
		try {
			xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (E) {
				xmlhttp = false;
			}
		}
		if (!xmlhttp && typeof XMLHttpRequest!='undefined') {
			xmlhttp = new XMLHttpRequest();
		}
		return xmlhttp;
	}
/*function upload(file,urlpath) {

var xhr = new XMLHttpRequest();

// обработчик для закачки
xhr.upload.onprogress = function(event) {
  log(event.loaded + ' / ' + event.total);
}

// обработчики успеха и ошибки
// если status == 200, то это успех, иначе ошибка
xhr.onload = xhr.onerror = function() {
  if (this.status == 200) {
    log("success");
  } else {
    log("error " + this.status);
  }
};

xhr.open("POST", urlpath, true);
xhr.setRequestHeader('Content-Type', 'multipart/form-data')
xhr.send(file);

}*/

/*
 * CONST
 */
var Operations = Object.freeze({"send_message_to_all":"SEND_MESSAGE_TO_ALL",
	"send_message_to_user":"SEND_MESSAGE_TO_USER",
	"add_user_to_room":"ADD_USER_TO_ROOM",
	"add_room":"ADD_ROOM"});

var serverPrefix = "/crmChat";
var DEFAULT_FILE_PREFIX_LENGTH = 15;