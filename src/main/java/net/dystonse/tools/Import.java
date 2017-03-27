package net.dystonse.tools;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Types;
import java.sql.SQLException;
import java.sql.PreparedStatement;

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
    static Option optHost, optUser, optPassword, optHelp;

    public static CommandLine parseCommandLine(String[] args) {
        optHost     = Option.builder("h")   .longOpt("host").    required().hasArg().desc("Hostname or IP of the database server").build();
        optUser     = Option.builder("u")   .longOpt("user").    required().hasArg().desc("User name for the database server").build();
        optPassword = Option.builder("p")   .longOpt("password").required().hasArg().desc("Password for the database server").build();
        optHelp     = Option.builder("help").longOpt("help").                        desc("Print command line syntax").build();

        Options options = new Options();
        options.addOption(optHost);
        options.addOption(optUser);
        options.addOption(optPassword);
        options.addOption(optHelp);

        CommandLineParser parser = new DefaultParser();
        try {
           CommandLine line = parser.parse(options, args);
           if(line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Import", options, true);
           }
           return line;
        } catch(ParseException exp) {
            System.err.println("Parsing command line failed. Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Import", options, true);
            System.exit(-1);
            return null;
        }
    }

    public static void main(String[] args) throws SQLException, IOException
    {
        CommandLine line = parseCommandLine(args);

        System.out.println( "Hole Daten von der VBB..." );

        int minX = 13268700;
        int minY = 52461900;
        int maxX = 13481900;
        int maxY = 52554200;
        String sURL = "http://fahrinfo.vbb.de/bin/query.exe/dny?look_minx="+minX+"&look_maxx="+maxX+"&look_miny="+minY+"&look_maxy="+maxY+"&tpl=trains2json2&look_productclass=127&look_json=yes&performLocating=1&look_nv=zugposmode|2|interval|0|intervalstep|1|";

        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object. 
        JsonArray vehicles = rootobj.get("t").getAsJsonArray();
        int count = rootobj.get("n").getAsInt();
        System.out.println( "...habe " + count + " Datensätze erhalten." );
        
        
        System.out.println("Verbinde mit der Datenbank...");

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(line.getOptionValue("user"));
        dataSource.setPassword(line.getOptionValue("password"));
        dataSource.setServerName(line.getOptionValue("host"));
        Connection conn = dataSource.getConnection();

        final PreparedStatement statement = conn.prepareStatement("INSERT INTO `dystonse`.`realtime-input` (`id`, `datasource`, `compound_id`, `productclass`, `d`, `name`, `destination`, `location`, `timestamp`, `delay`) VALUES (NULL, 'VBB', ?, ?, ?, ?, ?, GeomFromText(?), CURRENT_TIMESTAMP, ?);");

        for(int i = 0; i < count; i++) {
            JsonObject vehicle = vehicles.get(i).getAsJsonObject();

            System.out.println("Wrting vehicle data for " + vehicle.get("n").getAsString().trim() + " to " + vehicle.get("l").getAsString());

            statement.setString(1, vehicle.get("i").getAsString());
            statement.setInt(2, vehicle.get("c").getAsInt());
            statement.setInt(3, vehicle.get("d").getAsInt());
            statement.setString(4, vehicle.get("n").getAsString().trim());
            statement.setString(5, vehicle.get("l").getAsString());
            statement.setString(6, "POINT("+ vehicle.get("y").getAsFloat()/1000000.0f + " " + vehicle.get("x").getAsFloat()/1000000.0f + ")");
            if(vehicle.get("rt") != null) {
                statement.setFloat(7, vehicle.get("rt").getAsInt());
            } else {
                statement.setNull(7, Types.FLOAT);
            }
            statement.addBatch();
        }
        System.out.println("Alle Daten vorberietet, führe Batch aus.");
        statement.executeBatch();
        statement.close();

        conn.close();
        System.out.println("Erfolgreich beendet.");
    }
}
