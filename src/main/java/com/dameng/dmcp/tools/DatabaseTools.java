package com.dameng.dmcp.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: 陈磊
 * @Date: 2018/8/1
 * @Description:
 */
public class DatabaseTools {

    /**
     * 获得操作系统环境变量
     * @param key
     * @return
     */
    private String getEnvironmentProperty(String key) {
        return System.getenv(key);
    }

    /**
     * 获得操作系统环境变量，支持默认值
     * @param key
     * @param defaultValue
     * @return
     */
    private String getEnvironmentProperty(String key, String defaultValue) {
        String environmentProperty = this.getEnvironmentProperty(key);
        if (environmentProperty == null) {
            return defaultValue;
        }
        return environmentProperty;
    }

    /**
     * 执行SQL脚本
     */
    private void execute() throws Exception {
        String driver_name = this.getEnvironmentProperty("DRIVER_NAME");
        System.out.println("driver_name: " + driver_name);
        String url = this.getEnvironmentProperty("URL");
        System.out.println("url: " + url);
        String username = this.getEnvironmentProperty("USERNAME");
        System.out.println("username: " + username);
        String password = this.getEnvironmentProperty("PASSWORD");
        System.out.println("password: <protected>");

        // 加载驱动
        DriverManager.registerDriver((Driver) Class.forName(driver_name).newInstance());
        // 建立连接
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (Statement statement = connection.createStatement()) {
                // 读取SQL文件
                StringBuffer fileContent = new StringBuffer();
                try (FileReader fileReader = new FileReader(this.getEnvironmentProperty("SQL_PATH", "/root/db_tools/script/execute.sql"))) {
                    try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                        while (bufferedReader.ready()) {
                            String line = bufferedReader.readLine();
                            // 忽略注释
                            if (line.startsWith("--")) {
                                continue;
                            }
                            fileContent.append(line);
                        }
                    }
                }
                System.out.println("sqls: " + fileContent);
                // 环境变量替换
                StringBuffer sqlContent = new StringBuffer();
                Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
                Matcher matcher = pattern.matcher(fileContent);
                while (matcher.find()) {
                    String environmentProperty = this.getEnvironmentProperty(matcher.group(1));
                    matcher.appendReplacement(sqlContent, environmentProperty);
                }
                matcher.appendTail(sqlContent);
                System.out.println("sqls: " + sqlContent);
                // 执行SQL
                String[] sqls = sqlContent.toString().split(";");
                if (this.getEnvironmentProperty("IGNORE_ERROR", "false").equalsIgnoreCase("true")) {
                    // 忽略异常，遇到异常继续执行下一句
                    System.out.println("ignore_error: true");
                    for (String sql : sqls) {
                        System.out.println("sql: " + sql);
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 2);
                        }
                        try{
                            statement.execute(sql);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } else {
                    // 不忽略异常，遇到异常终止执行
                    System.out.println("ignore_error: false");
                    for (String sql : sqls) {
                        System.out.println("sql: " + sql);
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 2);
                        }
                        statement.addBatch(sql);
                    }
                    statement.executeBatch();
                }

            }
        }
    }

    public static void main(String[] args) {
        try {
            new DatabaseTools().execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
