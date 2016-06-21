package com.intita.wschat.event;

import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.stereotype.Component;


/**
 * 
 * @author Nicolas
 */

@Component
public class ParticipantRepository {

	public ConcurrentSkipListMap<String,Integer> getActiveSessions() {
		return activeSessions;
	}

	private ConcurrentSkipListMap<String,Integer> activeSessions = new ConcurrentSkipListMap<String,Integer>();
	//put null to distinguish WS from LP sessions. We need no last action time for Web Sockets
	public void add(String chatId) {
		if (activeSessions.containsKey(chatId)){
			int presenceIndex = activeSessions.get(chatId);
			activeSessions.put(chatId, presenceIndex+1);
			System.out.println("presence increased to:"+(presenceIndex+1));
		}
		else{
	activeSessions.put(chatId,1);
	System.out.println("presence added with start value:"+1);
		}
		
	}

	public boolean isOnline(String chatId) {
		boolean containsKey = activeSessions.containsKey(chatId);
		Integer getId = activeSessions.get(chatId);
		
		boolean online = containsKey &&  getId > 0;
		//System.out.println("isOnline "+chatId+" ? "+ online);
		return online;
	}

	public void removeParticipant(String chatId) {
		if (activeSessions.containsKey(chatId)){
			int presenceIndex = activeSessions.get(chatId);
			if (presenceIndex<=0)
			{
				activeSessions.remove(chatId);
				System.out.println("presence removed");
			}
			else
			{
			activeSessions.put(chatId, presenceIndex-1);
			System.out.println("presence decreased to:"+(presenceIndex-1));
			}
		}
	}
}
