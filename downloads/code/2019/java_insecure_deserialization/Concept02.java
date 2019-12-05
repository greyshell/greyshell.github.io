import java.lang.reflect.Method;
import java.lang.Runtime;

public class Concept02 {
    public static void main(String[] args) {
        try {
            // understanding the concept of Java reflection API
            Class<Runtime> cls = Runtime.class;
            Method m = cls.getMethod("getRuntime"); // examine a method of that Class dynamically

            // equivalent code: Runtime r = (Runtime) Runtime.getRuntime();
            Runtime r = (Runtime) m.invoke(null,null);  // executing the method to get a Runtime object
            // arg 1 => static class function -> null is passed instead of object
            // arg 2 => null means no argument

            r.exec("/usr/bin/gnome-calculator");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
