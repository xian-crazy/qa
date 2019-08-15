package com.quvideo.qa.common.quvideoallure;

import io.qameta.allure.Allure;
import org.apache.commons.lang3.StringUtils;

public class QA {
    public static void Owner(String name){
        Allure.label("owner",name+":  ");
    }
    public static void Des(String d){
        Allure.getLifecycle().updateTestCase((executable) -> {
            String des=executable.getDescription();
            String deshtml=executable.getDescriptionHtml();
            if(StringUtils.isBlank(deshtml)){
                deshtml="<ol><li>%des%</li></ol>";
            }else{
                deshtml=  deshtml.replace("</ol>","<li>%des%</li></ol>");
            }
            deshtml=    deshtml.replace("%des%",d);

            deshtml=deshtml.contains("<div style=\"color:green\">")?deshtml:"<div style=\"color:green\"><h4>"+des+"</h4>"+deshtml+"</div>";
            executable.setDescriptionHtml(deshtml);
        });
    }

}
