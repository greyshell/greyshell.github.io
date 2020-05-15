import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SafeDeserialize {
    public static void main(String[] args) {
        try {
            // fix 1: a blacklist based approach where the InvokerTransformer is blocked
            // however this can be bypassed by discovering a new gadget chains
            // in current Java, by default enableUnsafeSerialization is set to 'false'
            System.setProperty(
                    "org.apache.commons.collections.enableUnsafeSerialization",
                    "true");
            // file name of the deserialized object
            String fname = "../Serialization/bad_serialized_object.ser";
            FileInputStream fin = new FileInputStream(fname);
            // fix 2: whitelist based approach, resolve the class from the serialized object and
            Set whitelist = new HashSet<String>(Arrays.asList("User"));
            ObjectInputStream oin = new SafeObjectInputStream(fin, whitelist);

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
