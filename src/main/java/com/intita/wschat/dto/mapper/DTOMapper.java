package com.intita.wschat.dto.mapper;

import com.intita.wschat.dto.model.*;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatLikeStatusService;
import com.intita.wschat.services.ChatUsersService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by roma on 18.04.17.
 */
@Component
public class DTOMapper {

    @Autowired private ChatLikeStatusService chatLikeStatusService;
    @Autowired private ChatUsersService chatUserService;

    ModelMapper modelMapper = new ModelMapper();
    PropertyMap<ChatUser, ChatUserDTO> chatUserMap = new PropertyMap<ChatUser, ChatUserDTO>() {
        protected void configure() {
            map().setAvatar(source.getIntitaUser().getAvatar());
            map().setIntitaUserId(source.getIntitaUser().getId());
        }
    };
    PropertyMap<UserMessage, UserMessageDTO> userMessageMap = new PropertyMap<UserMessage, UserMessageDTO>() {
        protected void configure() {
            map().setRoomId(source.getRoom().getId());
        }
    };

    DTOMapper(){
        modelMapper.addMappings(chatUserMap);
        modelMapper.addMappings(userMessageMap);
    }

    public IntitaUserDTO map(User intitaUser) {
        return modelMapper.map(intitaUser, IntitaUserDTO.class);
    }


    public ExtendedIntitaUserDTO mapExtended(User intitaUser) {
        ExtendedIntitaUserDTO dto = modelMapper.map(intitaUser, ExtendedIntitaUserDTO.class);
        if (intitaUser.getChatUser() != null) {
            dto.setChatUserId(intitaUser.getChatUser().getId());
        }
        return dto;
    }

    public ChatUserDTO map(ChatUser chatUser) {
        return modelMapper.map(chatUser, ChatUserDTO.class);
    }
    public Collection<ChatUserDTO> map(Collection<ChatUser> chatUsers) {
    	java.lang.reflect.Type targetListType = new TypeToken<Collection<ChatUserDTO>>() {}.getType();
        return modelMapper.map(chatUsers, targetListType);
    }
    public UserMessageDTO map(UserMessage userMessage) {
        UserMessageDTO messageDTO =  modelMapper.map(userMessage, UserMessageDTO.class);
        if (!userMessage.isActive()) {
            messageDTO.setBody("");
        }
        return messageDTO;
    }


    public UserMessage map(UserMessageDTO messageDTO) {
        UserMessage model = modelMapper.map(messageDTO, UserMessage.class);
        return model;
    }

    public UserMessageWithLikesDTO mapMessageWithLikes(UserMessage userMessage) {
        UserMessageWithLikesDTO model = modelMapper.map(userMessage, UserMessageWithLikesDTO.class);
        model.setLikes(chatLikeStatusService.getMessageLikesCount(userMessage.getId()));
        model.setDislikes(chatLikeStatusService.getMessageDislikesCount(userMessage.getId()));
        if (!userMessage.isActive()) {
            model.setBody("");
        }
        return model;
    }
    public UserMessageWithLikesDTO mapMessageWithLikes(UserMessageDTO messageDTO) {
        UserMessageWithLikesDTO model = modelMapper.map(messageDTO, UserMessageWithLikesDTO.class);
        model.setLikes(0L);
        model.setDislikes(0L);
        return model;
    }


    public List<ChatRoomDTO> mapListRoom(List<Room> rooms) {
        List<ChatRoomDTO> resultDTOs = rooms.stream()
                .map(room -> map(room)).collect(Collectors.toList());
        return resultDTOs;
    }

    public ChatRoomDTO map (Room room) {
        return modelMapper.map(room,ChatRoomDTO.class);
    }

    public List<UserMessageDTO> mapListUserMessage(List<UserMessage> userMessages){
        List<UserMessageDTO> messagesDTO = userMessages.stream()
                .map(message -> map(message))
                .collect(Collectors.toList());
        return messagesDTO;
    }
    public List<UserMessageWithLikesDTO> mapListUserMessagesWithLikes(List<UserMessage> userMessages){
        List<UserMessageWithLikesDTO> messagesDTO = userMessages.stream()
                .map(message -> mapMessageWithLikes(message))
                .collect(Collectors.toList());
        return messagesDTO;
    }

}
