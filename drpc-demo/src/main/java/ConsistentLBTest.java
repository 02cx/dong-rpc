
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConsistentLBTest {
    static Random ran = new Random();

    /** key's count */
    private static final Integer EXE_TIMES = 10_0000;

    private static final Integer NODE_COUNT = 3;

    private static final Integer VIRTUAL_NODE_COUNT = 150;

    public static void main(String[] args) {
        ConsistentLBTest test = new ConsistentLBTest();

        Map<NodeX, Integer> nodeRecord = new HashMap<>();

        List<NodeX> allNodes = test.getNodes(NODE_COUNT);
        ConsistentLB locator = new ConsistentLB(allNodes,VIRTUAL_NODE_COUNT);

        List<String> allKeys = test.getAllStrings();
        for (String key : allKeys) {
            NodeX node = locator.getPrimary(key);

            Integer times = nodeRecord.get(node);
            if (times == null) {
                nodeRecord.put(node, 1);
            } else {
                nodeRecord.put(node, times + 1);
            }
        }

        System.out.println("Nodes count : " + NODE_COUNT + ", Keys count : " + EXE_TIMES + ", Normal percent : " + (float) 100 / NODE_COUNT + "%");
        System.out.println("-------------------- boundary  ----------------------");
        for (Map.Entry<NodeX, Integer> entry : nodeRecord.entrySet()) {
            System.out.println("Node name :" + entry.getKey() + " - Times : " + entry.getValue() + " - Percent : " + (float)entry.getValue() / EXE_TIMES * 100 + "%");
        }

    }


    private List<NodeX> getNodes(int nodeCount) {
        List<NodeX> nodes = new ArrayList<NodeX>();

        for (int k = 1; k <= nodeCount; k++) {
            NodeX node = new NodeX("node" + k);
            nodes.add(node);
        }

        return nodes;
    }

    /**
     *	All the keys
     */
    private List<String> getAllStrings() {
        List<String> allStrings = new ArrayList<String>(EXE_TIMES);

        for (int i = 0; i < EXE_TIMES; i++) {
            allStrings.add(generateRandomString(ran.nextInt(50)));
        }

        return allStrings;
    }

    /**
     * To generate the random string by the random algorithm
     * <br>
     * The char between 32 and 127 is normal char
     *
     * @param length
     * @return
     */
    private String generateRandomString(int length) {
        StringBuffer sb = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            sb.append((char) (ran.nextInt(95) + 32));
        }

        return sb.toString();
    }
}
