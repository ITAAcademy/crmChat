package com.intita.wschat.dto.mapper;

import com.intita.wschat.dto.model.UserMessageDTO;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.dto.model.IntitaUserDTO;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by roma on 18.04.17.
 */
@Component
public class DTOMapper {
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
    public UserMessageDTO map(UserMessage userMessage) {
        return modelMapper.map(userMessage, UserMessageDTO.class);
    }

    public List<UserMessageDTO> mapList(List<UserMessage> userMessages){
        List<UserMessageDTO> messagesDTO = userMessages.stream()
                .map(message -> map(message))
                .collect(Collectors.toList());
        return messagesDTO;
    }

}
