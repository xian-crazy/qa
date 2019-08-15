package com.quvideo.qa.yapiwatcher.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBean {
   private String email;
    private String role;
    private int uid;
    private String username;
    private String _id;
    public UserBean thiz(){
        return  this;
    }
}
