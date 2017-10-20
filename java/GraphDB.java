import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.ArrayList;

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
    private HashMap<Long, LinkedList<GraphDB.Node>> adj;
    private HashMap<Long, GraphDB.Node> vertices;
    private TrieST<String> names;
    private HashMap<Long, GraphDB.Node> verticesWithNames;
    private HashMap<String, LinkedList<Long>> nodeWithNames;
    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        this.adj = new HashMap<>();
        this.vertices = new HashMap<>();
        names = new TrieST<>();
        nodeWithNames = new HashMap<>();
        verticesWithNames = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
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
        // Your code here.
        LinkedList<Long> removeKeys = new LinkedList<>();
        for (Map.Entry<Long, LinkedList<GraphDB.Node>> entry : adj.entrySet()) {
            if (entry.getValue().size() == 0) {
                long key = entry.getKey();
                removeKeys.addLast(key);
            }
        }
        for (long id : removeKeys) {
            adj.remove(id);
            vertices.remove(id);
        }

    }

    void addEdge(long v, long w) {
        adj.get(v).addLast(vertices.get(w));
        adj.get(w).addLast(vertices.get(v));
    }

    void addNode(GraphDB.Node node) {
        vertices.put(node.id, node);
        LinkedList<GraphDB.Node> newLinked = new LinkedList<>();
        adj.put(node.id, newLinked);
    }

    void addWays(ArrayList<Long> ways) {
        long key1 = ways.get(0);
        for (int i = 1; i < ways.size(); i++) {
            long key2 = ways.get(i);
            addEdge(key1, key2);
            key1 = key2;
        }
    }

    void addName(String v) {
        names.put(cleanString(v), v);
    }

    TrieST<String> getTrie() {
        return names;
    }

    void addExtraInfo(Long id, String value) {
        Node x = vertices.get(id);
        verticesWithNames.put(id, x);
        x.name = cleanString(value);
        x.actualName = value;
        if (nodeWithNames.containsKey(x.name)) {
            nodeWithNames.get(x.name).addLast(id);
        } else {
            LinkedList<Long> newLL = new LinkedList<>();
            newLL.addLast(id);
            nodeWithNames.put(x.name, newLL);
        }
    }

    LinkedList<Map<String, Object>> getlocations(String locationName) {
        LinkedList<Map<String, Object>> result = new LinkedList<>();
        locationName = cleanString(locationName);
        HashMap<String, Object> location = new HashMap<>();

        for (Long id : nodeWithNames.get(locationName)) {
            GraphDB.Node node = verticesWithNames.get(id);

            location.put("lat", node.lat);
            location.put("lon", node.lon);
            location.put("name", node.actualName);
            location.put("id", node.id);
            result.add(location);
            location = new HashMap<>();
        }
        return result;

    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return vertices.keySet();
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        return adjIDs(v);
    }

    private LinkedList<Long> adjIDs(long v) {
        LinkedList<Long> adjIDs = new LinkedList<>();
        for (GraphDB.Node vertex : adj.get(v)) {
            adjIDs.addLast(vertex.id);
        }
        return adjIDs;
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        double lonV = vertices.get(v).lon,
               latV = vertices.get(v).lat,
               lonW = vertices.get(w).lon,
               latW = vertices.get(w).lat;

        double londiff = lonV - lonW,
               latdiff = latV - latW;

        return Math.sqrt(londiff * londiff + latdiff * latdiff);
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        double closestSoFar = Double.MAX_VALUE;
        long closestID = 0;

        for (Long id : vertices()) {
            double distance = calcDistance(id, lon, lat);
            if (distance < closestSoFar) {
                closestID = id;
                closestSoFar = distance;
            }
        }
        return closestID;
    }

    private double calcDistance(long v, double lon, double lat) {
        double lonV = vertices.get(v).lon,
               latV = vertices.get(v).lat;

        double londiff = lonV - lon,
               latdiff = latV - lat;

        return Math.sqrt(londiff * londiff + latdiff * latdiff);
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return vertices.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return vertices.get(v).lat;
    }

    int size() {
        return vertices.size();
    }

    static class Node {
        final long id;
        final double lat;
        final double lon;
        String name;
        String actualName;

        Node(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.name = "";
            this.actualName = "";
        }
    }
}
