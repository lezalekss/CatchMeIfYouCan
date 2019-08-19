package com.rmt;

import java.sql.*;
import java.util.logging.Logger;

public final class DBConnection {
    // connection with oracle for every user thread
    private Connection connection;
    private Logger logger = Logger.getLogger(ServerAppMain.class.getName());
    public DBConnection(){
        try{
            logger.info("Trying to setup connection with database");
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection= DriverManager.getConnection("jdbc:oracle:thin:@//localhost:1521/xepdb1", "catchmeifyoucan", "catchmeifyoucan!");
        } catch (ClassNotFoundException e) {
           e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertIntoDatabase(String user, String pass){
        try{
            Statement stmt = (Statement)connection.createStatement();
            stmt.execute("INSERT INTO users(username, userpass) VALUES ('"+user+"','"+pass+"')");
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean isRegistered(String username){
        try{
            Statement stmt = (Statement)connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username FROM users");

            while (rs.next())
                if(username.equals(rs.getString(1)))
                    return true;
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPasswordCorrect(String username, String password){
        try{
            Statement stmt = (Statement)connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT userpass FROM users WHERE username='"+username+"' ");
            while (rs.next())
                if(password.equals(rs.getString(1)))
                    return true;
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
