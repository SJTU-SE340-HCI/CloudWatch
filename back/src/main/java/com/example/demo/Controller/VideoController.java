package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Controller
@RequestMapping(value = "/videoroom")
@CrossOrigin
public class VideoController {

    private String DBDriver;
    private String DBURL;
    private String DBUser;
    private String DBPass;
    private Connection conn=null;
    private Statement stmt=null;
    private ResultSet rs=null;

    private float time=0;
    private float oldtime=0;

    public void connect() {
        DBDriver="com.mysql.cj.jdbc.Driver";
        DBURL="jdbc:mysql://localhost:3306/video?serverTimezone=UTC";
        DBUser="root";//用户名
        DBPass="123456";
        try{
            Class.forName(DBDriver);//加载驱动程序
            //System.out.print("数据库驱动程序加载成功");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            conn=DriverManager.getConnection(DBURL,DBUser,DBPass);//取得连接对象
            stmt=conn.createStatement();//取得SQL语句对象
            //System.out.print("连接数据库成功");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping
    @ResponseBody
    public String Create(){
        String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";
        int maxPos = chars.length();
        String roomid="";
        for (int i = 0; i < 6; i++) {
            roomid += chars.charAt((int) (Math.random() * maxPos));
        }
        System.out.println("id:" + roomid);
        try {
            connect();
            String sql = "INSERT INTO room(RoomId)VALUES('"+roomid+"')";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return roomid;
    }

    @RequestMapping(value = "/room")
    @ResponseBody
    public boolean Enter(String roomid) throws Exception {
        //System.out.print(roomid);
        connect();
        String sql = "select * from room where RoomId='" + roomid + "'";
        rs = stmt.executeQuery(sql);
        //System.out.print(rs.next());
        if (rs.next()) {
            rs.close();
            stmt.close();
            conn.close();
            return true;
        }
        return false;

    }

    @RequestMapping(value = "/play")
    @ResponseBody
    public float play(String roomid,float curTime) throws Exception {
        //System.out.print(roomid);
        System.out.print(curTime);
        System.out.print(oldtime);

        connect();

        String sql2 = "select * from room where RoomId='" + roomid + "'";
        rs = stmt.executeQuery(sql2);
        if(rs.next()){
            time=rs.getFloat("VideoTime");
            System.out.print(time);
        }

        if((curTime-time)>0&&(curTime-time)<4){
            oldtime=time;
            String sql = "update room set VideoTime = "+curTime+" where RoomId ='" + roomid + "'";
            stmt.executeUpdate(sql);
            rs.close();
            stmt.close();
            conn.close();
            return curTime;
        }
        else if ((time-oldtime)>0&&(time-oldtime)<4){
            String sql = "update room set VideoTime = "+curTime+" where RoomId ='" + roomid + "'";
            stmt.executeUpdate(sql);
            rs.close();
            stmt.close();
            conn.close();
            return curTime;
        }
        else {
            curTime=time;
            oldtime=time-2;
        }

        System.out.print(curTime+"\n");

        rs.close();
        stmt.close();
        conn.close();
        return curTime;
    }

    @RequestMapping(value = "/pause")
    @ResponseBody
    public float pause(String roomid,float curTime) throws Exception {
        //System.out.print(roomid);
        //System.out.print(curTime);

        connect();

        String sql = "update room set VideoTime = " + curTime + " where RoomId ='" + roomid + "'";
        stmt.executeUpdate(sql);

        rs.close();
        stmt.close();
        conn.close();
        return curTime;
    }

}
