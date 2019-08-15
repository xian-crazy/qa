package com.quvideo.qa.yapiwatcher.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * yapi 项目环境配置bean
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvBean {
    private List<Map<String,String>> header;
    private List<Map<String,String>> global;
    private String _id;
    private String name;
    private String domain;

}
