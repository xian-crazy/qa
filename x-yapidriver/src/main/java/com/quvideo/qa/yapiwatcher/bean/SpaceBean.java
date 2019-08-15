package com.quvideo.qa.yapiwatcher.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpaceBean {
    private long add_time;
    private Map custom_field1;
    private String group_desc;
    private String group_name;
    private String role;
    private String type;
    private int uid;
    private long up_time;
    private int _id;
    private Map<Integer,UserBean> userBeanMap;

    public String toString(){
        return "空间名："+group_name+" 监控账户autotest权限："+role;
    }
}
