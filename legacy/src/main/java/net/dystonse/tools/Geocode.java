package net.dystonse.tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;


import java.io.IOException;

import org.apache.commons.cli.*;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

public class Geocode 
{
    static Option optHelp, optRoute;
    static CommandLine line;
 
    public static void main(String[] args) throws SQLException, IOException {
        parseCommandLine(args);
        //System.in.read();
        bla();
    }

    private static List<List<GeoPosition>> loadRealtimedata(String routeName) throws SQLException {
        List<List<GeoPosition>> ret = new LinkedList<List<GeoPosition>>();

        Connection conn = Database.getConnection(line);
         
        System.out.println("Querying data for route " + routeName);
        Statement outerStmt = conn.createStatement();
        Statement innerStmt = conn.createStatement();
        ResultSet outerRs = outerStmt.executeQuery("SELECT DISTINCT `compound_id` FROM `realtime-input` WHERE `name` = '"+routeName+"' LIMIT 0,10");
        
        final int MAX_TIME_DIFF = 1000 * 60 * 3;

        while(outerRs.next()) {
            String id = outerRs.getString("compound_id");
            System.out.println("Fetching samples for train " + id + "...");
            List<GeoPosition> track = new ArrayList<GeoPosition>();
		
            ResultSet innerRs = innerStmt.executeQuery("SELECT `location`,`timestamp` FROM `realtime-input` WHERE `compound_id`='" + id + "' AND `name` = '"+routeName+"' ORDER BY `timestamp`;");
            Timestamp lastTs = null;
            while(innerRs.next()) {
                Point p = new Point((byte[])innerRs.getObject("location"));
                Timestamp ts = innerRs.getTimestamp("timestamp");
                if(lastTs != null && (ts.getTime () - lastTs.getTime ()) > MAX_TIME_DIFF) {
                     ret.add(track);
                     track = new ArrayList<GeoPosition>();
                }

                track.add(new GeoPosition(p.latitude, p.longitude));
                lastTs = ts;
            }
            innerRs.close();
            ret.add(track);
        }
        outerRs.close();     
        return ret;
    }

    private static List<List<GeoPosition>> loadShapes(String routeName) throws SQLException {
        List<List<GeoPosition>> ret = new LinkedList<List<GeoPosition>>();

        Connection conn = Database.getConnection(line);
         
        System.out.println("Querying data for route " + routeName);
        Statement outerStmt = conn.createStatement();
        Statement innerStmt = conn.createStatement();
        ResultSet outerRs = outerStmt.executeQuery("SELECT DISTINCT shape_id FROM trips WHERE route_id IN (SELECT route_id FROM routes WHERE route_short_name = '"+routeName+"');");
        
        while(outerRs.next()) {
            String id = outerRs.getString("shape_id");
            System.out.println("Fetching samples for shape " + id + "...");
            List<GeoPosition> track = new ArrayList<GeoPosition>();
		
            ResultSet innerRs = innerStmt.executeQuery("SELECT shape_pt_sequence, shape_pt_lat, shape_pt_lon FROM shapes WHERE shape_id = '" + id + "' ORDER BY shape_pt_sequence;");
            while(innerRs.next()) {
                //Point p = new Point((byte[])innerRs.getObject("location"));
                double latitude = innerRs.getDouble("shape_pt_lat");
                double longitude = innerRs.getDouble("shape_pt_lon");
                track.add(new GeoPosition(latitude, longitude));
            }
            innerRs.close();
            ret.add(track);
        }
        outerRs.close();     
        return ret;
    }

    public static void bla() throws SQLException {
        String routeName = line.getOptionValue(optRoute.getOpt());
        
        RoutePainter routePainter = new RoutePainter(true, false);
        RoutePainter trainPainter = new RoutePainter(false, true);
        for(List<GeoPosition> track : loadRealtimedata(routeName)) {
            trainPainter.addRoute(track);
        }

        for(List<GeoPosition> track : loadShapes(routeName)) {
            routePainter.addRoute(track);
        }

        // TODO Die obere Struktur mit den zwei Schleifen in Methode auslagern, duplozieren, für Routen anpassen. RoutePainter parametrisieren für Linien, Kreise oder beides.
        // SELECT DISTINCT shape_id FROM trips WHERE route_id IN (SELECT route_id FROM routes WHERE route_short_name = "S42");
        // SELECT shape_pt_sequence, shape_pt_lat, shape_pt_lon FROM shapes WHERE shape_id =1119 ORDER BY shape_pt_sequence

        JXMapViewer mapViewer = new JXMapViewer();

		// Display the viewer in a JFrame
		JFrame frame = new JFrame("JXMapviewer2 Example 2");
		frame.getContentPane().add(mapViewer);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// Create a TileFactoryInfo for OpenStreetMap
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);
		mapViewer.setTileFactory(tileFactory);

        // Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);

		mapViewer.addMouseListener(new CenterMapListener(mapViewer));
		
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
		
		mapViewer.addKeyListener(new PanKeyListener(mapViewer));


	
		// Set the focus
		mapViewer.zoomToBestFit(new HashSet<GeoPosition>(routePainter.getRoute(0)), 0.7);
/*
		// Create waypoints from the geo-positions
		Set<Waypoint> waypoints = new HashSet<Waypoint>();
        int i = 0;
        List<GeoPosition> track = routePainter.getRoute(i);
        while(track != null) {
            waypoints.add(new DefaultWaypoint(track.get(0)));
            track = routePainter.getRoute(++i);
        }

		// Create a waypoint painter that takes all the waypoints
		WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setWaypoints(waypoints);
		*/
		// Create a compound painter that uses both the route-painter and the waypoint-painter
	    List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(routePainter);
		painters.add(trainPainter);
		
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);      
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
        optRoute = Option.builder("r").longOpt("route").hasArg().required().desc("Name of the route which shall be shown.").build();
        options.addOption(optHelp);
        options.addOption(optRoute);
        Database.addCommandLineOptions(options, requireCredentials);        

        return options;
    }

}

class RoutePainter implements Painter<JXMapViewer>
{
	private boolean antiAlias = false;
    private boolean lines = true;
    private boolean dots = false;
	
	private List<List<GeoPosition>> tracks;
	
	/**
	 * @param track the track
	 */
	public RoutePainter(boolean lines, boolean dots)
	{
        this.lines = lines;
        this.dots = dots;
		this.tracks = new ArrayList<List<GeoPosition>>();
	}

    public void addRoute(List<GeoPosition> track) {
        this.tracks.add(track);
    }

    public List<GeoPosition> getRoute(int i) {
        if(i >= tracks.size())
            return null;
        return tracks.get(i);
    }

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h)
	{
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int i = 0;

        for(List<GeoPosition> track : tracks) {

            // do the drawing
            //g.setColor(Color.BLACK);
            //g.setStroke(new BasicStroke(4));
            //drawRoute(g, map, track);

            // do the drawing again
            g.setColor(new Color(Color.HSBtoRGB(((float)i) / 12.0f, 1, 0.5f)));
            g.setStroke(new BasicStroke(3));

            drawRoute(g, map, track);
            i++;
        }

		g.dispose();
	}

	/**
	 * @param g the graphics object
	 * @param map the map
	 */
	private void drawRoute(Graphics2D g, JXMapViewer map, List<GeoPosition> track)
	{
		int lastX = 0;
		int lastY = 0;
		
		boolean first = true;
        int zoom = map.getZoom();
        TileFactory tf = map.getTileFactory();
		
		for (GeoPosition gp : track)
		{
			// convert geo-coordinate to world bitmap pixel
			Point2D pt = tf.geoToPixel(gp, zoom);

            if(dots) {
                g.drawArc((int) pt.getX()-10, (int) pt.getY()-10, 20, 20, 0, 360);
            }

            if(lines) {
                if (first)
                {
                    first = false;
                }
                else
                {
                    g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
                }
                
                lastX = (int) pt.getX();
                lastY = (int) pt.getY();
            }
		}
	}
}