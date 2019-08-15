package com.quvideo.qa.yapiwatcher.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * yapi 接口分类bean
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatMenuBean {
    private int index;
    private int _id;
    private String name;
    private int project_id;
    private String desc;
    private int uid;
    private long add_time;
    private long up_time;
    private int __v;
    private int parent_id;
    private List<InterFaceBean> list;

}
