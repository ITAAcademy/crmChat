package com.intita.wschat.models;

public class OperationStatus {
	public OperationType getType() {
		return type;
	}
	public void setType(OperationType type) {
		this.type = type;
	}
	public enum OperationType {NONE,SEND_MESSAGE_TO_ALL,SEND_MESSAGE_TO_USER}
	public OperationStatus(){
		
	}
public OperationStatus(OperationType type,boolean success, String description){
	this.success=success;
	this.description=description;
	this.type=type;
}
public boolean isSuccess() {
	return success;
}
public void setSuccess(boolean success) {
	this.success = success;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
private boolean success;
private String description;
private OperationType type;

}
