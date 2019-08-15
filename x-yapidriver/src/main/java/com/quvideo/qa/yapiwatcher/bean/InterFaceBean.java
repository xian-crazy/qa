package com.quvideo.qa.yapiwatcher.bean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quvideo.qa.yapiwatcher.Yapi;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * yapi 接口info bean
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterFaceBean {
   // private String project_name;
    private String cat_name;
    private long add_time;
    private long up_time;
    private boolean api_opened;
    private int catid;
    private int edit_uid;
    private String method;
    private String path;
    private int project_id;
    private String status;
    private List<String> tag;
    private String title;
    private int uid;
    private int _id;
    private int index;
    private boolean res_body_is_json_schema;
    private String resBody;
  //  private String group_name;
   // private int group_id;
    private ProjectBean project;
    private List<Integer> caseIds;
    private String author;

    private List<HashMap> reqQueryArgs;
    private List<HashMap> reqHeaders;
    private List<HashMap> reqBodyArgs;

    public String getAuthor(){
        return project.getSpace().getUserBeanMap().get(uid).getUsername();
    }

    public boolean isNotJsonResTag(){
        boolean isnj=false;
        for(String tg:tag){
            if(tg.equals(Yapi.RESPONSE_NOT_JSON)){
                isnj=true;
                break;
            }
        }
        return isnj;
    }

    public boolean isNotNeedCase(){
        boolean isnj=false;
        for(String tg:tag){
            if(tg.equals(Yapi.NOT_NEED_CASE)){
                isnj=true;
                break;
            }
        }
        return isnj;
    }

    public boolean notCheckRequired(){
        boolean isnj=false;
        for(String tg:tag){
            if(tg.equals(Yapi.RESPONSE_NOT_REQUIRED)){
                isnj=true;
                break;
            }
        }
        return isnj;
    }

    public String toString(){
        return "空间："+(null!=project?project.getSpace().getGroup_name():"")+"   项目："+(null!=project?project.getName():"")+"   接口分类："+ cat_name+  " 接口名："+this.getTitle();
    }
}
