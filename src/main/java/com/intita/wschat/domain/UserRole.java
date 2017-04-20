package com.intita.wschat.domain;

import com.intita.wschat.models.RoomPermissions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roma on 18.04.17.
 */
public enum UserRole{
    ADMIN(1 << 0, "user_admin"),
    STUDENT(1 << 1, "user_student"),
    SUPER_VISOR(1 << 2, "user_super_visor"),
    TEACHER_CONSULTANT(1 << 3, "user_teacher_consultant"),
    ACCOUNTANT(1 << 4, "user_accountant"),
    CONSULTANT(1 << 5, "user_consultant"),
    TENANTS(1 << 6, "user_tenant"),
    TRAINER(1 << 7, "user_trainer");

    private int value;
    private String tableName;

    public static Map<String, Integer> getSupported(){
        Map<String, Integer> aMap = new HashMap<>();
        for (RoomPermissions.Permission permission : RoomPermissions.Permission.values()){
            aMap.put(permission.toString(),permission.getValue());
        }
        return aMap;
    }
    public  boolean checkNumberForThisPermission(Integer number){
        return (number & getValue()) == getValue();
    }
    private UserRole(int value, String tableName){
        this.value = value;
        this.tableName = tableName;
    }
    public int getValue(){
        return value;
    }
    public String getTableName() {
        return tableName;
    }

}
