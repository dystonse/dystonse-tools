package net.dystonse.tools;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Types;
import java.util.Arrays;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.io.IOException;
import java.net.HttpURLConnection;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.cli.*;
public class Import 
{
    static Option optHost, optUser, optPassword, optHelp, optDatabase, optCreate, optShow, optRect;
    static  int minX, minY, maxX, maxY;    

    static Connection conn;
    static CommandLine line;
    static JsonArray vehicles;

    static int count;

    static void setupConnection() throws SQLException {
        if(conn != null) {
            return;
        }

        System.out.println("Verbinde mit der Datenbank...");

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(line.getOptionValue(optUser.getOpt()));
        dataSource.setPassword(line.getOptionValue(optPassword.getOpt()));
        dataSource.setServerName(line.getOptionValue(optHost.getOpt()));
        conn = dataSource.getConnection();
    }

    static Options createOptions(boolean requireCredentials, boolean requireRect) {
        optHost     = Option.builder("h")   .longOpt("host").    required(requireCredentials).hasArg().desc("Hostname or IP of the database server").build();
        optUser     = Option.builder("u")   .longOpt("user").    required(requireCredentials).hasArg().desc("User name for the database server").build();
        optPassword = Option.builder("p")   .longOpt("password").required(requireCredentials).hasArg().desc("Password for the database server").build();
        optDatabase = Option.builder("d")   .longOpt("database").required().                  hasArg().desc("The database name").build();
        optCreate   = Option.builder("c")   .longOpt("create-table").                                  desc("Executes a CREATE TABLE statement instead of inserting data").build();
        optShow     = Option.builder("s")   .longOpt("show-table").                                    desc("Prints out a CREATE TABLE statement instead of inserting data").build();
        optHelp     = Option.builder("help").longOpt("help").                                          desc("Print command line syntax").build();
        optRect     = Option.builder("r")   .longOpt("rect").    required(requireRect).       hasArg().desc("Use this bounding box to limit queries. Provide four values separated by semicolons").build();
        
        OptionGroup group = new OptionGroup();
        group.addOption(optCreate);
        group.addOption(optShow);
        group.addOption(optHelp);
        
        Options options = new Options();
        options.addOption(optHost);
        options.addOption(optUser);
        options.addOption(optPassword);
        options.addOption(optDatabase);
        options.addOption(optRect);
        options.addOptionGroup(group);

        return options;
    }

    public static void parseCommandLine(String[] args) {
        Options options = createOptions(false, false);
        CommandLineParser parser = new DefaultParser();
        try {
           line = parser.parse(options, args);
           if(line.hasOption(optHelp.getOpt())) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth(100);
                formatter.setOptionComparator((Option a, Option b) -> 0);
                formatter.printHelp("Import", options, true);
                System.exit(0);
           }
           if(!line.hasOption(optShow.getOpt())) {
                // restart options parsing with a fresh options & parser instance
                options = createOptions(true, line.hasOption(optCreate.getOpt()));
                parser = new DefaultParser();
                line = parser.parse(options, args);
           }
        } catch(ParseException exp) {
            System.err.println("Parsing command line failed. Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator((Option a, Option b) -> 0);
            formatter.setWidth(100);
            formatter.printHelp("java -cp <...> Import", options, true);
            System.exit(-1);
        }
    }

    static void optAssert(boolean assertion, String message) {
        if(!assertion) {
            throw new IllegalArgumentException(message);
        }
    }

    static void parseRect() throws IllegalArgumentException {
        final int MIN_LON = 6;
        final int MAX_LON = 15;
        final int MIN_LAT = 47;
        final int MAX_LAT = 56;

        String   rectString  = line.getOptionValue(optRect.getOpt());
        optAssert(rectString != null, "Parameter 'rect' must be present and have 4 numbers, separated by semicolons.");
        String[] rectStrings = rectString.split(";");
        optAssert(rectStrings.length == 4, "Parameter 'rect' must be present and have 4 numbers, separated by semicolons.");
        float[] rectValues = new float[4];
        for(int i = 0; i < 4; i++) {
            rectValues[i] = Float.parseFloat(rectStrings[i]);
            if(rectValues[i] < MIN_LON * 1_000_000)
                rectValues[i] *= 1_000_000;
        }
        
        Arrays.sort(rectValues);
        for(int i = 0; i < 2; i++)
            optAssert(rectValues[i] >  MIN_LON * 1_000_000 && rectValues[i] <  MAX_LON * 1_000_000, "Lon values must be between " + MIN_LON + " and " + MAX_LON + " degrees. You can use floats or integers, in any order.");
        for(int i = 2; i < 4; i++)
            optAssert(rectValues[i] >  MIN_LAT * 1_000_000 && rectValues[i] <  MAX_LAT * 1_000_000, "Lat values must be between " + MIN_LAT + " and " + MAX_LAT + " degrees. You can use floats or integers, in any order.");
        minX = (int)rectValues[0];
        maxX = (int)rectValues[1];
        minY = (int)rectValues[2];
        maxY = (int)rectValues[3];
    }
    static void getVehicles() throws IOException {
        System.out.println( "Fetching data from VBB..." );

        String sURL = "http://fahrinfo.vbb.de/bin/query.exe/dny?look_minx="+minX+"&look_maxx="+maxX+"&look_miny="+minY+"&look_maxy="+maxY+"&tpl=trains2json2&look_productclass=127&look_json=yes&performLocating=1&look_nv=zugposmode|2|interval|0|intervalstep|1|";

        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object. 
        vehicles = rootobj.get("t").getAsJsonArray();
        count = rootobj.get("n").getAsInt();
        System.out.println( "...retrieved " + count + " records." );
    }

    public static void main(String[] args) throws SQLException, IOException
    {
        parseCommandLine(args);

        if(line.hasOption(optShow.getOpt())) {
            System.out.println(getCreateStatement());
            System.exit(0);
        }

        parseRect();
        setupConnection();
        String database = line.getOptionValue("database");
        if(line.hasOption(optCreate.getOpt())) {
            Statement smt = conn.createStatement();
            smt.execute(getCreateStatement());
            conn.close();
            System.out.println("Table has been created.");
            System.exit(0);
        }

        getVehicles();

        final PreparedStatement statement = conn.prepareStatement("INSERT INTO `" + database + "`.`realtime-input` (`id`, `datasource`, `compound_id`, `productclass`, `d`, `name`, `destination`, `location`, `timestamp`, `delay`) VALUES (NULL, 'VBB', ?, ?, ?, ?, ?, GeomFromText(?), CURRENT_TIMESTAMP, ?);");

        for(int i = 0; i < count; i++) {
            JsonObject vehicle = vehicles.get(i).getAsJsonObject();

            System.out.println("Wrting vehicle data for " + vehicle.get("n").getAsString().trim() + " to " + vehicle.get("l").getAsString());

            statement.setString(1, vehicle.get("i").getAsString());
            statement.setInt   (2, vehicle.get("c").getAsInt());
            statement.setInt   (3, vehicle.get("d").getAsInt());
            statement.setString(4, vehicle.get("n").getAsString().trim());
            statement.setString(5, vehicle.get("l").getAsString());
            statement.setString(6, "POINT("+ vehicle.get("y").getAsFloat()/1000000.0f + " " +
                                             vehicle.get("x").getAsFloat()/1000000.0f + ")");
            if(vehicle.get("rt") != null) {
                statement.setFloat(7, vehicle.get("rt").getAsInt());
            } else {
                statement.setNull(7, Types.FLOAT);
            }
            statement.addBatch();
        }
        System.out.println("All data prepared, executing batch.");
        statement.executeBatch();
        statement.close();

        conn.close();
        System.out.println("Program ended successfully.");
    }

    static String getCreateStatement() {
        String database = line.getOptionValue("database");
        String createStatement = "CREATE DATABASE IF NOT EXISTS `" + database + "` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;\n" +
                "  USE `" + database + "`;\n" +
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
