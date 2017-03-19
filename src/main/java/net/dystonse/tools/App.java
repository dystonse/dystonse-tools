package net.dystonse.tools;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Arrays;
import java.sql.ResultSet;
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

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException, IOException
    {
        String user, host, password;
        try {
            List<String> argList = Arrays.asList(args);
            int hostPos = argList.indexOf("--host");
            host = argList.get(hostPos + 1);
            int userPos = argList.indexOf("--user");
            user = argList.get(userPos + 1);
            int passwordPos = argList.indexOf("--password");
            password = argList.get(passwordPos + 1);
        } catch (Exception e) {
            System.err.println("Usage: <App> --host host --user user --password password");
            return;
        }

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
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setServerName(host);
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
