/**
 * Created by parisl on 4/19/17.
 */
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class TrieST<Value> {
    //private static final int R = 128; // extended ASCII
    private Node root = new Node();

    private static class Node {
        private Object val;
        //private Node[] next = new Node[R];
        private HashMap<Character, Node> children = new HashMap<>();

    }

    public void put(String key, Value val) {
        root = put(root, key, val, 0);
    }

    private Node put(Node currRoot, String key, Value val, int len) {
        if (currRoot == null) {
            currRoot = new Node();
        }
        if (len == key.length()) {
            currRoot.val = val;
            return currRoot;
        }
        char c = key.charAt(len);
        //currRoot.next[c] = put(currRoot.next[c], key, val, len + 1);
        currRoot.children.put(c, put(currRoot.children.get(c), key, val, len + 1));
        return currRoot;
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public Value get(String key) {
        Node x = get(root, key, 0);
        if (x == null || x.val == null) {
            return null;
        }
        return (Value) x.val;
    }

    private Node get(Node currRoot, String key, int len) {
        if (currRoot == null) {
            return null;
        }
        if (len == key.length()) {
            return currRoot;
        }
        char c = key.charAt(len);
        return get(currRoot.children.get(c), key, len + 1);
    }


    private void collect(Node root, String prefix, LinkedList<String> list) {
        if (root == null) {
            return;
        }
        if (root.val != null) {
            list.addLast((String) root.val);
        }

//        for (char c = 0; c < R; c++) {
//            collect(root.next[c], prefix + c, list);
//        }

        for (Node x : root.children.values()) {
            collect(x, prefix, list);
        }
    }

    public LinkedList<String> keysWithPrefix(String prefix) {
        LinkedList<String> list = new LinkedList<>();
        Node currRoot = get(root, prefix, 0);
        collect(currRoot, prefix, list);
        return list;
    }

}
