import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.*;


/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */


public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    private Map<Long, Node> spots = new HashMap<>();
    private Map<Long, Location> sites = new HashMap<>();
    private Map<String, List<Long>> wayNames = new HashMap<>();
    private TrieST<Long> st = new TrieST<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
//            File inputFile = new File(dbPath);
//            FileInputStream inputStream = new FileInputStream(inputFile);
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dbPath);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        // TODO: Your code here.
        Iterator<Map.Entry<Long, Node>> nodes = spots.entrySet().iterator();
        while (nodes.hasNext()) {
            Map.Entry<Long, Node> item = nodes.next();
            Node n = item.getValue();
            if (noConnectNode(n)) {
                nodes.remove();
            }
        }
    }

    private class Node {
        double lon;
        double lat;
        List<Long> adjNodes;
        Set<String> nodeNames;

        Node(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
            this.adjNodes = new LinkedList<>();
            this.nodeNames = new HashSet<>();
        }
    }

    private class Location {
        double lon;
        double lat;
        String name;

        Location(double lon, double lat, String name) {
            this.lon = lon;
            this.lat = lat;
            this.name = name;
        }
    }

    void addNode(long id, double lon, double lat) {
        Node newNode = new Node(lon, lat);
        spots.put(id, newNode);
    }

    void addLocation(long id, double lon, double lat, String locationName) {
        Location newLoc = new Location(lon, lat, locationName);
        sites.put(id, newLoc);
    }

    void addEdge(long id1, long id2) {
        if (isNodeValid(id1) && isNodeValid(id2)) {
            spots.get(id1).adjNodes.add(id2);
            spots.get(id2).adjNodes.add(id1);
        } else {
            throw new IllegalArgumentException("Node not valid.");
        }
    }

    boolean noConnectNode(Node n) {
        if (n.adjNodes.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    double locationLat(long id) {
        if (isLocationValid(id)) {
            return sites.get(id).lat;
        } else {
            throw new IllegalArgumentException("Location not found.");
        }

    }

    double locationLon(long id) {
        if (isLocationValid(id)) {
            return sites.get(id).lon;
        } else {
            throw new IllegalArgumentException("Location not found.");
        }
    }

    private boolean isNodeValid(long id) {
        if(spots.containsKey(id)) {
            return true;
        }
        return false;
    }

    private boolean isLocationValid(long id) {
        if (sites.containsKey(id)) {
            return true;
        }
        return false;
    }

    String getWayName(long id) {

        if (isLocationValid(id)) {
            return sites.get(id).name;
        } else {
            throw new IllegalArgumentException("Location not found.");
        }

    }

    public List<String> getLocationsByPrefix(String prefix) {
        List<String> locationNameList = new LinkedList<>();

        String cleaned = cleanString(prefix);
        for (String key : st.keysWithPrefix(cleaned)) {
            Long id = wayNames.get(key).get(0);
            String fullName = getWayName(id);
            locationNameList.add(fullName);
        }

        return locationNameList;
    }

    public List<Long> getLocations(String locationName) {
        List<Long> locationList = new LinkedList<>();

        String cleaned = cleanString(locationName);
        for (long v : wayNames.get(cleaned)) {
            locationList.add(v);
        }

        return locationList;
    }


    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return spots.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        if (isNodeValid(v)) {
            return spots.get(v).adjNodes;
        } else {
            throw new IllegalArgumentException("Node not found.");
        }
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double shortest = Double.MAX_VALUE;
        long closestId = -117;
        for (long id : spots.keySet()) {
//            Node n = spots.get(id);
            double currentDistance = distance(lon(id), lat(id), lon, lat);
            if (currentDistance < shortest) {
                shortest = currentDistance;
                closestId = id;
            }
        }
        return closestId;

    }

    /**
     * Gets the longitude of a vertex.
     * @param id The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long id) {
        if (isNodeValid(id)) {
            return spots.get(id).lon;
        } else {
            throw new IllegalArgumentException("Node not found.");
        }
    }

    /**
     * Gets the latitude of a vertex.
     * @param id The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long id) {
        if (isNodeValid(id)) {
            return spots.get(id).lat;
        } else {
            throw new IllegalArgumentException("Node not found.");
        }
    }

    /**
     * Add a high way to the spots
     * @param highWay The ids of the highWay.
     * @param nodeName The name of the node
     */
    void addHighWay(List<Long> highWay, String nodeName) {
        int length = highWay.size();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                spots.get(highWay.get(i)).nodeNames.add(nodeName);
                continue;
            }
            addEdge(highWay.get(i - 1), highWay.get(i));
            spots.get(highWay.get(i)).nodeNames.add(nodeName);
        }

    }

    /**
     * Add name to the id
     * @param id The id of the highWay.
     * @param lon The lon of the id.
     * @param lat The lat of the id.
     * @param name The name of the id
     */

    void addName(long id, double lon, double lat, String name) {
        String goodName = cleanString(name);

        if (!wayNames.containsKey(goodName)) {
            wayNames.put(goodName, new LinkedList<>());
        }

        wayNames.get(goodName).add(id);
        addLocation(id, lon, lat, name);
        st.put(goodName, id);

    }

    Set<String> nodeNames(long v) {
        Set<String> names = new HashSet<>();
        for (String way : spots.get(v).nodeNames) {
            names.add(way);
        }
        return names;
    }
}
