package com.quvideo.qa.common.tools;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * MyBatis的工具类
 *
 * @author yehao
 */
public abstract class MyBatisUtil {
    //protected static Logger LOG = LogManager.getLogger(MyBatisUtil.class);
    protected static Logger LOG = LoggerFactory.getLogger(MyBatisUtil.class);
    //支持多个数据源
    private static Map<String, SqlSessionFactory> SqlSessionFactorMap = new ConcurrentHashMap<String, SqlSessionFactory>();
    private static Map<String, SSHinfo> sshinfoMap = new HashMap<String, SSHinfo>();
    private static Map<String, SqlSession> sqlSessionMap = new HashMap<String, SqlSession>();
    private static String env = null;

    static {
        env = Bt.initEnv();
    }

    /**
     * 初始化Session工厂
     * @param dbsourceFile   数据初始化xml文件
     */
    public static void initialFactory(String dbsourceFile) {
        // synchronized (MyBatisUtil.class) {
        if (SqlSessionFactorMap.get(dbsourceFile) == null) {
            try {
                LOG.info("初始化数据库配置：" + dbsourceFile);
                InputStream inputStream = Resources.getResourceAsStream(dbsourceFile);
                Properties properties = new Properties();
                if (initSSHInfo(dbsourceFile, env)) {
                    properties.setProperty(env + ".addr", "127.0.0.1:" + sshinfoMap.get(dbsourceFile).localPort);
                }
                SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, env, properties);
                SqlSessionFactorMap.put(dbsourceFile, sqlSessionFactory);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            //   }
        }

    }



    public static <T> T getMapper(String dbname, Class<T> mapperClass) {
        initsqlSession(dbname);
        return sqlSessionMap.get(dbname).getMapper(mapperClass);
    }

    private static void initsqlSession(String dbsourceFile) {
        synchronized (MyBatisUtil.class) {
            if (sqlSessionMap.get(dbsourceFile) == null) {
                LOG.info("初始化" + dbsourceFile + "的sqlsession");
                SSHinfo ssHinfo = sshinfoMap.get(dbsourceFile);
                if (null != ssHinfo) {
                    JschUtils.goSSH(ssHinfo.localPort, ssHinfo.sship, 22, ssHinfo.sshUsername, ssHinfo.password, ssHinfo.remotoHost, ssHinfo.remotoPort);
                }
                //Collection<String> pmn= SqlSessionFactorMap.get(dbsourceFile).getConfiguration().getEnvironment().getDataSource().s;

                sqlSessionMap.put(dbsourceFile, SqlSessionFactorMap.get(dbsourceFile).openSession(true));
            }
        }
    }

    private static boolean initSSHInfo(String dbsourceFile, String envid) {

//        String sship=null;
//        String sshUsername=null;
//        String privateKey=null;
        SSHinfo sshinfo = new SSHinfo();
        /**
         *  <property name="online.ssh.ip" value="116.62.220.253"/>
         *  <property name="online.ssh.username" value="longzhujiang"/>
         *  <property name="online.ssh.privateKey" value="config/cmdb_dsa/cmdb_id_dsa"/>
         */
        try {
            List<?> properties = Bt.xml(Resources.getResourceAsStream(dbsourceFile), "//configuration/properties/property");
            Iterator it = properties.iterator();

            while (it.hasNext()) {
                Element p = (Element) it.next();
                switch (p.attributeValue("name").trim().replace(envid, "")) {
                    case ".ssh.ip":
                        sshinfo.sship = p.attributeValue("value");
                        break;
                    case ".ssh.username":
                        sshinfo.sshUsername = p.attributeValue("value");
                        break;
                    case ".ssh.password":
                        sshinfo.password = p.attributeValue("value");
                        break;
                    case ".addr":
                        String[] addr = p.attributeValue("value").split(":");
                        sshinfo.remotoHost = addr[0];
                        sshinfo.remotoPort = Integer.parseInt(addr[1]);
                        break;

                    default:

                }
            }

            if (sshinfo.isOK()) {
                synchronized (MyBatisUtil.class) {

                    //查询已经存在的跳板信息是否匹配
                    for (String key : sshinfoMap.keySet()) {
                        if (sshinfoMap.get(key).asString().equals(sshinfo.asString())) {
                            LOG.info(dbsourceFile + " 与 " + key + " 使用相同跳板机配置");
                            sshinfo.localPort = sshinfoMap.get(key).localPort;
                            break;
                        }
                    }
                    if (0 == sshinfo.localPort) {
                        InetAddress inetAddress = InetAddress.getLoopbackAddress();
                        ServerSocket serverSocket = new ServerSocket(0, 1, inetAddress);
                        sshinfo.localPort = serverSocket.getLocalPort();
                        serverSocket.close();
                    }
                    sshinfoMap.put(dbsourceFile, sshinfo);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sshinfo.isallOK();
    }

    /**
     * 提交并关闭session
     */
    public static void closeAllSession() {
        synchronized (MyBatisUtil.class) {
            if (sqlSessionMap.size() != 0) {
                LOG.info("开始关闭*所有*数据库链接！");
                for (String dbsourceFile : sqlSessionMap.keySet()) {
                    closeSession(dbsourceFile);
                }
                LOG.info("结束关闭*所有*数据库链接！");
            }
        }
    }


    public static void closeSession(String dbsourceFile) {
        synchronized (MyBatisUtil.class) {
            SqlSession sqlSession = sqlSessionMap.get(dbsourceFile);
            if (sqlSession != null) {
                {
                    LOG.info("关闭" + dbsourceFile + "数据库链接");
                    try {
                        sqlSession.commit();
                        sqlSession.close();
                        sqlSessionMap.put(dbsourceFile, null);
                        LOG.info(dbsourceFile + "数据库链接关闭成功");
                    } catch (Exception e) {
                        LOG.error(dbsourceFile + "数据库链接关闭异常", e);
                    }
                }
            }
        }
    }

    private static class SSHinfo {
        public String sship = null;
        public String sshUsername = null;
        public String password = null;
        public String remotoHost = null;
        public int remotoPort = 0;
        public int localPort = 0;

        public boolean isOK() {
            return null != sship && null != sshUsername && null != password;
        }

        public boolean isallOK() {
            return null != sship && null != sshUsername && null != password && 0 != localPort && null != remotoHost && 0 != remotoPort;
        }

        public String asString() {
            return sship.trim() + sshUsername.trim() + password.trim() + remotoHost.trim() + remotoPort;
        }
    }


    private void temp() {
        SqlSessionFactory sqlSessionFactory = null;

    }

}

