package com.zli.example;

import java.sql.*;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class Database
{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://lt-pc1.uwaterloo.ca/";
    private static final String DATABASE = "cs858";
    private static final String USER = "root";
    private static final String PASS = "cs858FTW!";

    public int executeSQL(String sql) {
        int lastInsertedId = 0;
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(URL + DATABASE, USER, PASS);
            stmt = conn.createStatement();
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()){
                lastInsertedId = rs.getInt(1);
            }
            stmt.close();
            conn.close();
            
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return lastInsertedId;
    }
}
