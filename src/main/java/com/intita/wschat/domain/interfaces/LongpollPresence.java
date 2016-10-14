package com.intita.wschat.domain.interfaces;

import java.util.Date;

public class LongpollPresence implements IPresentOnForum{
	Long lastPresenceTime = null;
	
	public LongpollPresence(){
		this.lastPresenceTime = new Date().getTime();
	}
	@Override
	public boolean isPresent() {
		if (new Date().getTime() - lastPresenceTime > PRESENCE_TIMEOUT_MS){
			return false;
		}
		return true;
	}

	@Override
	public void setLastPresenceTime(Date date) {
		this.lastPresenceTime = date.getTime();		
	}

	@Override
	public void addConnectionsCount(int count) {
		//Not Used for Long Polling
		
	}
	@Override
	public void decreaseConnectionsCount(int count) {
		//Not Used for Long Polling
		
	}
	@Override
	public boolean isTimeBased() {
		return true;
	}
	@Override
	public boolean isConnectionBased() {
		// TODO Auto-generated method stub
		return false;
	}


}
