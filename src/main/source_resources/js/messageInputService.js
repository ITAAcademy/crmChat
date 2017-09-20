springChatServices.service('MessageInputService', function() {
    this.updateMessageInputText = function(){

    }
    var messageBodySetter = null;
    var messsageAttachesSetter = null;
    this.setMessageBodySetter = function(callback){
    	messageBodySetter = callback;
    }
    this.setMessageAttachesSetter = function(callback) {
    	messsageAttachesSetter = callback;
    }
    this.setMessageBody = function(body){
    	if (messageBodySetter!=null) {
    		messageBodySetter(body);
    	}
    }
    this.setMessageAttaches = function(attaches){
    	if (messsageAttachesSetter!=null) {
    		messsageAttachesSetter(attaches);
    	}
    }


});