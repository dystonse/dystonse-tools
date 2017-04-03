package net.dystonse.tools;

import java.sql.Connection;
import java.sql.Types;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.Arrays;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.commons.cli.*;


public class Geocode 
{
    static Option optHelp;
    static CommandLine line;
 
    public static void main(String[] args) throws SQLException, IOException {
        parseCommandLine(args);
        bla();
    }

    public static void bla() throws SQLException {
        Connection conn = Database.getConnection(line);
        String routeName = "U5";
        System.out.println("Querying data for route " + routeName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DISTINCT `destination` FROM `realtime-input` WHERE `name` = '"+routeName+"'");
        System.out.println("Route " + routeName + " goes to...");
        while(rs.next()) {
            String destination = rs.getString("destination");
            System.out.println("...to " + destination);
        }
        rs.close();
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
                formatter.printHelp("Geocode", options, true);
                System.exit(0);
           }
        //    if(!line.hasOption(optShow.getOpt())) {
        //         // restart options parsing with a fresh options & parser instance
        //         options = createOptions(true, true);
        //         parser = new DefaultParser();
        //         line = parser.parse(options, args);
        //    }
        } catch(ParseException exp) {
            System.err.println("Parsing command line failed. Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator((Option a, Option b) -> 0);
            formatter.setWidth(100);
            formatter.printHelp("Geocode", options, true);
            System.exit(-1);
        }
    }

    static Options createOptions(boolean requireCredentials, boolean requireOptions) {
        Options options = new Options();

        optHelp = Option.builder("help").longOpt("help").desc("Print command line syntax").build();
        options.addOption(optHelp);
        Database.addCommandLineOptions(options, requireCredentials);        

        return options;
    }

}