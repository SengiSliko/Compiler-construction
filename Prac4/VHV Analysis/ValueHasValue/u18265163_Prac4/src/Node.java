import java.util.ArrayList;
import java.util.List;

public class Node {
    private String type;
    private String content;
    private int id;
    public List<Node> children;

    public Node(int id, String type, String content) {
        this.type = type;
        this.content = content;
        this.id = id;
        children = new ArrayList<Node>();
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

}
