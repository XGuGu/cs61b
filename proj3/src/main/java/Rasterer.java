import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private Boolean query_success;
    private static final double ROOT_WIDTH = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON;
    private static final double ROOT_HEIGHT = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
    private static final double ROOT_LONDPP = ROOT_WIDTH / MapServer.TILE_SIZE;

    public Rasterer() {
        query_success = true;
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        //System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        double lonDPP = (params.get("lrlon") - params.get("ullon")) / params.get("w");
        int depth = getDepth(lonDPP);

        results.put("depth", depth);
        double xStepCount = ROOT_WIDTH / Math.pow(2, depth);
        double yStepCount = ROOT_HEIGHT / Math.pow(2, depth);

        int[] xCounts = horizonCount(params.get("ullon"), params.get("lrlon"), xStepCount);
        int[] yCounts = verticalCount(params.get("ullat"), params.get("lrlat"), yStepCount);


        String[][] images = getImageFiles(depth, xCounts, yCounts);

        results.put("raster_ul_lon", MapServer.ROOT_ULLON + xCounts[0] * xStepCount);
        results.put("raster_lr_lon", MapServer.ROOT_ULLON + (1.0 + xCounts[1]) * xStepCount);
        results.put("raster_ul_lat", MapServer.ROOT_ULLAT - yCounts[0] * yStepCount);
        results.put("raster_lr_lat", MapServer.ROOT_ULLAT - (1.0 + yCounts[1]) * yStepCount);
        results.put("render_grid", images);

        if (params.get("ullon") > params.get("lrlon") || params.get("lrlat") > params.get("ullat") ||
                params.get("lrlon") <= MapServer.ROOT_ULLON || params.get("ullon") >= MapServer.ROOT_LRLON ||
                params.get("lrlat") >= MapServer.ROOT_ULLAT || params.get("ullat") <= MapServer.ROOT_LRLAT) {
            query_success = false;
        }
        results.put("query_success", query_success);



        return results;
    }

    private String[][] getImageFiles(int d, int[] xCounts, int[] yCounts) {
        int xlength = xCounts[1] + 1 - xCounts[0];
        int ylength = yCounts[1] + 1 - yCounts[0];
        String[][] result = new String[ylength][xlength];

        int yStart = yCounts[0];
        int yEnd = yCounts[1];
        int xStart = xCounts[0];
        int xEnd = xCounts[1];

        for (int i = 0; i + yStart <= yEnd; i++) {
            for (int j = 0; j + xStart <= xEnd; j++) {
                String fileName = "d";
                fileName += Integer.toString(d) + "_x";
                fileName += Integer.toString(j + xStart) + "_y";
                fileName += Integer.toString(i + yStart) + ".png";
                result[i][j] = fileName;
            }
        }

        return result;
    }

    private int getDepth(double req_lonDPP) {
        int depth = 0;
        while (ROOT_LONDPP > req_lonDPP) {
            depth++;
            req_lonDPP *= 2;
        }

        if (depth > 7) {
            return 7;
        } else {
            return depth;
        }
    }

    private int[] horizonCount(double userUllon, double userLrlon, double step) {
        int[] result = new int[2];
        double currentLon = MapServer.ROOT_ULLON + step;
        int a = 0;
        for (; userUllon > currentLon; a++) {
            currentLon += step;
        }
        result[0] = a;

        for (; userLrlon > currentLon; a++) {
            currentLon += step;
            if (currentLon > MapServer.ROOT_LRLON) {
                break;
            }
        }
        result[1] = a;

        return result;
    }

    private int[] verticalCount(double userUllat, double userLrlat, double step) {
        int[] result = new int[2];
        double currentLat = MapServer.ROOT_ULLAT - step;
        int a = 0;
        for (; userUllat < currentLat; a++) {
            currentLat -= step;
        }
        result[0] = a;

        for (; userLrlat < currentLat; a++) {
            currentLat -= step;
            if (currentLat < MapServer.ROOT_LRLAT) {
                break;
            }
        }
        result[1] = a;

        return result;
    }


}