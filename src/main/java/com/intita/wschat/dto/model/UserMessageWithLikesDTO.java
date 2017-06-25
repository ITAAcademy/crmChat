package com.intita.wschat.dto.model;

import com.intita.wschat.dto.interfaces.DataTransferObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by roma on 22.06.17.
 */
public class UserMessageWithLikesDTO implements DataTransferObject {
    private ChatUserDTO author;

    public ChatUserDTO getAuthor() {
        return author;
    }

    public void setAuthor(ChatUserDTO author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(ArrayList<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String body;
    private Date date;
    private ArrayList<String> attachedFiles;
    private Long id;

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getDislikes() {
        return dislikes;
    }

    public void setDislikes(Long dislikes) {
        this.dislikes = dislikes;
    }

    private Long likes;
    private Long dislikes;

}
