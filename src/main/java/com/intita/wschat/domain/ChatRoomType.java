package com.intita.wschat.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roma on 18.04.17.
 */
public enum ChatRoomType {
    DEFAULT(0), PRIVATE(1),CONSULTATION(2), STUDENTS_GROUP(4), ROLES_GROUP(8);
    private int value;
    private ChatRoomType(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
    private static Map<Integer, ChatRoomType> map = new HashMap<Integer, ChatRoomType>();

    static {
        for (ChatRoomType legEnum : ChatRoomType.values()) {
            map.put(legEnum.getValue(), legEnum);
        }
    }

    public static ChatRoomType valueOf(int legNo) {
        return map.get(legNo);
    }

}
