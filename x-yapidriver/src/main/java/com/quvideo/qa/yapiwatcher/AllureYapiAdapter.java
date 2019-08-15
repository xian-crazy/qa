package com.quvideo.qa.yapiwatcher;

import com.alibaba.fastjson.JSON;

import com.quvideo.qa.yapiwatcher.bean.ERB;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

public class AllureYapiAdapter {
    protected static Logger log  = LoggerFactory.getLogger(AllureYapiAdapter.class);

    public  El root=null;
    private  El style=null;
    public El script=null;
    private El gdoc=null;
    private El div0=null;
    private El h2=null;
    private El h3=null;


    public static AllureYapiAdapter newIt(){
        return new AllureYapiAdapter();
    }
    private AllureYapiAdapter documentInit(){
        //先添加一个根元素：root，并指定标签：languages
        root=newDiv(YDR1.yapi);
        style=new El("style").setTextContent(YDR1.style);
        script=new El("script").setTextContent(YDR1.JsonViewjs).setAttribute("type","text/javascript").setAttribute("language","javascript");


        gdoc=newDiv(YDR1.gdoc);
        div0=newDiv(null);
        h2=newEl("h2","id",YDR1.id0);
        h3=newEl("h3","id",YDR1.id0);
        return this;
    }



    public  String descriptionHtml(String description) {
        StringWriter writer=new StringWriter();
            //先添加一个根元素：root，并指定标签：languages
            documentInit();
            h3.setTextContent(description);
            appendChilds(root,style,gdoc);
            gdoc.appendChild(div0);
            appendChilds(div0,h3);
        return root.asString();
    }
    

    public  String descriptionERb(ERB erb, RuntimeException e) {
        try {
            //先添加一个根元素：root，并指定标签：languages
            documentInit();
            h2.setTextContent(erb.getName());
            if(null!=e&&(e instanceof  ValidationException)) {
                El exception = newEl("a", "onclick", "javascript:document.getElementById('schemaValidationInfo').scrollIntoView();");
                exception.setTextContent("schema校验详情");
                exception.setAttribute("style", "color:red");
                exception.setAttribute("href","javascript:void(0);");
                gdoc.appendChild(exception);
            }

            El h3=newEl("h3",null,null);
            h3.setTextContent("基本信息");

            El base=newDiv(YDR1.rowcasereport);
            El baseinfo =newDiv(YDR1.col_21);
            baseinfo.setTextContent("空间："+erb.getProject().getSpace().getGroup_name()+"  项目："+erb.getProject().getName()+"  用例集合："+erb.getCaseColName());
            base.appendChild(baseinfo);
            El casename=newNode("用例名",erb.getName());
            El env=newNode("执行环境",erb.getEnv());
            El Method=newNode("Method",erb.getMethod());
            El Path=newNode("Path",erb.getPath());

            El divreq=newDiv(null);
            El h3r=newEl("h3",null,null);
            h3r.setTextContent("Request");
            El url=newNode("Url",erb.getUrl());
            El qHeaders=newJsonNode("Req_Header",JSON.toJSONString(erb.getHeaders()));
            El qBody=newJsonNode("Req_Body",erb.getData());
            El qParams=newJsonNode("Req_Params",erb.getParams());
          //  divreq.appendChild(h3r).appendChild(url).appendChild(qHeaders).appendChild(qBody).appendChild(qParams);
            appendChilds(divreq,h3r,url,qHeaders,qBody,qParams);

            El divrep=newDiv(null);
            El h3p=newEl("h3",null,null);
            h3p.setTextContent("Reponse");
            El pHeaders=newJsonNode("Rep_Header",erb.getRes_header());
            El pBody=newJsonNode("Rep_Body",erb.getRes_body());
            appendChilds(divrep,h3p,pHeaders,pBody);


            appendChilds(root,script,style,gdoc);

            gdoc.appendChild(div0);
            appendChilds(div0,h2,h3,base,casename,env,Method,Path,divreq,divrep);

            if(null!=e) {

                El divv = newDiv(null);
                El h3v = newEl("h3", "id", "schemaValidationInfo");
                h3v.setTextContent("schema校验信息");
                if(e instanceof ValidationException) {
                    ValidationException v=(ValidationException)e;
                    El Validationinfo = newJsonNode("Validation_info", v.toJSON().toString());
                    El schema = newJsonNode("schema", v.getViolatedSchema().toString());
                    appendChilds(divv, h3v, Validationinfo, schema);

                }else if(e instanceof JSONException){
                    JSONException j=(JSONException)e;
                    El mesg = newNode("校验异常信息", j.getMessage());
                    El schema = newNode("提示信息", "请检查接口定义，响应是否开启了json-schema");
                    //divv.appendChild(h3v).appendChild(Validationinfo).appendChild(schema);
                    appendChilds(divv, h3v, mesg, schema);
                }
                div0.appendChild(divv);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    return root.asString();
    }
    

    

    private void appendChilds(El tag,El...els){
        for(El el:els){
            tag.appendChild(el);
        }
    }


    private  El newJsonNode(String name,String json){
        String id="yapi"+name;
        El node=newDiv(YDR1.rowcasereport);
        El nodeName =newDiv(YDR1.col_3);
        nodeName.setTextContent(name);
       // nodeName.setAttribute("name",name);
        node.appendChild(nodeName);
        El nodeContent =newDiv(YDR1.col_21);
        El code =newEl("code",null,null);
        El pre =newEl("pre","id",id);
        code.appendChild(pre);
        nodeContent.appendChild(code);
        El script =newEl("script",null,null);
        script.setTextContent("document.getElementById(\""+id+"\").innerHTML=forJson("+json+")");
        nodeContent.appendChild(script);
        node.appendChild(nodeContent);
        return node;
    }

    private  El newNode(String name,String content){
        El node=newDiv(YDR1.rowcasereport);
        El nodeName =newDiv(YDR1.col_3);
        nodeName.setTextContent(name);
//        nodeName.setAttribute("name",name);
        El nodeContent =newDiv(YDR1.col_21);
        nodeContent.setTextContent(content);
        node.appendChild(nodeName);
        node.appendChild(nodeContent);
        return node;
    }

    private  El newEl(String name,String attrkey,String attrvalue){
        El el=new El(name);
        if(StringUtils.isNotEmpty(attrvalue)) {
            el.setAttribute(attrkey, attrvalue);
        }
        return el;
    }
    private  El newEl(String name,String clazz){
        return newEl(name,"class",clazz);
    }
    private  El newDiv(String clazz){
        return newEl("div",clazz);
    }


    @Test
    public void tt(){

        AllureYapiAdapter al=AllureYapiAdapter.newIt().documentInit();


        al.root.appendChild(al.script);
        log.info("*****************************");
        log.info(al.root.asString());
     //  log.info(docToString(al.document));
    }

}

class El{
   private String name=null;
   private Map<String,String> attrs=new HashMap<>();
   private String content="";
   private List<El> subEls=new ArrayList<>();
    private String attrs2str(){
        String str="";
        for(String att:attrs.keySet()){
            str += " "+att+"=\""+attrs.get(att)+"\" ";
        }
        return str;
    }
    public El(String name){
        this.name=name;
    }

    public El setAttribute(String attrName,String attrValue){
       this.attrs.put(attrName,attrValue);
        return this;
    }
    public El setTextContent(String content){
        this.content=content;
        return this;
    }

    public El appendChild(El el){
       // this.content+=el.asString();
        subEls.add(el);
        return this;
    }
    private String contents(){
        String str=content;
        for(El el:subEls){
            str+=el.asString();
        }
        return str;
    }
    public String asString(){
        return "<"+this.name+attrs2str()+">"+contents()+"</"+name+">";
    }

}

/**
 * yapiDesReport
 */
class YDR1{
    public static final String yapi="yapi-run-auto-test";
    public static final String gdoc="g-doc";
    public static final String id0="0";
    public static final String rowcasereport="row case-report";
    public static final String col_21="col-21";
    public static final String col_3="col-3 case-report-title";

   public static final String JsonViewjs=input2Str(AllureYapiAdapter.class.getResourceAsStream("/JsonView/JsonView.js"));

   private static String input2Str(InputStream input){
       String text = "" ;
       try {

           Scanner scan = new Scanner(input);
           scan.useDelimiter("\r"); 				// 设置分隔符
           while(scan.hasNext()){
               text += scan.next();
           }
          // System.out.println(text);
       } catch (Exception e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       return text;
   }

    public static final String style="" +
            ".ObjectBrace {" +
            "  color: #00AA00;" +
            "  font-weight: bold;" +
            "}" +
            ".ArrayBrace {" +
            "  color: #0033FF;" +
            "  font-weight: bold;" +
            "}" +
            ".PropertyName {" +
            "  color: #CC0000;" +
            "  font-weight: bold;" +
            "}" +
            ".String {" +
            "  color: #007777;" +
            "}" +
            ".Number {" +
            "  color: #AA00AA;" +
            "}" +
            ".Boolean {" +
            "  color: #0000FF;" +
            "}" +
            ".Function {" +
            "  color: #AA6633;" +
            "  text-decoration: italic;" +
            "}" +
            ".Null {" +
            "  color: #0000FF;" +
            "}" +
            ".Comma {" +
            "  color: #000000;" +
            "  font-weight: bold;" +
            "}"+
            "@charset \"UTF-8\";" +
            "h2," +
            "h3," +
            "blockquote {" +
            "margin: 0;" +
            "padding: 0;" +
            "font-weight: normal;" +
            " -webkit-font-smoothing: antialiased;" +
            "}" +
            "" +
            ".yapi-run-auto-test {" +
            "font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Helvetica, \"PingFang SC\"," +
            "\"Hiragino Sans GB\", \"Microsoft YaHei\", SimSun, sans-serif;" +
            "font-size: 13px;" +
            "color: #393838;" +
            "position: relative;" +
            "}" +
            "" +
            ".yapi-run-auto-test" +
            "h2," +
            "h3{" +
            "color: #97cc64;" +
            "line-height: 36px;" +
            "}" +
            "" +
            ".yapi-run-auto-test h2 {" +
            "font-size: 28px;" +
            "padding-top: 10px;" +
            "padding-bottom: 10px;" +
            "}" +
            "" +
            ".yapi-run-auto-test h3 {" +
            "clear: both;" +
            "font-weight: 400;" +
            "" +
            "border-left: 3px solid #59d69d;" +
            "padding-left: 8px;" +
            "font-size: 18px;" +
            "}" +
            "" +
            ".yapi-run-auto-test code," +
            "pre {" +
            "font-family: Monaco, Andale Mono, Courier New, monospace;" +
            "}" +
            "" +
            ".yapi-run-auto-test code {" +
            "background-color: #fee9cc;" +
            "color: rgba(0, 0, 0, 0.75);" +
            "padding: 1px 3px;" +
            "font-size: 12px;" +
            " -webkit-border-radius: 3px;" +
            " -moz-border-radius: 3px;" +
            "border-radius: 3px;" +
            "}" +
            "" +
            ".yapi-run-auto-test pre {" +
            "display: block;" +
            "padding: 14px;" +
            "margin: 0 0 18px;" +
            "line-height: 16px;" +
            "font-size: 11px;" +
            "border: 1px solid #d9d9d9;" +
            "white-space: pre-wrap;" +
            "background: #f6f6f6;" +
            "overflow-x: auto;" +
            "}" +
            "" +
            ".yapi-run-auto-test pre code {" +
            "background-color: #f6f6f6;" +
            "color: #737373;" +
            "font-size: 14px;" +
            "padding: 0;" +
            "}" +
            "" +
            ".yapi-run-auto-test .case-report {" +
            "margin: 5px;" +
            "display: flex;" +
            "}" +
            "" +
            ".yapi-run-auto-test .case-report .case-report-title {" +
            "font-size: 14px;" +
            "text-align: right;" +
            "padding-right: 20px;" +
            "}" +
            "" +
            ".yapi-run-auto-test .col-3 {" +
            "display: block;" +
            "box-sizing: border-box;" +
            "color: #24a023;" +
            "width: 8%;" +
            "}" +
            "" +
            ".yapi-run-auto-test .col-21 {" +
            "display: block;" +
            "box-sizing: border-box;" +
            "width: 87.5%;" +
            "}"+
            ".link {" +
            " color: blue;" +
             "}";
}


