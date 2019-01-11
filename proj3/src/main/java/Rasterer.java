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
    private static final double ROOT_W = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON;
    private static final double ROOT_H = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
    private static final double ROOT_LONDPP = ROOT_W / MapServer.TILE_SIZE;

    public Rasterer() {
        // YOUR CODE HERE

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
        double lonDPP = (params.get("lrlon") - params.get("ullon")) / params.get("w");
        int depth = getDepth(lonDPP);
        Map<String, Object> results = new HashMap<>();

        results.put("depth", depth);

        // Check for valid query box
        return results;
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


}