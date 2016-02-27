package com.intita.wschat.models;

import java.util.Date;

public class ConnectionEvent {
public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public boolean isWs() {
		return ws;
	}
	public void setWs(boolean ws) {
		this.ws = ws;
	}
private Date date;
private boolean ws;
public ConnectionEvent(boolean ws,Date date){
	this.ws=ws;
	this.date=date;
}
public ConnectionEvent(boolean ws){
	this.ws=ws;
	this.date=new Date();
}
}
