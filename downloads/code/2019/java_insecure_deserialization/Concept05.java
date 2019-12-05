import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.InvokerTransformer;

public class Concept05 {
    public static void main(String[] args) {
        try {
            // concept of InvokerTransformer
            Dashboard d = new Dashboard(23.9, 8000);
            System.out.println(d.toString());

            Transformer t = new InvokerTransformer(
                    "toString",  // method name
                    null,  // toString() parameter types
                    null  // toString() argument
            );
            // invoke the toString() method of Dashboard object via InvokerTransformer's transform() method
            String out = (String) t.transform(d);  // on `transform`, calls that toString() for the Dashboard object
            System.out.println(out);  // same as d.toString()

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
