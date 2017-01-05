package com.intita.wschat.services;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.LangId;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.RoomRepository;
import com.intita.wschat.repositories.UserMessageRepository;
import com.intita.wschat.web.BotController;
import com.intita.wschat.web.ChatController;

@Service
public class UserMessageService {

	@Autowired
	private UserMessageRepository userMessageRepository;

	@Autowired 
	private ChatUsersService chatUserService;

	@Autowired
	private RoomsService roomsService;

	@Autowired
	private BotCategoryService botCategoryService;

	@Autowired
	private BotItemContainerService botItemContainerService;

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getUserMesagges(){
		return (ArrayList<UserMessage>) IteratorUtils.toList(userMessageRepository.findAll().iterator());
	}
	@Transactional(readOnly=true)
	public UserMessage getUserMessage(Long id){
		return userMessageRepository.findOne(id);
	}
	/*@Transactional(readOnly=true)
	public ArrayList<UserMessage> getChatUserMessagesByAuthor(String author) {

		return userMessageRepository.findByAuthor(chatUserService.getChatUser(author));
	}*/
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getChatUserMessagesById(Long id) {

		return wrapBotMessages(userMessageRepository.findByAuthor(chatUserService.getChatUser(id)));
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getUserMessagesByRoom(Room room) {
		return wrapBotMessages(userMessageRepository.findByRoom(room));
	}
	@Transactional(readOnly=true)
	public UserMessage getLastUserMessageByRoom(Room room){
		return userMessageRepository.findFirstByRoomOrderByDateDesc(room);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room) {
		return wrapBotMessages(userMessageRepository.findFirst20ByRoomOrderByIdDesc(room));
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getFirst20UserMessagesByRoomIdContains(Long roomId,String searchQuery) {
		return wrapBotMessages(userMessageRepository.findFirst20ByRoomAndBodyIgnoreCaseContainingOrderByIdDesc(
				new Room(roomId),searchQuery));
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room, String lang) {
		return wrapBotMessages(userMessageRepository.findFirst20ByRoomOrderByIdDesc(room), lang);
	}

	public ArrayList<UserMessage> getUserMessagesByRoomId(Long roomId) {

		return wrapBotMessages(userMessageRepository.findByRoom(new Room(roomId)));
	}

	@Transactional()
	public boolean addMessage(ChatUser user, Room room,String body) {
		if(user == null || room == null || body == null) return false;
		//have premition?
		UserMessage userMessage = new UserMessage(user,room,body);
		userMessageRepository.save(userMessage);
		return true;
	}
	@Transactional()
	public boolean addMessage(UserMessage message) {
		if (message==null) return false;
		userMessageRepository.save(message);
		return true;
	}
	@Transactional()
	public boolean addMessages(Iterable<UserMessage> messages) {
		if (messages==null) return false;
		userMessageRepository.save(messages);
		return true;
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByDate(Date date) {
		return userMessageRepository.findAllByDateAfter(date);
	}

	public static boolean isNumber(String str)  
	{  
		try  
		{  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}


	@SuppressWarnings("deprecation")
	public boolean isValidJSON(final String json) {
		boolean valid = false;
		try {
			final JsonParser parser = new ObjectMapper().getJsonFactory()
					.createJsonParser(json);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (JsonParseException jpe) {
			// jpe.printStackTrace();
		} catch (IOException ioe) {
			// ioe.printStackTrace();
		}

		return valid;
	}
	public ArrayList<UserMessage> wrapBotMessages(ArrayList<UserMessage> input_messages) {
		String lang = ChatController.getCurrentLang();
		return wrapBotMessages(input_messages, lang);
	}

	public ArrayList<UserMessage> wrapBotMessages(ArrayList<UserMessage> input_messages, String lang) {
		ArrayList<UserMessage> result = new ArrayList<>();
		for (UserMessage message : input_messages) {
			if (message.getAuthor().getId().equals(BotController.BotParam.BOT_ID) && isValidJSON(message.getBody()))
			{
				String originalBody = message.getBody();

				{
					ObjectMapper mapper = new ObjectMapper();
					JsonNode json = null;

					try {
						json = mapper.readTree(originalBody);
					} catch (JsonProcessingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					if (json != null) {
						JsonNode body_body =  json.get("id");
						if (body_body != null) {
							String body_json = body_body.asText();

							if (body_json != null) {
								if (isNumber(body_json))
								{
									Long id = new Integer(body_json).longValue();
									BotDialogItem botDialogItem = botItemContainerService.getByObjectId(new LangId(id, lang));
									if (botDialogItem != null) {
										/*String body = botDialogItem.getBody();
										if (!body.isEmpty()) {
											ObjectNode jsonNode = (ObjectNode)json;
											jsonNode.put("body", body); 
											String res_json_str = jsonNode.toString();
											message.setBody(res_json_str);
										}*/
										try {
											String res_json_str = mapper.writeValueAsString(botDialogItem);
											message.setBody(res_json_str);
										} catch (JsonProcessingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
			result.add(message);
		}
		return result;
	}

	public static boolean isStringJson(String jsonInString ) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(jsonInString);
			return true;
		} catch (IOException e) {
			return false;
		}
	}




	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByRoomAndDateAfter(room, date);
		return  wrapBotMessages(messages);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> get10MessagesByRoomDateAfter(Room room, Date date){
		ArrayList<UserMessage> messages =  userMessageRepository.findFirst10ByRoomAndDateAfter(room, date);
		return  wrapBotMessages(messages);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> get10MessagesByRoomDateBefore(Room room, Date date){		
		ArrayList<UserMessage> messages =  userMessageRepository.findFirst10ByRoomAndDateBeforeOrderByIdDesc(room, date);
		return  wrapBotMessages(messages);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> get10MessagesByRoomDateBeforeAndBodyContains(Room room, Date date,String body){
		ArrayList<UserMessage> messages = (body==null || body.length()<1) ?
				userMessageRepository.findFirst10ByRoomAndDateBeforeOrderByIdDesc(room, date) : userMessageRepository.
				findFirst10ByRoomAndDateBeforeAndBodyIgnoreCaseContainingOrderByIdDesc(room, date,body);
		return  wrapBotMessages(messages);
	}
    @Transactional(readOnly=true)
    public ArrayList<UserMessage> get10MessagesByRoomAndBodyContains(Room room, Date date,String body){
        ArrayList<UserMessage> messages = (body==null || body.length()<1) ?
                userMessageRepository.findFirst10ByRoomAndDateBeforeOrderByIdDesc(room, date) : userMessageRepository.
                findFirst10ByRoomAndDateBeforeAndBodyIgnoreCaseContainingOrderByIdDesc(room, date,body);
        return  wrapBotMessages(messages);
    }

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDateNotUser(Room room, Date date, ChatUser user)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByRoomAndDateAfterAndAuthorNot(room, date, user);
		return  wrapBotMessages(messages);
	}

	@Transactional(readOnly=true)
	public Long getMessagesCountByRoomDateNotUser(Room room, Date date, ChatUser user)  {
		Long messages_count =  userMessageRepository.countByRoomAndDateAfterAndAuthorNot(room, date, user);
		return  messages_count;
	}

	@Transactional(readOnly=true)
	public List<UserMessage> getMessagesByDateNotUser(Date date, ChatUser user) {
		List<UserMessage> messages =  userMessageRepository.findAllByDateAfterAndAuthorNot( date, user);
		ArrayList<UserMessage> result =  wrapBotMessages( new ArrayList<UserMessage>(messages));
		return result;
	}

	@Transactional(readOnly=true)
	public Set<UserMessage> getMessagesByNotUser( ChatUser user) {
		Set<UserMessage> messages = userMessageRepository.findAllByAuthorNot( user); 
		ArrayList<UserMessage> result =  wrapBotMessages( new ArrayList<UserMessage>(messages));
        return new HashSet<UserMessage>(result);
	}

}
