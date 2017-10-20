import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;

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
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */
    private static HashMap<Long, Double> distTo = new HashMap<>();
    private static HashMap<Long, Long> edgeTo = new HashMap<>();
    private static ArrayHeap<Long> pq = new ArrayHeap<>();
    private static HashSet<Long> pqValues = new HashSet<>();

    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                                double destlon, double destlat) {
        LinkedList<Long> spt = new LinkedList<>();
        long source = g.closest(stlon, stlat);
        long destination = g.closest(destlon, destlat);
        //System.out.println(source);
        // Set distance to infinity
        for (Long id : g.vertices()) {
            distTo.put(id, Double.POSITIVE_INFINITY);
        }
        double heuristic = g.distance(source, destination);
        distTo.put(source, 0.0);

        // relax vertices in order of distance from source
        pq.insert(source, distTo.get(source) + heuristic);
        pqValues.add(source);

        while (!pq.isEmpty()) {
            long v = pq.removeMin();
            pqValues.remove(v);
            if (v == destination) {
                break;
            }
            for (long e : g.adjacent(v)) {
                relax(v, e, destination, g);
            }
        }

        long prev = edgeTo.get(destination);
        spt.addFirst(destination);
        while (prev != source) {
            spt.addFirst(prev);
            prev = edgeTo.get(prev);
        }
        spt.addFirst(source);
        return spt;
    }

    private static void relax(long from, long to, long destination, GraphDB g) {
        double heuristic = g.distance(to, destination);
        double weight = g.distance(from, to);
        if (distTo.get(to) > distTo.get(from) + weight) {
            distTo.put(to, distTo.get(from) + weight);
            edgeTo.put(to, from);
            if (pqValues.contains(to)) {
                pq.changePriority(to, distTo.get(to) + heuristic);
            } else {
                pq.insert(to, distTo.get(to) + heuristic);
                pqValues.add(to);
            }
        }
    }

}
