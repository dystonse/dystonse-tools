package net.dystonse.tools;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Types;
import java.util.Arrays;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.apache.commons.cli.*;

public class Database {

    static Connection conn  ;

    static Option optHost, optUser, optPassword, optDatabase;

    static String databaseName;

    static Connection getConnection(CommandLine line) throws SQLException {
        if(conn == null) {
            System.out.println("Verbinde mit der Datenbank...");

            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser(line.getOptionValue(optUser.getOpt()));
            dataSource.setPassword(line.getOptionValue(optPassword.getOpt()));
            dataSource.setServerName(line.getOptionValue(optHost.getOpt()));
            conn = dataSource.getConnection();

            databaseName = line.getOptionValue("database");
            Statement stmt = conn.createStatement();
            stmt.execute("USE `" + databaseName + "`;");
        }
        return conn;
    }

    static String getDatabaseName() {
        return databaseName;
    }

    static void addCommandLineOptions(Options options, boolean requireCredentials) {
        optHost     = Option.builder("h")   .longOpt("host").    required(requireCredentials).hasArg().desc("Hostname or IP of the database server").build();
        optUser     = Option.builder("u")   .longOpt("user").    required(requireCredentials).hasArg().desc("User name for the database server").build();
        optPassword = Option.builder("p")   .longOpt("password").required(requireCredentials).hasArg().desc("Password for the database server").build();
        optDatabase = Option.builder("d")   .longOpt("database").required().                  hasArg().desc("The database name").build();
        
        options.addOption(optHost);
        options.addOption(optUser);
        options.addOption(optPassword);
        options.addOption(optDatabase);
    }

    static String getCreateStatement(String databaseName) {
        String createStatement = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;\n" +
                "  USE `" + databaseName + "`;\n" +
                "\n" +
                "  CREATE TABLE IF NOT EXISTS `realtime-input` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'internal ID for each data row',\n" +
                "  `datasource` varchar(30) NOT NULL DEFAULT 'test',\n" +
                "  `compound_id` varchar(30) NOT NULL COMMENT 'id with slashes as given by Hafas',\n" +
                "  `productclass` int(11) NOT NULL COMMENT 'attribute ''c'' from Hafas',\n" +
                "  `d` int(11) NOT NULL COMMENT 'attribute ''d'' from Hafas',\n" +
                "  `name` varchar(30) NOT NULL,\n" +
                "  `destination` varchar(50) NOT NULL,\n" +
                "  `location` point NOT NULL,\n" +
                "  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                "  `delay` float DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  KEY `timestamp` (`timestamp`)\n" +
                ") ENGINE=InnoDB  DEFAULT CHARSET=latin1";
        return createStatement;
    }
}
