package com.quvideo.qa.crazyyapidriver;

import com.quvideo.qa.common.quvideoallure.QA;
import com.quvideo.qa.common.tools.QuvAssert;
import com.quvideo.qa.yapiwatcher.AllureYapiAdapter;
import com.quvideo.qa.yapiwatcher.Yapi;
import com.quvideo.qa.yapiwatcher.bean.*;
import io.qameta.allure.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  YapiCaseDriver {
    protected Logger log  = LoggerFactory.getLogger(this.getClass());

    private  static  Yapi yapi= new Yapi();
    private static List<SpaceBean> spaceBeansList=null;
    private static Map<String, ProjectBean> allprojectBeans=null;//key 项目token，项目实例
    private static  Map<String,List<CaseColBean>> allcaseColBeans=null;//key 项目token value 测试用例集实例
    @Getter
    private static  Map<Integer,InterFaceBean> allInterFaceBeans=null;//所有的接口实例
   // private static  Map<Integer,InterFaceBean> caseIdvIFB=null;//key 用例id，value  接口实例
    private static    List<ERB> allexcutResBeans = null;


    @BeforeClass
    public void beforeClass(){
        if(null==spaceBeansList){
            spaceBeansList=yapi.intiSpaceList();
        }
        log.info("本次监控空间数量：" + spaceBeansList.size());
        allprojectBeans= watchSpaceProject();
        log.info("本次监控项目数量：" + allprojectBeans.size());
        allcaseColBeans=watchProjectCaseCols();
        allInterFaceBeans= watcherInterFases();
        allexcutResBeans=  watcherAllCaseRes();
    }

    @Step("本次监控空间下项目信息")
    private Map<String,ProjectBean> watchSpaceProject(){
        Map<String,ProjectBean> allprojectBeans=new HashMap<>();
        for(SpaceBean spaceBean:spaceBeansList){
            List<ProjectBean> projectBeans=yapi.getSpaceProject(spaceBean);
            for(ProjectBean projectBean:projectBeans){
                allprojectBeans.put(projectBean.getToken(),projectBean);
            }
        }
        return allprojectBeans;
    }

    @Step("获取项目下测试用例集信息")
    private  Map<String,List<CaseColBean>> watchProjectCaseCols(){
        Map<String,List<CaseColBean>> allcaseColBeans=new HashMap<>();
        for(ProjectBean projectBean: allprojectBeans.values()){
            List<CaseColBean> caseColBeanList= yapi.getCaseList(projectBean.get_id());
            allcaseColBeans.put(projectBean.getToken(),caseColBeanList);
        }
        return allcaseColBeans;
    }

    @DataProvider(name="projectList")
    public Object[][] getProjects(){
        int projectSize=allprojectBeans.size();

        Object[][] ret=new Object[projectSize][];
        int i=0;
            for (ProjectBean projectBean:allprojectBeans.values()) {
                ret[i] = new Object[]{projectBean};
                i++;
            }
        return  ret;
    }


    @Test(dataProvider = "projectList")
    @Severity(SeverityLevel.NORMAL)
    public void projectCheck(ProjectBean projectBean){
       Kids.epic(projectBean);
        Allure.feature("项目["+projectBean.getName()+"]配置规范性检查");
        Allure.story("项目设置规范性检查");
        Allure.link("项目设置链接","http://restapi.quvideo.com/project/"+projectBean.get_id()+"/setting");
        Allure.descriptionHtml(AllureYapiAdapter.newIt().descriptionHtml(projectBean.toString()));
        QuvAssert quvAssert=new QuvAssert();
        quvAssert.assertTrue(projectBean.isIs_json5(),"验证项目 ["+projectBean.getName()+"] json5是否开启");
        quvAssert.assertTrue(projectBean.isStrice(),"验证项目 ["+projectBean.getName()+"] mock严格模式是否开启");
        List<EnvBean> envBeanList =projectBean.getEnv();
        Map<String,EnvBean> envName=new HashMap<>();
        for(EnvBean envBean:envBeanList){
            envName.put(envBean.getName().trim().toLowerCase(),envBean);
        }
        if(envName.containsKey("mock")){
            quvAssert.assertEquals(envName.get("mock").getDomain(),"http://"+Yapi.yapiaddr+"/mock/"+projectBean.get_id(),"mock环境地址是否配置正确");//http://restapi.quvideo.com/mock/128"
        }else{
            quvAssert.assertTrue(envName.containsKey("mock"), "验证项目 [" + projectBean.getName() + "] 环境配置是否包含mock配置，以mock命名");
        }
        if(envName.containsKey("qa")){
            quvAssert.assertFalse(envName.get("qa").getDomain().isEmpty(),"qa环境地址是否为空");//http://restapi.quvideo.com/mock/128"
        }else{
            quvAssert.assertTrue(envName.containsKey("qa"), "验证项目 [" + projectBean.getName() + "] 环境配置是否包含qa配置，以qa命名");
        }
        quvAssert.assertAll();
    }



    @Step("采集项目接口信息")
    private Map<Integer,InterFaceBean> watcherInterFases(){
        Map<Integer,InterFaceBean> allInterFaceBeans=new HashMap<>();
        List<CatMenuBean> catMenuBeans=null;


        Map<Integer,List<Integer>> apivcases=new HashMap<>();//接口vs用例的map关系集合
        for(ProjectBean projectBean:allprojectBeans.values()) {
            List<CaseColBean> caseColBeanList = allcaseColBeans.get(projectBean.getToken());
            //将项目所有用例按所属接口分组
            for(CaseColBean caseColBean :caseColBeanList){
                List<CaseBean> caseBeans=caseColBean.getCaseList();
                try {
                    for (CaseBean caseBean : caseBeans) {
                        int inid = caseBean.getInterface_id();
                        if (null == apivcases.get(inid)) {
                            apivcases.put(inid, new ArrayList<>());
                        }
                        apivcases.get(inid).add(caseBean.get_id());
                    }
                }catch (Exception e){
                    log.error("项目id："+caseColBean.getProject_id()+" 下的测试集合 "+caseColBean.getName()+" 集合id："+caseColBean.get_id() +"下无测试用例");
                }
            }
        }
        for(ProjectBean projectBean:allprojectBeans.values()) {
            catMenuBeans= yapi.getCatAndApilist(projectBean);
            //根据接口测试用例分组情况，按接口设置
            for(CatMenuBean catMenuBean:catMenuBeans){
                List<InterFaceBean> interFaceBeans= catMenuBean.getList();
                for(InterFaceBean interFaceBean:interFaceBeans){
                    List<Integer> caseIds=apivcases.get(interFaceBean.get_id());
                    interFaceBean.setCaseIds(caseIds);
                    allInterFaceBeans.put(interFaceBean.get_id(),interFaceBean);
                }
                // allInterFaceBeans.addAll(interFaceBeans);
            }
        }
        return allInterFaceBeans;
    }

    @DataProvider(name="apiList")
    public Object[][] getApiList(){
        int size=allInterFaceBeans.size();
        Object[][] ret=new Object[size][];
        int i=0;
        for(InterFaceBean interFaceBean:allInterFaceBeans.values()) {
               ret[i] = new Object[]{interFaceBean};
                i++;
        }
        return  ret;
    }

    @Test(dataProvider = "apiList")
    @Severity(SeverityLevel.CRITICAL)
    public void apiCheck(InterFaceBean interFaceBean){
        Kids.epic(interFaceBean);
        Allure.feature("项目["+interFaceBean.getProject().getName()+"]配置规范性检查");
        Allure.story("API分类: "+interFaceBean.getCat_name()+" 规范性检查");
        Allure.descriptionHtml(AllureYapiAdapter.newIt().descriptionHtml("检查【"+interFaceBean.toString()+"】设置是否符合规范，规范内容会逐步完善"));
        Allure.link("接口设置链接","http://restapi.quvideo.com/project/"+interFaceBean.getProject_id()+"/interface/api/"+interFaceBean.get_id());
        QA.Owner(interFaceBean.getAuthor()+"--Yapi接口定义");
       // Allure.feature(interFaceBean.getCat_name()+"接口规范性检查");
        //Allure.story("API: "+interFaceBean.getTitle()+" 规范性检查");
        QuvAssert quvAssert=new QuvAssert(); Long addTime=interFaceBean.getAdd_time();
        Long now = System.currentTimeMillis() / 1000;//换算成秒
        Long timeOut=now-addTime;
        String status = interFaceBean.getStatus();
//        if(!"done".equals(interFaceBean.getStatus())){
//            if(!"stoping".equals(interFaceBean.getStatus())){
//                if (timeOut > 604800) {//7天(d) = 604800秒(s),如果大于7天，检查状态是否为已完成
//                    quvAssert.assertFalse(timeOut > 604800, "接口自创建到当前时间超过30天仍未发布,请确认是否暂停开发当前接口已超过" + (timeOut / 86400) + "天");//1天(d) = 86400秒(ms)
//                }
//            }
//        }else{
//            if (timeOut > 259200) {//3天，已完成接口超过三天，判断是否有用例覆盖
//            String caseids = null == interFaceBean.getCaseIds() ? null : StringUtils.join(interFaceBean.getCaseIds(), ",");
//            quvAssert.assertTrue(caseids != null, "已完成接口超过3天无至少一个用例覆盖");
//            }
//        }

        if ("undone".equals(status) || "testing".equals(status) || "design".equals(status)) {
            if (timeOut > 604800) {//1天(d) = 86400秒(s),  如果大于7天，检查状态是否为已完成
                quvAssert.assertFalse(timeOut > 86400 * 30, "接口自创建到当前时间超过30天仍未发布,当前接口已超过" + (timeOut / 86400) + "天，请确认是否暂停开发");//1天(d) = 86400秒(ms)
            }
        }

        if (("undone".equals(status)&& timeOut > 86400 * 3)|| "testing".equals(status) || "done".equals(status)) {
                String caseids = null == interFaceBean.getCaseIds() ? null : StringUtils.join(interFaceBean.getCaseIds(), ",");
            if(!interFaceBean.isNotNeedCase()) {//如果接口tag包含 not_need_case，则不执行当前断言
                quvAssert.assertTrue(caseids != null, "接口状态为 [" + c2s(status) + "] 无至少一个用例覆盖,请尽快补充冒烟用例");
            }
        }
        if(!interFaceBean.isNotJsonResTag()) {
            quvAssert.assertTrue(interFaceBean.isRes_body_is_json_schema(), "接口返回数据设置 jsonSchema是否打开?如果当前接口响应确认非json格式，请给该接口设置tag：" + Yapi.RESPONSE_NOT_JSON);
        }

        quvAssert.assertAll();

    }

    private String c2s(String code) {
        String str = null;
        switch (code) {
            case "undone":
                str = "开发中";
                break;
            case "testing":
                str = "已提测";
                break;
            case "design":
                str = "设计中";
                break;
            case "deprecated":
                str = "已过时";
                break;
            case "stoping":
                str = "暂停开发";
                break;
            case "done":
                str = "已发布";
        }
        return str;
    }


    @Step("获取项目测试用例集执行结果")
    private   List<ERB> watcherAllCaseRes(){
        List<ERB> allexcutResBeans = new ArrayList<>();
        List<Object[]> retLis=new ArrayList<>();
        for(String projectToken:allcaseColBeans.keySet())
            for(CaseColBean caseColBean:allcaseColBeans.get(projectToken)){
                if(caseColBean.getParent_id()==-1) {//crazy-yapi-服务器测试支持包含子集合用例
                    List<ERB> excutResBeansMock = yapi.excutCaseCols(allprojectBeans.get(projectToken), caseColBean, projectToken, allInterFaceBeans, "mock",true);
                    allexcutResBeans.addAll(excutResBeansMock);
                    List<ERB> excutResBeansQa = yapi.excutCaseCols(allprojectBeans.get(projectToken), caseColBean, projectToken, allInterFaceBeans, "qa",true);
                    allexcutResBeans.addAll(excutResBeansQa);
                }
            }
        return allexcutResBeans;
    }


    @DataProvider(name = "CaseColsList")
    public Object[][] getCaseColsList(){
        int caseSize=allexcutResBeans.size();
        Object[][] ret=new Object[caseSize][];
        int i=0;
        for (ERB excutResBean : allexcutResBeans) {
            ret[i] = new Object[]{excutResBean};
            i++;
        }
        return  ret;
    }

    @Test(dataProvider = "CaseColsList")
    @Description("项目接口测试用例集执行")
    @Severity(SeverityLevel.BLOCKER)
    public void caseResultCheck(ERB excutResBean) {
        Kids.epic(excutResBean);
        Allure.feature(excutResBean.getProject().getName() + ":测试用例集结果");
        Allure.story("Case: "+excutResBean.getCaseColName()+"_"+excutResBean.getId()+"_"+excutResBean.getName()+" 用例");
        Allure.descriptionHtml(AllureYapiAdapter.newIt().descriptionERb(excutResBean,null));
        Allure.link("接口设置链接", "yapi接口管理平台", "http://restapi.quvideo.com/project/" + excutResBean.getInterFaceBean().getProject_id() + "/interface/api/" + excutResBean.getInterFaceBean().get_id());
        Allure.link("测试用例链接", "yapi接口管理平台", "http://restapi.quvideo.com/project/" + excutResBean.getProject().get_id() + "/interface/case/" + excutResBean.getId());
        QA.Owner(excutResBean.getAuthor()+"--Yapi冒烟用例");

        String status=excutResBean.getInterFaceBean().getStatus();
        if("stoping".equals(status)||"design".equals(status)||"undone".equals(status)){
            throw new SkipException("当前用例所对应的接口状态为 ["+c2s(status)+"], 不监控冒烟用例执行结果");
        }
        QuvAssert quvAssert=new QuvAssert();
        List<Map<String,String>> vs=excutResBean.getValidRes();
        if(vs.size()==1) {
            quvAssert.assertEquals(excutResBean.getValidRes().get(0).get("message"), "验证通过", "用例执行是否通过");
        }else{
            for(Map<String,String> r:vs){
                quvAssert.assertNull(r.get("message"), "执行用例失败");
            }
        }
        if(!excutResBean.getInterFaceBean().isNotJsonResTag()) {
            String validateInfo = schema(excutResBean);
            boolean isok = validateInfo.startsWith("schema校验成功");
            if (isok) {
                quvAssert.assertTrue(isok, "校验schema");
            } else {
                quvAssert.assertEquals(validateInfo, "schema校验成功", "校验schema");
            }
        }
        quvAssert.assertAll();
    }

//    @Step("schema校验")
//    private void schemaValidate(String ){
//
//    }

    private String schema(ERB excutResBean){
        String validateInfo="schema校验成功！";
        //根据用例id获取接口响应bodyschema定义
        String schemaStr=excutResBean.getInterFaceBean().getResBody();
        //获取响应体
        String res_body=excutResBean.getRes_body();
        if(StringUtils.isEmpty(schemaStr)){
            validateInfo="错误：当前用例覆盖的接口schema为空";
            return validateInfo;
        }
        if(StringUtils.isEmpty(res_body)){
            validateInfo="错误：当前用例执行后响应体res_body为空";
            return validateInfo;
        }
        //进行schema校验
        JSONObject rawSchema=null;
        try {
             rawSchema = new JSONObject(new JSONTokener(schemaStr));
        }catch (JSONException e){
            return validateInfo="获取schema异常："+e.getMessage();
        }
            Schema schema = SchemaLoader.load(rawSchema);


            try {
                String type=rawSchema.getString("type");
                    if (type.equals("array")) {
                        schema.validate(new JSONArray(res_body));
                    } else if (type.equals("object")) {
                        schema.validate(new JSONObject(res_body));
                        if(!excutResBean.getInterFaceBean().notCheckRequired()) {
                            JSONArray requiredList = rawSchema.getJSONArray("required");
                        }
                    } else {
                        validateInfo = "错误：schema校验失败！schema校验暂未匹配，请联系测试同学修改";
                    }

            } catch (ValidationException  | JSONException e1) {
                validateInfo=e1.getMessage().contains("required")?"当前schema，root节点无至少一个必填字段！":e1.getMessage();
                validateInfo=(e1 instanceof ValidationException)?"错误：schema校验失败！详细信息见：schema校验详情":validateInfo;
                e1.printStackTrace();
               Allure.descriptionHtml(AllureYapiAdapter.newIt().descriptionERb(excutResBean,e1));
            }
        return validateInfo;
    }


}

class Kids{
    private  static String epicStr(String spaceName,String projectName,int projectId){
      //  return "空间名:" + spaceName + "-项目名:" + projectName + "-项目id_" + projectId;
        return "空间[" + spaceName + "] yapi规范性检查";
    }
    public static void epic(InterFaceBean interFaceBean){
        Allure.epic(epicStr(interFaceBean.getProject().getSpace().getGroup_name(),interFaceBean.getProject().getName(),interFaceBean.getProject_id()));
    }

    public static void epic(ERB excutResBean) {
        Allure.epic(epicStr(excutResBean.getProject().getSpace().getGroup_name(),excutResBean.getProject().getName(),excutResBean.getProject().get_id()));
    }
    public static void epic(ProjectBean  projectBean ){
        Allure.epic(epicStr(projectBean.getSpace().getGroup_name(),projectBean.getName(),projectBean.get_id()));
    }

}

