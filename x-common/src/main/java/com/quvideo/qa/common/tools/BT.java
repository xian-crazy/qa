package com.quvideo.qa.common.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.quvideo.qa.common.tools.MyBatisUtil;
import com.quvideo.qa.common.tools.QuvAssert;
import com.quvideo.qa.common.tools.RandomTools;
import io.restassured.response.Response;
import org.apache.ibatis.io.Resources;
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

import static com.quvideo.qa.common.tools.BT.LOG;

/**
 * 工具类Basic tools
 */
public class BT {
    // protected static Logger LOG = LogManager.getLogger(BT.class);
    protected static Logger LOG = LoggerFactory.getLogger(BT.class);
    public static final String RNL = System.getProperty("line.separator");//系统换行符
    private static Map<String, Properties> proertiesMap = new HashMap<String, Properties>();
    private static Map<String, String> mapperToDbxml = new ConcurrentHashMap<String, String>();


    //生成随机数/字符串/日期工具库
    public static RandomTools ran = RandomTools.getInstance();

   

   

    /**
     * <pre>
     *  e.g.     BT.getProv("/config/application.properties", "default.env");
     * </pre>
     *
     * @param propertisFile 读取的配置文件路径
     * @param key           配置键
     * @return key String值
     */
    public static String GetProv(String propertisFile, String key) {
        String value;
        value = GetProv(propertisFile).getProperty(key);
        LOG.info("获取文配置文件：" + propertisFile + "; " + key + " = " + value);
        return value;
    }

    /**
     *
     * @param propertisFile BT.etProv("/config/application.properties")G
     * @return Properties对象
     */
    public static Properties GetProv(String propertisFile) {
        synchronized (proertiesMap) {
            if (proertiesMap.get(propertisFile) == null) {
                Properties p = new Properties();
                InputStream is = BT.class.getResourceAsStream(propertisFile);
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

    /**
     * 获取当前测试用例执行环境，优先从 命令行-Denv 取值，当命令行没有设置时，从工程resources//config/application.properties中读取default.env值
     * @return
     */
    public static String initEnv() {
        String env = null;
        env = System.getProperty("env");
        if (null == env) {
            env = BT.GetProv("/config/application.properties", "default.env");
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
     * @return
     */
    public static Object getValueByEvn(String evnkey, Object value, Object... keyvalues) {
        Map<String, Object> kv = new HashMap<>();
        kv.put(evnkey, value);
        for (int i = 0; i < keyvalues.length; i++) {
            kv.put((String) keyvalues[i], keyvalues[++i]);
        }
        return kv.get(BT.initEnv());
    }


    public static void colseAllMybatisSession(){
        MyBatisUtil.closeAllSession();
    }


    /**
     * 根据resources/database目录及其子目录下的mybatis的configuration xml文件实例化当前类中所有声明的Mapper类
     * @param caseIns 包含有Mapper类生命的类实例，一般为this
     */
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

    /**
     * 根据resources/database目录及其子目录下的mybatis的configuration xml文件实例化Mapper类
     * @param mapperClass  mapper类
     * @param <T> 泛型
     * @return   mapper类实例
     */
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



    public static Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 把字符串转为url
     *
     * @param urlstr
     * @return  URL对象
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
     * 如果响应是jsonArray，且以jsonObject作为数据类型，验证array第index个jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeystr(QuvAssert softAssert, Response response, int index, String keys) {
        String[] keysarr = keys.split(",");
        softassertJsonObjectKeyarr(softAssert, response, index, keysarr);
    }

    /**
     * 如果响应body是jsonObject，验证jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeystr(QuvAssert softAssert, Response response, String keys) {
        String[] keysarr = keys.split(",");
        softassertJsonObjectKeyarr(softAssert, response, keysarr);
    }

    /**
     * 验证响应body是jsonObject，jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeyarr(QuvAssert softAssert, Response response, String... keys) {
        JSONObject jsonObject = JSON.parseObject(response.body().asString());
        softassertJsonObjectKeyarr(softAssert, jsonObject, keys);
    }

    /**
     * 验证响应是jsonArry，且以jsonObject作为数据类型，jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeyarr(QuvAssert softAssert, Response response, int index, String... keys) {
        JSONArray jsonArray = JSON.parseArray(response.body().asString());
        JSONObject jsonObject = jsonArray.getJSONObject(index);
        softassertJsonObjectKeyarr(softAssert, jsonObject, keys);
    }


    /**
     * 验证jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeyarr(QuvAssert softAssert, JSONObject jsonObject, String... keys) {
        for (String k : keys) {
            softAssert.assertTrue(jsonObject.containsKey(k.trim()), "返回对象不包含属性：" + k);
        }
    }


    /**
     * 验证jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeystr(QuvAssert softAssert, JSONObject jsonObject, String keys) {
        String[] keysarr = keys.split(",");
        softassertJsonObjectKeyarr(softAssert, jsonObject, keysarr);
    }


    /**
     * 扫描并数据库配置
     */
    private static void colectMappersInfoAndinitialFactory() {
        synchronized (BT.mapperToDbxml) {
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
            BT.listDirectory(databasePath, files);

            //多线程处理mapperinfo
            CountDownLatch latch = new CountDownLatch(files.size());

            ExecutorService executors = Executors.newFixedThreadPool(files.size());
            List<Future<String>> resultList = new ArrayList<>();
            //遍历database目录下的多个数据源xml配置
            for (File dbxml : files) {
                executors.submit(new ColectMappersInfoTask(dbxml, databasePath, BT.mapperToDbxml, latch));
                //  Future<String> future =  executors.submit(new ColectMappersInfoTask(dbxml,databasePath,mapperToDbxml));
                //  resultList.add(future);
            }
            // LOG.warn("await colectMappersInfo");
            latch.await();
            latch.await(3, TimeUnit.MINUTES);
            Set<String> dbxmlset = new HashSet<>();
            for (String dbxml : BT.mapperToDbxml.values()) {
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
            List<Node> mapperxmls = BT.xml(Resources.getResourceAsStream(dbxmlPath), "/configuration/mappers/mapper/@resource");

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
               //获取mapperxml中的mapper类名，一个mapperxml中对应一个mapper类
        try {
            String mapperClass = ((Attribute) BT.xml(Resources.getResourceAsStream(mapperxml), "/mapper/@namespace").get(0)).getValue();
            this.mapperToDbxml.put(mapperClass, dbxmlPath);
            LOG.debug("数据库文件：" + dbxmlPath + "  mapper文件：" + mapperxml + "    mapper类：" + mapperClass);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sublatch.countDown();
        }
    }
}
