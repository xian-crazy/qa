package com.quvideo.qa.common.tools;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class JschUtils {
    protected static Logger LOG  = LoggerFactory.getLogger(JschUtils.class);
    protected static Set<String> sshinfoset=new HashSet<>();

    //建立跳板机通道
    public static void goSSH(int localPort, String sshHost, int sshPort,
                             String sshUserName, String password, String remotoHost, int remotoPort) {
        String sshinfo=sshHost.trim()+remotoHost.trim()+remotoPort+localPort;
        synchronized (JschUtils.class) {
            if (sshinfoset.contains(sshinfo)) {
                LOG.warn("访问服务器：" + remotoHost + ":" + remotoPort + " 的跳板通道：" + sshHost +":"+localPort+ " 已经存在！");
            } else {
                LOG.warn("创建访问服务器：" + remotoHost + ":" + remotoPort + " 的跳板通道：" + sshHost + " 本地端口：" + localPort);
                try {
                    JSch jsch = new JSch();

                    //jsch.addIdentity(Resources.getResourceAsFile(privateKey).getAbsolutePath());
                    //jsch.
                    LOG.info("identity added ");
                    Session session = jsch.getSession(sshUserName, sshHost, sshPort);
                    session.setPassword(password);
                    LOG.info("session created.");
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();
                    LOG.info(session.getServerVersion());//这里打印SSH服务器版本信息

                    //通过ssh连接到mysql机器

                    int tyrs=5;
                    while(tyrs>=0) {
                        try {
                            int assinged_port = session.setPortForwardingL("127.0.0.1",localPort, remotoHost, remotoPort);
                            LOG.info("建立跳板机通道成了");
                            LOG.info("localhost:" + assinged_port + " -> " + remotoHost + ":" + remotoPort);
                            break;
                        }catch (JSchException e){
                            e.printStackTrace();
                            Thread.sleep(1000*1000);
                            tyrs--;
                        }
                    }

                    sshinfoset.add(sshinfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //连接数据库测试
    public static Connection connDB() {
        //声明Connection对象
        Connection con = null;
        //驱动程序名
        String driver = "com.mysql.cj.jdbc.Driver";
        //URL指向要访问的数据库名称
        String url = "jdbc:mysql://localhost:2000/xiaoyingtvcenter";
        //Mysql配置是的用户名
        String user = "xiaoyingtv";
        //Mysql配置时的密码
        String password = "Quvideo2012";
        //遍历查询结果集
        try {
            //加载驱动程序
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            con = DriverManager.getConnection(url, user, password);
            System.out.println("连接数据库成了");
            con.close();
        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return con;
    }

    @Test
    public void demo() {
       // goSSH(2000, "116.62.220.253", 22, "qa-autotest", "KEbHGc95j2", "rr-bp1t51ve2779h8yjc.mysql.rds.aliyuncs.com", 3306);
       // connDB();

        try{
            InetAddress inetAddress= InetAddress.getLoopbackAddress();
            ServerSocket serverSocket=new ServerSocket(0,10,inetAddress);
            serverSocket.close();
            LOG.info(serverSocket.getLocalPort()+"");
            ServerSocket serverSocket1=new ServerSocket(0,10,inetAddress);
            serverSocket1.close();
            LOG.info(serverSocket1.getLocalPort()+"");
            ServerSocket serverSocket2=new ServerSocket(0,10,inetAddress);
            serverSocket2.close();
            LOG.info(serverSocket2.getLocalPort()+"");
            Thread.sleep(1000*1000);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
