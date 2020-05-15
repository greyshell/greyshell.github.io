import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SafeDeserializeRCE {
    public static void main(String[] args) {
        try {
            System.setProperty(
                    "org.apache.commons.collections.enableUnsafeSerialization",
                    "true");

            // file name of the deserialized object
            String fname = "../Serialization/bad_serialized_object.ser";
            FileInputStream fin = new FileInputStream(fname);

            // whitelist all classes for RCE
            Set rce_whitelist = new HashSet<String>(Arrays.asList("org.apache.commons.collections.bag.HashBag",
                    "org.apache.commons.collections.keyvalue.TiedMapEntry",
                    "org.apache.commons.collections.map.LazyMap",
                    "org.apache.commons.collections.functors.ChainedTransformer",
                    "[Lorg.apache.commons.collections.Transformer;",
                    "org.apache.commons.collections.functors.ConstantTransformer",
                    "java.lang.Runtime",
                    "org.apache.commons.collections.functors.InvokerTransformer",
                    "[Ljava.lang.Object;",
                    "[Ljava.lang.Class;",
                    "java.lang.String",
                    "java.lang.Object",
                    "[Ljava.lang.String;",
                    "java.lang.Integer",
                    "java.lang.Number",
                    "java.util.HashMap"));

            ObjectInputStream oin = new SafeObjectInputStream(fin, rce_whitelist);

            // expecting a User type obj, actual deserialization happens here
            User a = (User) oin.readObject();
            oin.close();
            fin.close();
            System.out.println("\nThe object was read from " + fname + ":");
            System.out.println(a);
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
