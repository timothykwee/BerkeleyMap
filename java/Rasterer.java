import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.
    private QuadTree quadTree;

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        imgRoot = "";
        Tile val = new Tile(imgRoot, MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT,
                                MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT);
        quadTree = new QuadTree(val);

        quadTree.fillQuadTree(val);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
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
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {

        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        String[][] files;
        QuadTree.TreeNode[][] tiles;
        double ullat = params.get("ullat"),
               ullon = params.get("ullon"),
               lrlat = params.get("lrlat"),
               lrlon = params.get("lrlon"),
               width = params.get("w");

        results.put("render_grid", null);
        results.put("raster_ul_lon", null);
        results.put("raster_ul_lat", null);
        results.put("raster_lr_lon", null);
        results.put("raster_lr_lat", null);
        results.put("depth", null);
        results.put("query_success", false);

        if (!checkValid(ullat, ullon, lrlat, lrlon)) {
            return results;
        }

        List<QuadTree.TreeNode> intersections = bfs(ullat, ullon, lrlat, lrlon, width);
        HashSet<QuadTree.TreeNode> distinctLatitudes = new HashSet<>();
        for (QuadTree.TreeNode tile : intersections) {
            distinctLatitudes.add(tile);
        }
        int rows = distinctLatitudes.size();

        int cols = intersections.size() / rows;
        intersections.sort(null);
        files = new String[rows][cols];
        tiles = new QuadTree.TreeNode[rows][cols];

        int index = 0;
        for (int i = rows - 1; i >= 0; i--) {
            for (int j = 0; j < cols; j++) {
                String name = intersections.get(index).value.getName();
                name = "img/" + name + ".png";
                files[i][j] = name;
                tiles[i][j] = intersections.get(index);
                index += 1;
            }
        }
        //System.out.println(files);
        results.put("render_grid", files);
        results.put("raster_ul_lon", tiles[0][0].value.getUpperLeftLon());
        results.put("raster_ul_lat", tiles[0][0].value.getUpperLeftLat());
        results.put("raster_lr_lon", tiles[rows - 1][cols - 1].value.getLowerRightLon());
        results.put("raster_lr_lat", tiles[rows - 1][cols - 1].value.getLowerRightLat());
        results.put("depth", tiles[0][0].value.getName().length());
        results.put("query_success", true);

        return results;
    }

    private boolean checkValid(double ullat1, double ullon1, double lrlat1, double lrlon1) {
        double ullat = MapServer.ROOT_ULLAT,
               ullon = MapServer.ROOT_ULLON,
               lrlat = MapServer.ROOT_LRLAT,
               lrlon = MapServer.ROOT_LRLON;

        if (ullon1 > lrlon || lrlon1 < ullon || ullat1 < lrlat || lrlat1 > ullat) {
            return false;
        }
        return !(ullon1 > lrlon1 || ullat < lrlat);

    }

    private List<QuadTree.TreeNode> bfs(double ullat, double ullon, double lrlat,
                                        double lrlon, double width) {
        List<QuadTree.TreeNode> intersections = new ArrayList<>();
        ArrayList<QuadTree.TreeNode> fringe = new ArrayList<>();

        fringe.add(quadTree.getRoot());

        while (!fringe.isEmpty()) {
            QuadTree.TreeNode tileNode = fringe.remove(0);
            if (intersect(tileNode, ullat, ullon, lrlat, lrlon)
                    && (lonDPP(tileNode, ullon, lrlon, width) || tileNode.isLeaf())) {
                intersections.add(tileNode);
                continue;
            }

            if (intersect(tileNode.left, ullat, ullon, lrlat, lrlon)) {
                fringe.add(tileNode.left);
            }
            if (intersect(tileNode.midLeft, ullat, ullon, lrlat, lrlon)) {
                fringe.add(tileNode.midLeft);
            }
            if (intersect(tileNode.midRight, ullat, ullon, lrlat, lrlon)) {
                fringe.add(tileNode.midRight);
            }
            if (intersect(tileNode.right, ullat, ullon, lrlat, lrlon)) {
                fringe.add(tileNode.right);
            }
        }
        return intersections;
    }

    private boolean lonDPP(QuadTree.TreeNode tileNode, double ullon, double lrlon, double width) {
        double lonDPPTile = (tileNode.value.getLowerRightLon() - tileNode.value.getUpperLeftLon())
                             / MapServer.TILE_SIZE;
        double lonDPPQuery = (lrlon - ullon) / width;

        return lonDPPTile <= lonDPPQuery;
    }

    private boolean intersect(QuadTree.TreeNode tileNode, double ullat, double ullon,
                              double lrlat, double lrlon) {
        double ullat1 = tileNode.value.getUpperLeftLat(),
               ullon1 = tileNode.value.getUpperLeftLon(),
               lrlat1 = tileNode.value.getLowerRightLat(),
               lrlon1 = tileNode.value.getLowerRightLon();
        if (ullon > lrlon1 || lrlon < ullon1 || ullat < lrlat1 || lrlat > ullat1) {
            return false;
        }

        return true;
    }

    public int size() {
        return quadTree.getSize();
    }

}
