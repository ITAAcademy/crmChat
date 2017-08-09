package com.intita.wschat.dto.mapper;

import com.intita.wschat.dto.model.UserMessageDTO;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.dto.model.IntitaUserDTO;
import com.intita.wschat.dto.model.UserMessageWithLikesDTO;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatLikeStatusService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by roma on 18.04.17.
 */
@Component
public class DTOMapper {

    @Autowired private ChatLikeStatusService chatLikeStatusService;

    ModelMapper modelMapper = new ModelMapper();
    PropertyMap<ChatUser, ChatUserDTO> chatUserMap = new PropertyMap<ChatUser, ChatUserDTO>() {
        protected void configure() {
            map().setAvatar(source.getIntitaUser().getAvatar());
            map().setIntitaUserId(source.getIntitaUser().getId());
        }
    };

    DTOMapper(){
        modelMapper.addMappings(chatUserMap);
    }

    public IntitaUserDTO map(User intitaUser) {
        return modelMapper.map(intitaUser, IntitaUserDTO.class);
    }
    public ChatUserDTO map(ChatUser chatUser) {
        return modelMapper.map(chatUser, ChatUserDTO.class);
    }
    public Collection<ChatUserDTO> map(Collection<ChatUser> chatUsers) {
    	java.lang.reflect.Type targetListType = new TypeToken<Collection<ChatUserDTO>>() {}.getType();
        return modelMapper.map(chatUsers, targetListType);
    }
    public UserMessageDTO map(UserMessage userMessage) {
        return modelMapper.map(userMessage, UserMessageDTO.class);
    }
    public UserMessageWithLikesDTO mapMessageWithLikes(UserMessage userMessage) {
        UserMessageWithLikesDTO model = modelMapper.map(userMessage, UserMessageWithLikesDTO.class);
        model.setLikes(chatLikeStatusService.getMessageLikesCount(userMessage.getId()));
        model.setDislikes(chatLikeStatusService.getMessageDislikesCount(userMessage.getId()));
        return model;
    }
    public UserMessageWithLikesDTO map(UserMessageDTO messageDTO) {
        UserMessageWithLikesDTO model = modelMapper.map(messageDTO, UserMessageWithLikesDTO.class);
        model.setLikes(0L);
        model.setDislikes(0L);
        return model;
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
