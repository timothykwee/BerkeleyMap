import java.util.Comparator;

/**
 * Created by parisl on 4/14/17.
 */
class QuadTree {
    static class TreeNode implements Comparable<TreeNode> {
        Tile value;
        TreeNode left;
        TreeNode midLeft;
        TreeNode midRight;
        TreeNode right;

        TreeNode(Tile value, TreeNode left, TreeNode midLeft, TreeNode midRight, TreeNode right) {
            this.value = value;
            this.left = left;
            this.midLeft = midLeft;
            this.midRight = midRight;
            this.right = right;
        }

        boolean isLeaf() {
            return left == null && midLeft == null && midRight == null
                    && right == null;
        }

        @Override
        public int compareTo(TreeNode t1) {
            if (value.getUpperLeftLat() < t1.value.getUpperLeftLat()) {
                return -1;
            } else if (value.getUpperLeftLat() == t1.value.getUpperLeftLat()) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TreeNode treeNode = (TreeNode) o;
            return value.getUpperLeftLat() == treeNode.value.getUpperLeftLat();
        }

        @Override
        public int hashCode() {
            int result = (int) (value.getUpperLeftLat() * 10);

            return result;
        }
    }

    private TreeNode root;
    private int size;

    QuadTree(Tile val) {
        root = new TreeNode(val, null, null, null, null);
        size = 1;
    }

    void fillQuadTree(Tile val) {
        fillQuadTree(val, root);
    }

    private void fillQuadTree(Tile val, TreeNode currRoot) {
        if (val.getName().length() == 7) {
            return;
        }

        Tile val1 = new Tile(val.getName() + "1", val.getUpperLeftLon(), val.getUpperLeftLat(),
                val.getMiddleLon(), val.getMiddleLat());
        Tile val2 = new Tile(val.getName() + "2", val.getMiddleLon(), val.getUpperLeftLat(),
                val.getLowerRightLon(), val.getMiddleLat());
        Tile val3 = new Tile(val.getName() + "3", val.getUpperLeftLon(), val.getMiddleLat(),
                val.getMiddleLon(), val.getLowerRightLat());
        Tile val4 = new Tile(val.getName() + "4", val.getMiddleLon(), val.getMiddleLat(),
                val.getLowerRightLon(), val.getLowerRightLat());

        addLeft(val1, currRoot);
        addMidLeft(val2, currRoot);
        addMidRight(val3, currRoot);
        addRight(val4, currRoot);

        fillQuadTree(val1, currRoot.left);
        fillQuadTree(val2, currRoot.midLeft);
        fillQuadTree(val3, currRoot.midRight);
        fillQuadTree(val4, currRoot.right);
    }

    private void addLeft(Tile val, TreeNode root) {
        root.left = new TreeNode(val, null, null, null, null);
        size += 1;

    }

    private void addMidLeft(Tile val, TreeNode root) {
        root.midLeft = new TreeNode(val, null, null, null, null);
        size += 1;
    }


    private void addMidRight(Tile val, TreeNode root) {
        root.midRight = new TreeNode(val, null, null, null, null);
        size += 1;
    }

    private void addRight(Tile val, TreeNode root) {
        root.right = new TreeNode(val, null, null, null, null);
        size += 1;
    }

    TreeNode getRoot() {
        return root;
    }

    int getSize() {
        return size;
    }


}
