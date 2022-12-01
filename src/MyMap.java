import java.util.HashMap;

public class MyMap extends HashMap<String, String[]> {
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for(String key : this.keySet()) {
            builder.append(key).append("=[");
            for(String v : this.get(key)) builder.append(v).append(",");
            builder.deleteCharAt(builder.length()-1);
            builder.append("], ");
        }
        builder.delete(builder.length()-2,builder.length());
        builder.append("}");
        return builder.toString();
    }
}
