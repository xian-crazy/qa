package com.quvideo.qa.common.tools;


import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MybatisGenerator {
    //protected Logger log = LogManager.getLogger(this.getClass());
    protected  Logger log  = LoggerFactory.getLogger(this.getClass());

    @Test(description = "逆向生成数据查询代码，修改配置后运行当前用例")
    public void mybatisGenerator() {
        //配置需要逆向生成代码的相关信息，运行本用例
        String addr = "rm-uf69q361pp76n4eew.mysql.rds.aliyuncs.com:3306";//链接地址/数据库名
        String dbname="xiaoyingtvcenter";
        String username = "xiaoyingtv";
        String password = "Quvideo2012";
        String tables = "video_todo_code";//有多个表需要生成时，用逗号分隔符链接如：“qatest1,qatest2,xxxx”

//        String addr = "rm-t4ndan1lx329u56m8.mysql.singapore.rds.aliyuncs.com:3306";//链接地址/数据库名
//        String dbname="vivashow_additional";
//        String username = "qa_xiaoyingtv";
//        String password = "Quvideo2012";
//        String tables = "channel_config";//有多个表需要生成时，用逗号分隔符链接如：“qatest1,qatest2,xxxx”

        this.mybatisGeneratorActiong(addr,dbname,username,password,tables);
        log.info("mybatisGeneratorActiong ok!**********************");
    }

    private void mybatisGeneratorActiong(String addr,String dbname,String username,String password,String tables) {
        List<String> tablesList= Arrays.asList(StringUtils.split(tables,","));
        List<String> warnings = new ArrayList<String>();
        Exception exception=null;
        boolean overwrite = true;

        Configuration config = new Configuration();
        Context context = new Context(ModelType.FLAT);
        context.setId("sqlserverTables");
        context.setTargetRuntime("MyBatis3");

        //PluginConfiguration
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
        context.addPluginConfiguration(pluginConfiguration);

        //CommentGeneratorConfiguration
        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
        commentGeneratorConfiguration.addProperty("suppressAllComments", "true");
        context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

        //JDBCConnectionConfiguration
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL("jdbc:mysql://"+addr+"/"+dbname+"?serverTimezone=UTC");
        jdbcConnectionConfiguration.setDriverClass("com.mysql.cj.jdbc.Driver");
        jdbcConnectionConfiguration.setUserId(username);
        jdbcConnectionConfiguration.setPassword(password);
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

       //javaTypeResolver
        JavaTypeResolverConfiguration javaTypeResolverConfiguration=new JavaTypeResolverConfiguration();
        javaTypeResolverConfiguration.addProperty("forceBigDecimals","false");
        context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

        //javaModelGenerator
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration= new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage("com.quvideo.servertest.database.pojo."+dbname);
        javaModelGeneratorConfiguration.setTargetProject("./src/main/java");
        javaModelGeneratorConfiguration.addProperty("enableSubPackages" ,"true");
        javaModelGeneratorConfiguration.addProperty("trimStrings","true");
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        // sqlMapGenerator
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration=new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetPackage("mappers."+dbname);
        sqlMapGeneratorConfiguration.setTargetProject("./src/main/resources");
        sqlMapGeneratorConfiguration.addProperty("enableSubPackages" ,"true");
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);


        //javaClientGenerator xmlmapper 对应的Mapper接口类文件
        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration=new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        javaClientGeneratorConfiguration.setTargetPackage("com.quvideo.servertest.database.dao."+dbname);
        javaClientGeneratorConfiguration.setTargetProject("./src/main/java");
        javaClientGeneratorConfiguration.addProperty("enableSubPackages" ,"true");
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);


        //设置需要逆向生成代码的表
        Context tabelContest=new Context(ModelType.FLAT);
        TableConfiguration tc=new TableConfiguration(tabelContest);
        tc.setCountByExampleStatementEnabled(false);
        tc.setUpdateByExampleStatementEnabled(false);
        tc.setDeleteByExampleStatementEnabled(false);
        tc.setSelectByExampleStatementEnabled(false);
        tc.addProperty("useActualColumnNames","false");
        for(String tablename:tablesList){
            tc.setTableName(tablename);
            context.addTableConfiguration(tc);
            log.info("addTableConfiguration tablename = "+ tablename);
        }

        config.addContext(context);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = null;
        try {
            myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            exception=e;
        }
        try {
            if(null!= myBatisGenerator) {
                myBatisGenerator.generate(null);
            }
            log.info("ok!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (SQLException e) {
            e.printStackTrace();
            exception=e;
        } catch (IOException e) {
            e.printStackTrace();
            exception=e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            exception=e;
        }
        Assert.assertNull(exception,"myBatisGenerator 异常");
    }
}
