package com.intita.wschat.event;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.intita.wschat.models.ConnectionEvent;

/**
 * 
 * @author Nicolas
 */

@Component
public class ParticipantRepository {
	final long PRESENCE_TIMEOUT_SECONDS = 30;
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	public ConcurrentSkipListMap<String,ConnectionEvent> getActiveSessions() {
		return activeSessions;
	}

	private ConcurrentSkipListMap<String,ConnectionEvent> activeSessions = new ConcurrentSkipListMap<String,ConnectionEvent>();
	//put null to distinguish WS from LP sessions. We need no last action time for Web Sockets
	public void addWS(String chatId) {
		if (activeSessions.containsKey(chatId))
			activeSessions.putIfAbsent(chatId, new ConnectionEvent(true));
		activeSessions.put(chatId,new ConnectionEvent(true));
	}
	public void addLP(String chatId) {
		if (activeSessions.containsKey(chatId))
			activeSessions.putIfAbsent(chatId, new ConnectionEvent(false));
		activeSessions.put(chatId,new ConnectionEvent(false));
	}

	public boolean isOnline(String chatId) {
		boolean containsId = activeSessions.containsKey(chatId);
		if(containsId){
			ConnectionEvent conEvent = activeSessions.get(chatId);
			if (conEvent.isWs())return true;
			Date date = conEvent.getDate();
			long difference = getDateDiff(date,new Date(),TimeUnit.SECONDS);
			if (difference<=PRESENCE_TIMEOUT_SECONDS)return true;
			else {
				return false;
			}
		}
		else return false;
	}

	public void removeParticipant(String chatId) {
		activeSessions.remove(chatId);
	}
}
