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
            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost/catchmeifyoucandatabase", "root", "");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertIntoDatabase(String user, String pass){
        try{
            Statement stmt = (Statement)connection.createStatement();
            stmt.execute("INSERT INTO `users`(`Username`, `Password`) VALUES ('"+user+"','"+pass+"')");
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean isRegistered(String user){
        try{
            Statement stmt = (Statement)connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Username FROM user");

            while (rs.next())
                if(user.equals(rs.getString(1)))
                    return true;

            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }
    public boolean isRegistered(String user, String pass){
        try{
            Statement stmt = (Statement)connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Password FROM user WHERE Username='"+user+"' ");
            while (rs.next())
                if(pass.equals(rs.getString(1)))
                    return true;

            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }
}
