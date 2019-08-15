package com.quvideo.qa.common.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import org.apache.ibatis.io.Resources;

import static com.quvideo.qa.common.tools.Bt.LOG;


/**
 * 工具类Basic tools
 */
public class Bt {
    // protected static Logger LOG = LogManager.getLogger(Bt.class);
    protected static Logger LOG = LoggerFactory.getLogger(Bt.class);
    public static final String RNL = System.getProperty("line.separator");//系统换行符
    private static Map<String, Properties> proertiesMap = new HashMap<String, Properties>();
    private static Map<String, String> mapperToDbxml = new ConcurrentHashMap<String, String>();


    //生成随机数/字符串/日期工具库
    public static RandomTools ran = RandomTools.getInstance();

    public static ConcurrentHashMap<String, String> initData(String project) {
        ConcurrentHashMap<String, String> testData = new ConcurrentHashMap<>();
        switch (Bt.initEnv()) {
            case "pre":
            case "online":
                testData = properties2Map("/config/" + project + "/online.properties");
                break;
            // 按需添加，默认走qa
            case "online-us":
            case "online-sa":
            case "online-ph":
            default:
                testData = properties2Map("/config/" + project + "/qa.properties");
                break;
        }
        return testData;
    }

    public static ConcurrentHashMap<String, String> properties2Map(String file) {
        ConcurrentHashMap<String, String> testData = new ConcurrentHashMap<>();
        Properties prop = GetProv(file);
        for (final String name : prop.stringPropertyNames())
            testData.put(name, prop.getProperty(name));
        return testData;
    }

    public static String getConfigValue(String project, String key) {
        String configValue;
        switch (Bt.initEnv()) {
            case "pre":
            case "online":
                configValue = GetProv("/config/" + project + "/online.properties", key);
                break;
            // 按需添加，默认走qa
            case "online-us":
            case "online-sa":
            case "online-ph":
            default:
                configValue = GetProv("/config/" + project + "/qa.properties", key);
                break;
        }
        return configValue;
    }

    /**
     * <pre>
     *  e.g.     Bt.getProv("/config/application.properties", "default.env");
     * </pre>
     *
     * @param propertisFile 读取的配置文件路径
     * @param key           配置键
     * @return String值
     */
    public static String GetProv(String propertisFile, String key) {
        String value;
        value = GetProv(propertisFile).getProperty(key);
        LOG.info("获取文配置文件：" + propertisFile + "; " + key + " = " + value);
        return value;
    }

    /**
     * @param propertisFile Bt.getProv("/config/application.properties")
     * @return
     */
    public static Properties GetProv(String propertisFile) {
        synchronized (proertiesMap) {
            if (proertiesMap.get(propertisFile) == null) {
                Properties p = new Properties();
                InputStream is = Bt.class.getResourceAsStream(propertisFile);
                try {
                    p.load(new InputStreamReader(is, "UTF-8"));
                    is.close();
                    proertiesMap.put(propertisFile, p);

                } catch (IOException e) {
                    LOG.error("获取配置文件异常！", e);
                }
            }
        }
        return proertiesMap.get(propertisFile);
    }

    //通过xpath读取xml文件
    public static List<Node> xml(InputStream inputStream, String xpath) {
        SAXReader reader = new SAXReader();
        List<Node> elements = null;
        try {
            // 通过reader对象的read方法加载xml文件,获取docuemnt对象。
            Document dbxml = reader.read(inputStream);
            elements = dbxml.selectNodes(xpath);
            // System.out.println(xmlPath.getList("configuration.mappers.mapper", Node.class).get(0).getAttribute("resource"));
        } catch (Exception e) {
            LOG.error("xml exception:" + xpath, e);
        }
        return elements;
    }

    /** */
    /**
     * 遍历目录及其子目录下的所有文件并保存
     *
     * @param pathstr 目录全路径
     * @param myfile  列表：保存文件对象
     */
    public static void listDirectory(String pathstr, List<File> myfile) {
        File path = new File(pathstr);
        if (!path.exists()) {
            LOG.error("文件名称不存在!");
        } else {
            if (path.isFile()) {
                myfile.add(path);
                LOG.info("find file: " + path.getPath());
            } else {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    listDirectory(files[i].getPath(), myfile);
                }
            }
        }
    }

    public static String initEnv() {
        String env = null;
        env = System.getProperty("env");
        if (null == env) {
            env = Bt.GetProv("/config/application.properties", "default.env");
            System.setProperty("env", env);
            LOG.info("本次应用环境配置为：" + env);
        }
        return env.trim();
    }





    /**
     * 例子：getValueByEvn("qa",1,"online",2,"dev",3,"qax",4)
     *
     * @param evnkey    第一套环境key
     * @param value     第一套环境的value
     * @param keyvalues 成对定义
     * @return  runtime时，返回当前环境的value
     */
    public static Object getValueByEvn(String evnkey, Object value, Object... keyvalues) {
        Map<String, Object> kv = new HashMap<>();
        kv.put(evnkey, value);
        for (int i = 0; i < keyvalues.length; i++) {
            kv.put((String) keyvalues[i], keyvalues[++i]);
        }
        return kv.get(Bt.initEnv());
    }


    public static void initMappers(Object caseIns) {
        try {
            //for (Object caseIns : caseInsArr) {
            colectMappersInfoAndinitialFactory();
            //反射获取属性
            Field[] mapperFields = caseIns.getClass().getDeclaredFields();
            for (Field mapperField : mapperFields) {
                String dbxmlPath = mapperToDbxml.get(mapperField.getType().getName());
                if (null != dbxmlPath) {
                    mapperField.setAccessible(true);
                    //根据通过当前的dbxml和mapper类进行实例化
                    LOG.warn("实例化Mapper：" + caseIns.getClass().getSimpleName() + "--" + mapperField.getName());
                    mapperField.set(caseIns, MyBatisUtil.getMapper(dbxmlPath, mapperField.getType()));
                }
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T initMapper(Class<T> mapperClass) {
        try {
            colectMappersInfoAndinitialFactory();
            String dbxmlPath = mapperToDbxml.get(mapperClass.getName());
            if (null != dbxmlPath) {
                //根据通过当前的dbxml和mapper类进行实例化
                return MyBatisUtil.getMapper(dbxmlPath, mapperClass);
            } else {
                LOG.info("当前Mapper无匹配的数据库链接配置xml文件");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {

        }

    }


    public static Map<String, String> getPostManEnv(InputStream inputStream) {
        Map<String, String> envs = new HashMap<>();
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(inputStream, JSONObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("values");
        Iterator iterator = jsonArray.iterator();
        JSONObject jobj = null;
        while (iterator.hasNext()) {
            jobj = (JSONObject) iterator.next();
            envs.put(jobj.getString("key"), jobj.getString("value"));
        }
        return envs;
    }

    public static Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 把字符串转为url
     *
     * @param urlstr   url字符串
     * @return   URL对象
     */
    public static URL url(String urlstr) {
        URL url = null;
        try {
            url = new URL(urlstr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }




    /**
     * 扫描并数据库配置
     */
    private static void colectMappersInfoAndinitialFactory() {
        synchronized (Bt.mapperToDbxml) {
            if (!mapperToDbxml.isEmpty()) {
                return;
            }
        }
        // Map<String, String> mapperToDbxml = new HashMap<String, String>();
        long start = System.currentTimeMillis();

        try {


            List<File> files = new ArrayList<File>();
            String databasePath = null;

            //获取数据源/database目录绝对路径
            databasePath = Resources.getResourceAsFile("database").getPath();

            //获取database目录下所有xml文件的绝对路径
            Bt.listDirectory(databasePath, files);

            //多线程处理mapperinfo
            CountDownLatch latch = new CountDownLatch(files.size());

            ExecutorService executors = Executors.newFixedThreadPool(files.size());
            List<Future<String>> resultList = new ArrayList<>();
            //遍历database目录下的多个数据源xml配置
            for (File dbxml : files) {
                executors.submit(new ColectMappersInfoTask(dbxml, databasePath, Bt.mapperToDbxml, latch));
                //  Future<String> future =  executors.submit(new ColectMappersInfoTask(dbxml,databasePath,mapperToDbxml));
                //  resultList.add(future);
            }
            // LOG.warn("await colectMappersInfo");
            latch.await();
            latch.await(3, TimeUnit.MINUTES);
            Set<String> dbxmlset = new HashSet<>();
            for (String dbxml : Bt.mapperToDbxml.values()) {
                dbxmlset.add(dbxml);
            }
            //多线程初始化数据库工厂
            CountDownLatch latch2 = new CountDownLatch(dbxmlset.size());
            ExecutorService executors2 = Executors.newFixedThreadPool(dbxmlset.size());

            for (String dbxml : dbxmlset) {
                executors2.submit(new InitialFactory(dbxml, latch2));
            }
            latch2.await(3, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.warn("colect MappersInfo and init Factory time:" + (System.currentTimeMillis() - start));


    }

}

class InitialFactory implements Runnable {
    CountDownLatch latch = null;
    String dbxml = null;

    public InitialFactory(String dbxml, CountDownLatch latch) {
        this.dbxml = dbxml;
        this.latch = latch;
    }

    @Override
    public void run() {
        MyBatisUtil.initialFactory(dbxml);
        latch.countDown();
    }
}

class ColectMappersInfoTask implements Runnable {

    File dbxml = null;
    String databasePath = null;
    Map mapperToDbxml = null;
    CountDownLatch latch = null;

    public ColectMappersInfoTask(File dbxml, String databasePath, Map<String, String> mapperToDbxml, CountDownLatch latch) {
        this.dbxml = dbxml;
        this.databasePath = databasePath;
        this.mapperToDbxml = mapperToDbxml;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            //转换dbxml相对路径
            String dbxmlPath = "database" + dbxml.getPath().replace(databasePath, "");

            //获取xml文件中的mapper配置，一个dbxml可能存在多个表，即多个mapper配置
            List<Node> mapperxmls = Bt.xml(Resources.getResourceAsStream(dbxmlPath), "/configuration/mappers/mapper/@resource");

            Iterator it = mapperxmls.iterator();
            CountDownLatch sublatch = new CountDownLatch(mapperxmls.size());
            //多线程处理mapperClassinfo
            ExecutorService executors = Executors.newFixedThreadPool(mapperxmls.size());
            // List<Future<String>> resultList = new ArrayList<>();
            while (it.hasNext()) {
                String mapperxml = ((Attribute) it.next()).getValue();
                //获取mapperxml中的mapper类名，一个mapperxml中对应一个mapper类
                executors.submit(new ColectMappersClassInfoTask(mapperxml, dbxmlPath, mapperToDbxml, sublatch));

//        Future<String> future = executors.submit(new ColectMappersClassInfoTask(mapperxml, dbxmlPath, mapperToDbxml));
//        resultList.add(future);
            }

            // sublatch.await();
            sublatch.await(3, TimeUnit.MINUTES);
            // LOG.warn("ColectMappersInfoTask end");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
}

class ColectMappersClassInfoTask implements Runnable {

    String mapperxml = null;
    String dbxmlPath = null;
    Map<String, String> mapperToDbxml = null;
    CountDownLatch sublatch = null;

    public ColectMappersClassInfoTask(String mapperxml, String dbxmlPath, Map<String, String> mapperToDbxml, CountDownLatch sublatch) {
        this.mapperxml = mapperxml;
        this.dbxmlPath = dbxmlPath;
        this.mapperToDbxml = mapperToDbxml;
        this.sublatch = sublatch;
    }

    @Override
    public void run() {
        LOG.debug("ColectMappersClassInfoTask start");
        //获取mapperxml中的mapper类名，一个mapperxml中对应一个mapper类
        try {
            String mapperClass = ((Attribute) Bt.xml(Resources.getResourceAsStream(mapperxml), "/mapper/@namespace").get(0)).getValue();
            this.mapperToDbxml.put(mapperClass, dbxmlPath);
            System.out.println("数据库文件：" + dbxmlPath + "  mapper文件：" + mapperxml + "    mapper类：" + mapperClass);
            LOG.debug("ColectMappersClassInfoTask end");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sublatch.countDown();
        }
    }
}
