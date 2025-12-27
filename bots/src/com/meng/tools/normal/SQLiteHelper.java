package com.meng.tools.normal;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteHelper {
    public static void init() {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
        } catch (Exception e) {
            ExceptionCatcher.getInstance().catchException(null, e);
        }
        System.out.println("Opened database successfully");
    }   

}
