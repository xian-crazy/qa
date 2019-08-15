package com.quvideo.qa.yapiwatcher.bean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * yapi 项目信息bean
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectBean {
    private static Logger LOG = LoggerFactory.getLogger(ProjectBean.class);
    private boolean switch_notice;//邮件通知
    private boolean is_mock_open;//全局mock
    private boolean strice;//mock严格模式
    private boolean is_json5;//jsonschema
    private int _id;
    private String name;
    private String basepath;
    private String project_type;//私有。公开
    private int uid;
    private int group_id;//所属空间id
   // private String group_Name;//所属空间名称
    private SpaceBean space;

    private String icon;
    private String color;
    private long add_time;
    private long up_time;
    private List<EnvBean> env;//环境配置
    private String after_script;
    private List<Map<String,String>> cat;
    private String desc;
    private String pre_script;
    private String role;
    private List<Map<String,String>> tag;
    private boolean follow;
    private String token;
    public boolean isSwitch_notice() {
        return switch_notice;
    }

    public void setSwitch_notice(boolean switch_notice) {
        this.switch_notice = switch_notice;
    }

    public boolean isIs_mock_open() {
        return is_mock_open;
    }

    public void setIs_mock_open(boolean is_mock_open) {
        this.is_mock_open = is_mock_open;
    }

    public boolean isStrice() {
        LOG.info("isStrice? "+strice);
        return strice;
    }

    public void setStrice(boolean strice) {
        this.strice = strice;
    }

    public boolean isIs_json5() {
        LOG.info("isIs_json5? "+is_json5);
        return is_json5;
    }

    public void setIs_json5(boolean is_json5) {
        this.is_json5 = is_json5;
    }

    public String toString(){
        return "所属空间："+(null==this.space?"未定义空间":space.getGroup_name())+"   项目名:"+this.name+"   项目id："+_id;
    }


}


