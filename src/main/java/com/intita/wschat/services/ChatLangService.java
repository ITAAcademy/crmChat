package com.intita.wschat.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.web.ChatController;

@Service
@Transactional
public class ChatLangService {

	private final static Logger log = LoggerFactory.getLogger(ChatController.class);
	@Autowired
	private ChatLangRepository chatLangRepository;
	private Map<String,Map<String,Object>> localizationMap = new HashMap<>();

	public static class ChatLangEnum{ 
		public static final String UA = "ua";
		public static final String EN = "en";
		public static final String RU = "ru";
		public static final ArrayList<String> LANGS = new ArrayList<String>(
				Arrays.asList(UA, EN, RU));

	}
	public Map<String,Map<String,Object>> getLangFromDatabase(){
		Map<String,Map<String,Object>> langMap = new HashMap<>();
		Iterable<Lang> it = chatLangRepository.findAll();
		for(Lang lg:it)
		{
			HashMap<String, Object> result = null;
			JsonFactory factory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

			TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};

			try {
				result = mapper.readValue(lg.getMap(), typeRef);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Lang " + lg.getLang() + " is wrong!!!");
				e.printStackTrace();
			}
			langMap.put(lg.getLang(), result);
			log.info("Current lang pack" + langMap.toString());

		}
		return langMap;
	}

	public Map<String,Map<String,Object>> saveLangToDatabase(String lang, String value){
		Map<String,Map<String,Object>> langMap = new HashMap<>();
		chatLangRepository.updateMap(value,lang);
		return langMap;
	}
	
	public  Map<String,Map<String,Object>> updateDataFromDatabase()
	{
		Map<String,Map<String,Object>> langMap = getLangFromDatabase();
		localizationMap = langMap;
		return langMap;
	}
	
	public static String getCurrentLang()
	{
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession session = attr.getRequest().getSession(false);
		String lg;
		if(session != null)
			lg = (String) session.getAttribute("chatLg");
		else
			lg = "ua";
		if(lg == null)
			return "ua";
		return lg;
	}

	public int getCurrentLangInt()
	{
		String lang = getCurrentLang();
		if (lang.equals(("ua")))
			return 0;
		if (lang.equals(("ru")))
			return 1;
		return 2;
	}

	public Map<String,Map<String,Object>> getLocalizationMap(){
		return localizationMap;
	}
	
	public Map<String, Object> getLocalization()
	{
		return (Map<String, Object>) getLocalizationMap().get(getCurrentLang());
	}
	public Map<String, Object> getLocalization(String lang)
	{
		return (Map<String, Object>) getLocalizationMap().get(lang.toLowerCase());
	}

	
	@PostConstruct
	private void initService(){

	}
}
