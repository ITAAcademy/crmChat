package com.intita.wschat.event;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;


/**
 * 
 * @author Nicolas
 */

@Component
public class ParticipantRepository {

	public ConcurrentSkipListSet<String> getActiveSessions() {
		return activeSessions;
	}

	private ConcurrentSkipListSet<String> activeSessions = new ConcurrentSkipListSet<String>();
	//put null to distinguish WS from LP sessions. We need no last action time for Web Sockets
	public void add(String chatId) {
	activeSessions.add(chatId);
	}

	public boolean isOnline(String chatId) {
		return activeSessions.contains(chatId);
	}

	public void removeParticipant(String chatId) {
		activeSessions.remove(chatId);
	}
}
