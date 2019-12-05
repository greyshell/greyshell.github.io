import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

public class MyReverse implements Transformer {
    public Object transform(Object o) {
        String d = (String) o;
        System.out.println("executing transform method !!: " + d);
        new Exception().printStackTrace();
        return StringUtils.reverse(d);
    }
}
