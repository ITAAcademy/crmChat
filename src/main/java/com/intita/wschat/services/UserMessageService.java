package com.intita.wschat.services;


import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import com.intita.wschat.domain.UserRole;
import com.intita.wschat.domain.search.UserMessageSearchCriteria;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.UserMessageDTO;
import com.intita.wschat.exception.OperationNotAllowedException;
import com.intita.wschat.models.*;
import com.intita.wschat.util.TimeUtil;
import org.apache.commons.collections4.IteratorUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.repositories.UserMessageRepository;
import com.intita.wschat.web.BotController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Service
public class UserMessageService {

	@Autowired private UserMessageRepository userMessageRepository;
	@Autowired private ChatUsersService chatUserService;
	@Autowired private RoomsService roomsService;
	@Autowired private BotCategoryService botCategoryService;
	@Autowired private BotItemContainerService botItemContainerService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired private ChatLangService chatLangService;
	@Autowired private RoomHistoryService roomHistoryService;
	@Autowired private DTOMapper dtoMapper;

	@PersistenceContext
	EntityManager entityManager;

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getUserMesagges(){
		return (ArrayList<UserMessage>) IteratorUtils.toList(userMessageRepository.findAll().iterator());
	}
	@Transactional(readOnly=true)
	public UserMessage getUserMessage(Long id){
		return userMessageRepository.findOne(id);
	}

	@Transactional
	public UserMessage disableMessage(UserMessage message){
		message.setActive(false);
		message.setUpdateat(new Date());
		return userMessageRepository.save(message);
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
	@Transactional(readOnly=false)
	public void removeAllUserMessagesByRoom(Room room) {
		userMessageRepository.delete(userMessageRepository.findByRoom(room));
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
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room, String lang,ChatUser user) {
		Date clearDate = roomHistoryService.getHistoryClearDate(room.getId(), user.getId());
		if (clearDate==null)
			return wrapBotMessages(userMessageRepository.findFirst20ByRoomOrderByIdDesc(room), lang);
		else
			return wrapBotMessages(userMessageRepository.findFirst20ByRoomAndDateAfterOrderByIdDesc(room, clearDate), lang);
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
	public UserMessage addMessage(UserMessage message) {
		if (message==null) return null;
		return userMessageRepository.save(message);
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

	public ArrayList<UserMessage> getMessagesBetweenDatesByAuthor(Date earlyDate, Date laterDate,ChatUser user) {
		return userMessageRepository.findAllByAuthorAndDateIsBetween(user,earlyDate,laterDate);
	}
	public ArrayList<UserMessage> getMessagesBetweenDatesByAuthorAndRoom(Date earlyDate, Date laterDate,ChatUser user,Room room) {
		return userMessageRepository.findAllByAuthorAndRoomAndDateIsBetween(user,room, earlyDate,laterDate);
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
		String lang = chatLangService.getCurrentLang();
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

	@Transactional
	public ArrayList<UserMessage> getMessages(Long roomId, Date beforeDate,
		Date afterDate, String body,boolean filesOnly,boolean bookmarkedOnly,int messagesCount,boolean activeOnly,ChatUser chatUser){
		if (roomId==null) return new ArrayList<UserMessage>();
		System.out.println("roomId:"+roomId);
		ArrayList<UserMessage> messages;

		String whereParam = "";

		// Construct WHERE part
		whereParam += "m.room.id = :roomId";
		if (beforeDate!=null) {
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam +=  "m.date < :beforeDate";
		}
		if(afterDate != null) {
			if(whereParam.length() > 0) whereParam += " AND ";
			whereParam += "m.date >= :afterDate";
		}
		if (body!=null){
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam += "lower(m.body) like lower(:body)";
		}
		if (filesOnly){
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam += "m.attachedFilesJson is not null AND m.attachedFilesJson != :emptyJsonObject";
		}
		if (activeOnly){
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam += "m.active = true";
		}
		if (bookmarkedOnly) {
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam += " m.id IN (SELECT bookmark.chatMessage.id FROM ChatUserMessageBookmark bookmark WHERE bookmark.chatUser = :bookMarkOwner) ";
		}
		String wherePart = "WHERE "+ whereParam;
		String orderPart = " ORDER BY m.date DESC,m.id";


		String hql = "SELECT m FROM chat_user_message m " + " "+wherePart + orderPart;
		System.out.println("hql:"+hql);
		Query query = entityManager.createQuery(hql);
		query.setMaxResults(messagesCount);

		//set params
		if(roomId!=null)
			query.setParameter("roomId",roomId);
		if (beforeDate!=null)
			query.setParameter("beforeDate", beforeDate);
		if (afterDate!=null)
			query.setParameter("afterDate", afterDate);
		if (body!=null)
			query.setParameter("body", "%"+body+"%");
		if (filesOnly){
			query.setParameter("emptyJsonObject", "[]");
		}
		if (bookmarkedOnly && chatUser!=null) {
			query.setParameter("bookMarkOwner", chatUser);
		}

		messages = new ArrayList<>(query.getResultList());
		/*System.out.println("Before date:"+beforeDate);
		for (UserMessage m : messages){
			System.out.println("message:"+m.getBody() + " "+m.getDate());
		}*/
		return wrapBotMessages(messages);
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDateNotUser(Room room, Date date, ChatUser user)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByDateAfter(date);
		return  wrapBotMessages(messages);
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByRoomAndDateAfterAndActiveIsTrue(room, date);
		return  wrapBotMessages(messages);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date, String lang)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByRoomAndDateAfterAndActiveIsTrue(room, date);
		return  wrapBotMessages(messages, lang);
	}

	@Transactional(readOnly=true)
	public Long getMessagesCountByRoomDateNotUser(Room room, Date date, ChatUser user)  {
		Long messages_count =  userMessageRepository.countByRoomAndDateAfterAndAuthorNot(room, date, user);
		return  messages_count;
	}

	public Long getMessagesCountByDate(Date date,boolean onlyActive)
	{
		final Long msInDay = 1000L * 60 * 60 * 24;
		Long time = date.getTime();
		Long timeShortenedToMidnight = TimeUtil.removeTime(time);
		Long nextDayLong = timeShortenedToMidnight + msInDay;
		Date dateMidnight = new Date(timeShortenedToMidnight);
		Date nextDay = new Date(nextDayLong);
		if (onlyActive) {
			return userMessageRepository.countByDateAfterAndDateBeforeAndActive(dateMidnight, nextDay,true);
		}
		else {
			return userMessageRepository.countByDateAfterAndDateBefore(dateMidnight, nextDay);
		}
	}


	public Map<String,Integer> countMessageByAllRolesAndBetweenDate(Date date1, Date date2) {
		Map<String,Integer> messagesByRoles = new HashMap<String,Integer>();

		String request = "select count(id) from chat_user_message  " +
				" where m >= :date1 and m <= :date2 and m.author_id IN (SELECT id FROM :roleTableName) )";

		for (UserRole role : UserRole.values()) {
			Query query = entityManager.createNativeQuery(request);
			query.setParameter("roleTableName",role.getTableName());
			messagesByRoles.put(role.getTableName(),query.getFirstResult());
		}

		return messagesByRoles;

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

	@Transactional
	public Map<Room ,List<UserMessageDTO>> getAllUnreadedMessages(ChatUser user){
		List<ChatUserLastRoomDate> userRooms = chatLastRoomDateService.getUserLastRoomDates(user);
		Map<Room, List<UserMessageDTO>> result = new  HashMap<Room, List<UserMessageDTO>>();
		for (ChatUserLastRoomDate lastRoomEntry : userRooms){
			Room room = lastRoomEntry.getRoom();
			List<UserMessage> unreadedMessages = getMessagesByRoomDate(room, lastRoomEntry.getLastLogout(), "ua");
			List<UserMessageDTO> unreadedChatMessages = dtoMapper.mapListUserMessage(unreadedMessages);
			if(unreadedChatMessages.size()>0)
				result.put(room, unreadedChatMessages);
		}
		return result;
	}
	public Map<Room ,List<UserMessageDTO>> getAllUnreadedMessagesFrom24Hours(ChatUser user){
		List<ChatUserLastRoomDate> userRooms = chatLastRoomDateService.getUserLastRoomDates(user);
		Map<Room, List<UserMessageDTO>> result = new  HashMap<Room, List<UserMessageDTO>>();
		for (ChatUserLastRoomDate lastRoomEntry : userRooms){
			Room room = lastRoomEntry.getRoom();
			Date lastLogoutDate = lastRoomEntry.getLastLogout();
			Date currentDateMinus24Hours = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
			if (lastLogoutDate.before(currentDateMinus24Hours))
			{
				lastLogoutDate = currentDateMinus24Hours;
			}
			List<UserMessage> unreadedMessages = getMessagesByRoomDate(room, lastLogoutDate, "ua");
			List<UserMessageDTO> unreadedChatMessages = dtoMapper.mapListUserMessage(unreadedMessages);
			if(unreadedChatMessages.size()>0)
				result.put(room, unreadedChatMessages);
		}
		return result;
	}
	
	@Transactional
	public List<Date> getMessagesDatesByChatUserAndDate(Long chatUserId,Date early, Date late){
		return userMessageRepository.getMessagesDatesByChatUserAndDateBetween(chatUserId, early, late);
	}

	private String generateMessageSearchCondition(UserMessageSearchCriteria criteria) {
		boolean haveAnyParameterDefined = criteria.haveAnyParameterDefined();
		String hqlRequestCore = "";
		if (haveAnyParameterDefined) {
			hqlRequestCore += " WHERE ";
		}
		List<String> hqlRequestParts = new LinkedList<>();
		//"m.author.id=:authorId and m.room.id=:roomId and Date(m.date) >= Date(:earlyDate) and Date(m.date) <= Date(:lateDate)")
		Optional.ofNullable(criteria.getAuthorId()).ifPresent(userParam -> hqlRequestParts.add("m.author.id=:authorId"));
		Optional.ofNullable(criteria.getRoomId()).ifPresent(roomParam -> hqlRequestParts.add("m.room.id=:roomId"));
		Optional.ofNullable(criteria.getSearchValue()).ifPresent(searchParam -> hqlRequestParts.add(" lower(m.body) like :searchValue "));
		Optional.ofNullable(criteria.getEarlyDate()).ifPresent(earlyDateParam -> hqlRequestParts.add(" m.date > :earlyDate "));
		Optional.ofNullable(criteria.getLateDate()).ifPresent(lateDateParam -> hqlRequestParts.add(" m.date < :lateDate "));

		String whereRequestPart = hqlRequestCore + String.join(" AND ",hqlRequestParts);
		return whereRequestPart;
	}
	private void setMessageSearchQueryParameters(Query query,UserMessageSearchCriteria criteria ){
		Optional.ofNullable(criteria.getAuthorId()).ifPresent(authorId->
				query.setParameter("authorId",authorId));

		Optional.ofNullable(criteria.getRoomId()).ifPresent(roomId->
			query.setParameter("roomId",roomId));

		Optional.ofNullable(criteria.getSearchValue()).ifPresent(searchValue ->
				query.setParameter("searchValue","%"+searchValue+"%"));

		Optional.ofNullable(criteria.getEarlyDate()).ifPresent(
				date -> query.setParameter("earlyDate",date));

		Optional.ofNullable(criteria.getLateDate()).ifPresent(
				date -> query.setParameter("lateDate",date));

	}

	public List<Date> findMessagesDate(UserMessageSearchCriteria criteria) {
		//StringBuilder strBuilder = new StringBuilder();
		String request = "select m.date from chat_user_message m "+generateMessageSearchCondition(criteria);
		Query query = entityManager.createQuery(request);
		setMessageSearchQueryParameters(query,criteria);
		List<Date> result = query.getResultList();
		return result;
	}

	@Transactional
	public List<Date> getMessagesDatesByChatUserAndRoomAndDate(Long chatUserId,Long roomId,Date early, Date late){
		return userMessageRepository.getMessagesDatesByChatUserAndDateBetween(chatUserId, early, late);
	}

	public boolean isAuthor(Long messageId, Long chatUserId) {
		return userMessageRepository.countByIdAndAuthorId(messageId,chatUserId) > 0;
	}

	public UserMessageDTO updateMessage(UserMessageDTO messageDTO,ChatUser user) throws OperationNotAllowedException {
		UserMessage message = userMessageRepository.findOne(messageDTO.getId());
		if(!message.getAuthor().equals(user)) {
			throw new OperationNotAllowedException("");
		}
		message.setBody(messageDTO.getBody());
		message.setAttachedFiles(messageDTO.getAttachedFiles());
		UserMessage messageResult = userMessageRepository.save(message);
		UserMessageDTO dtoResult = dtoMapper.map(messageResult);
		return dtoResult;
	}

	public boolean isMessageBookmarked(Long messageId, Long chatUserId) {
		return userMessageRepository.isBookMarkedMessage(messageId,chatUserId).testBit(0);
	}

	@Transactional
	public boolean toggleBookMarkMessage(Long messageId, Long chatUserId) {
		boolean isBookMarked = isMessageBookmarked(messageId,chatUserId);
		if (isBookMarked){
			userMessageRepository.removeBookMarkMessage(messageId,chatUserId);
			return false;
		}
		Long roomId = userMessageRepository.findOne(messageId).getRoom().getId();
		userMessageRepository.addBookMarkMessage(messageId,chatUserId,roomId);
		return true;
	}

	
	
}
