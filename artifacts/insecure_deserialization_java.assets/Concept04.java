import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ConstantTransformer;

import java.lang.reflect.Method;
import java.util.HashSet;

public class Concept04 {
    public static void main(String[] args) {
        try {
            // concept of ConstantTransformer
            Transformer t = new ConstantTransformer(Runtime.class);  // it's transform() method always returns a reflection of java.lang.Runtime class

            HashSet h = new HashSet();
            Class cls = (Class) t.transform(h);
            System.out.println(cls);

            Dashboard d = new Dashboard(23.9, 8000);
            Class cls02 = (Class) t.transform(d);
            System.out.println(cls02);  // both cls and cls02 is same

            // we can perform command execution using reflection concept
            Method m = cls.getMethod("getRuntime"); // examine method dynamically
            // similar to Runtime r = (Runtime) Runtime.getRuntime();
            Runtime r = (Runtime) m.invoke(null,null);  // executing the method to get a Runtime object
            // arg 1 => static class function -> null is passed instead of object
            // arg 2 => null means no argument
            r.exec("/usr/bin/gnome-calculator");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
