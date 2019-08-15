package com.quvideo.qa.yapiwatcher.bean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * yapi 项目测试用例集合bean
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseColBean {
    private int index;
    private int _id;
    private String name;
    private int project_id;
    private String desc;
    private int uid;
    private long add_time;
    private int parent_id;
    private List<CaseBean> caseList;
}
