package com.intita.wschat.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

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

	private ConcurrentSkipListSet<String> activeSessions = new ConcurrentSkipListSet<>();

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
