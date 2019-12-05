import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;

import java.lang.reflect.Method;

public class Concept06 {
    public static void main(String[] args) {
        try {
            // combining the concept of reflection API, ConstantTransformer and InvokerTransformer

            // equivalent code: Class<Runtime> cls = Runtime.class;
            Transformer t0 = new ConstantTransformer(Runtime.class);
            String key = "any_key"; // we trigger with any Object by passing to the transform() method
            Class r0 = (Class) t0.transform(key); // transform() returns a reflection Object of java.lang.Runtime class

            System.out.println("object type: " + r0.getClass() + " | but basically it points to " + r0.getName());

            // equivalent code: Method m = cls.getMethod("getRuntime");
            Transformer t1 = new InvokerTransformer(
                    "getMethod",  // method name
                    new Class[]{String.class, Class[].class},
                    new Object[]{"getRuntime", new Class[0]}
            );
           Method m = (Method) t1.transform(r0);
           System.out.println(m.getClass());

            // equivalent code: Runtime r = (Runtime) m.invoke(null,null);
            Transformer t2 = new InvokerTransformer(
                    "invoke",  // method name
                    new Class[]{Object.class, Object[].class},
                    new Object[]{null, new Object[0]}
            );
            Runtime r = (Runtime) t2.transform(m);
            System.out.println(r.getClass());

            // equivalent code: r.exec("/usr/bin/gnome-calculator");
            Transformer t3 = new InvokerTransformer(
                    "exec",  // method name
                    new Class[]{String.class},
                    new String[]{"/usr/bin/gnome-calculator"}
            );
            t3.transform(r);

            // most compact format:
            t3.transform(t2.transform(t1.transform(t0.transform(key))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
