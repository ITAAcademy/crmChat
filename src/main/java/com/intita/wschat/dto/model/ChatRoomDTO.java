package com.intita.wschat.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.intita.wschat.models.*;
import jsonview.Views;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by roma on 18.04.17.
 */
public class ChatRoomDTO {

    private Long id;

    List< ChatUserLastRoomDate> chatUserLastRoomDate;

    private boolean active = true;

    private String name;

    private short type;



}
