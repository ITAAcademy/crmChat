package com.intita.wschat.domain.interfaces;

import java.util.Date;

public interface IPresentOnForum {
	final long PRESENCE_TIMEOUT_MS = 20000L;
	boolean isPresent();
	void setLastPresenceTime(Date date);
	void addConnectionsCount(int count);
	void decreaseConnectionsCount(int count);
	boolean isTimeBased();
	boolean isConnectionBased();
}
