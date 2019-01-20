import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        long startNode = g.closest(stlon, stlat);
        long endNode = g.closest(destlon, destlat);

        Set<Long> visited = new HashSet<>();
        Map<Long, Double> distanceTo = new HashMap<>();
        Map<Long, Long> edgeTo = new HashMap<>();
        System.out.println("start: " + startNode + " end: " + endNode);

        PriorityQueue<Long> fringe = new PriorityQueue<Long>(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                double o1Dis = g.distance(o1, endNode) + distanceTo.get(o1);
                double o2Dis = g.distance(o2, endNode) + distanceTo.get(o2);

                if (o1Dis > o2Dis) {
                    return 1;
                } else if (o1Dis < o2Dis) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        setAllDistancesToInfinity(g.vertices(), distanceTo, edgeTo);
        distanceTo.replace(startNode, 0.0);
        edgeTo.put(startNode, (long) 0);
        fringe.add(startNode);

        aStarSearch(fringe, visited, distanceTo, edgeTo, g, endNode);

//        LinkedList<Long> route = new LinkedList<>();
//        route = buildRoute(edgeTo, endNode);

        return buildRoute(edgeTo, endNode); // FIXME
    }

    private static void setAllDistancesToInfinity(Iterable<Long> vertices, Map<Long, Double> distanceTo,
                                                  Map<Long, Long> edgeTo) {
        for (long v : vertices) {
            distanceTo.put(v, Double.POSITIVE_INFINITY);
            edgeTo.put(v, (long) -117);
        }

    }

    private static void aStarSearch(PriorityQueue<Long> fringe, Set<Long> visited, Map<Long, Double> distanceTo,
                                    Map<Long, Long> edgeTo, GraphDB g, long endNode) {
        while (!fringe.isEmpty()) {
            long currentNode = fringe.poll();
            if (currentNode == endNode) {
                break;
            }

            if (visited.contains(currentNode)) {
                continue;
            }

            visited.add(currentNode);
            for (long neighbor : g.adjacent(currentNode)) {
                double distance = distanceTo.get(currentNode) + g.distance(currentNode, neighbor);
                if (distance < distanceTo.get(neighbor)) {
                    distanceTo.put(neighbor, distance);
                    edgeTo.put(neighbor, currentNode);
                    fringe.add(neighbor);
                }
            }
        }

    }

    private static LinkedList<Long> buildRoute(Map<Long, Long> edgeTo, long endNode) {
        LinkedList<Long> route = new LinkedList<>();

        for (long n = endNode; n != 0; n = edgeTo.get(n)) {
            route.add(0, n);
        }

        return route;
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> nav = new ArrayList<>();
        String wayName = "";
        int currentDir = NavigationDirection.START;
        double bearing = 0;
        long startNode = route.get(0);
        double prevBearing = g.bearing(startNode, route.get(1));
        double distance = 0;

        

        return nav; // FIXME
    }

    private static String wayName(GraphDB g, long v1, long v2) {
        Set<String> v1Names = g.nodeNames(v1);
        Set<String> v2Names = g.nodeNames(v2);

        for (String v1Name : v1Names) {
            for (String v2Name : v2Names) {
                if (v1Name.equals(v2Name)) {
                    return v1Name;
                }
            }
        }
        return "";
    }

    private static int direction(double bearing) {
        double abs = 0;
        boolean negative = false;
        if (bearing <= 180) {
            abs = Math.abs(bearing);
        } else {
            abs = 360 - Math.abs(bearing);
            negative = true;
        }

        if (abs <= 15) {
            return NavigationDirection.STRAIGHT;
        }
        if (abs > 15 && abs <= 30) {
            if (negative) {
                return NavigationDirection.SLIGHT_LEFT;
            } else {
                return NavigationDirection.SLIGHT_RIGHT;
            }
        }
        if (abs > 30 && abs <= 100) {
            if (negative) {
                return NavigationDirection.LEFT;
            } else {
                return NavigationDirection.RIGHT;
            }
        }
        else {
            if (negative) {
                return NavigationDirection.SHARP_LEFT;
            } else {
                return NavigationDirection.SHARP_RIGHT;
            }
        }

    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
