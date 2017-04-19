package com.intita.wschat.domain;

/**
 * Created by roma on 18.04.17.
 */
public enum ChatRoomType {
    DEFAULT(0), PRIVATE(1),CONSULTATION(4), STUDENTS_GROUP(4), ROLES_GROUP(8);
    private int value;
    private ChatRoomType(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }

}
