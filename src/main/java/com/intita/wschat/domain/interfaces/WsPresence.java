package com.intita.wschat.domain.interfaces;

import java.util.Date;

public class WsPresence implements IPresentOnForum {
int connectionsCount;

	public WsPresence(){
		connectionsCount = 1;
	}

	@Override
	public boolean isPresent() {
		if (connectionsCount <= 0) return false;
		return true;
	}

	@Override
	public void setLastPresenceTime(Date date) {		
		//Not used for websockets
	}

	@Override
	public void addConnectionsCount(int count) {
		connectionsCount += count;
		
	}

	@Override
	public void decreaseConnectionsCount(int count) {
		connectionsCount -= count;
		
	}

	@Override
	public boolean isTimeBased() {
		return false;
	}

	@Override
	public boolean isConnectionBased() {
		return true;
	}


}
