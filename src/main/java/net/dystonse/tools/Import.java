package net.dystonse.tools;

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
    static Option optHelp, optCreate, optShow, optRect;
    static CommandLine line;
 
    public static void main(String[] args) throws SQLException, IOException {
        parseCommandLine(args);
        
        if(line.hasOption(optShow.getOpt())) {
            System.out.println(Database.getCreateStatement(Database.getDatabaseName()));
            System.exit(0);
        }

        Connection conn = Database.getConnection(line);
        if(line.hasOption(optCreate.getOpt())) {
            Statement smt = conn.createStatement();
            smt.execute(Database.getCreateStatement(Database.getDatabaseName()));
            conn.close();
            System.out.println("Table has been created.");
            System.exit(0);
        }

        BoundingBox bb = parseRect();
        JsonArray vehicles = getVehicles(bb);
        importVehicles(vehicles);
        conn.close();
        System.out.println("Program ended successfully.");
    }


    static Options createOptions(boolean requireCredentials, boolean requireRect) {
        optCreate   = Option.builder("c")   .longOpt("create-table").                                  desc("Executes a CREATE TABLE statement instead of inserting data").build();
        optShow     = Option.builder("s")   .longOpt("show-table").                                    desc("Prints out a CREATE TABLE statement instead of inserting data").build();
        optHelp     = Option.builder("help").longOpt("help").                                          desc("Print command line syntax").build();
        optRect     = Option.builder("r")   .longOpt("rect").    required(requireRect).       hasArg().desc("Use this bounding box to limit queries. Provide four values separated by semicolons").build();
        
        OptionGroup group = new OptionGroup();
        group.addOption(optCreate);
        group.addOption(optShow);
        group.addOption(optHelp);
        
        Options options = new Options();
        Database.addCommandLineOptions(options, requireCredentials);

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

    static BoundingBox parseRect() throws IllegalArgumentException {
        String rectString  = line.getOptionValue(optRect.getOpt());
        try {
            return new BoundingBox(rectString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Parameter 'rect' must be present and have 4 numbers, separated by semicolons. " + e.getMessage());
        }
    }

    static JsonArray getVehicles(BoundingBox boundingBox) throws IOException {
        System.out.println( "Fetching data from VBB..." );

        String sURL = "http://fahrinfo.vbb.de/bin/query.exe/dny?" + boundingBox.getQueryString() + "&tpl=trains2json2&look_productclass=127&look_json=yes&performLocating=1&look_nv=zugposmode|2|interval|0|intervalstep|1|";

        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object. 

        int count = rootobj.get("n").getAsInt();
        System.out.println( "...retrieved " + count + " records." );
        
        return rootobj.get("t").getAsJsonArray();
    }
 
    static void importVehicles(JsonArray vehicles) throws SQLException {
        Connection conn = Database.getConnection(line);
        final PreparedStatement statement = conn.prepareStatement("INSERT INTO `" + Database.getDatabaseName() + "`.`realtime-input` (`id`, `datasource`, `compound_id`, `productclass`, `d`, `name`, `destination`, `location`, `timestamp`, `delay`) VALUES (NULL, 'VBB', ?, ?, ?, ?, ?, GeomFromText(?), CURRENT_TIMESTAMP, ?);");
        
        for(JsonElement vehicleElement : vehicles) {
            JsonObject vehicle = vehicleElement.getAsJsonObject();

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


    }

}
