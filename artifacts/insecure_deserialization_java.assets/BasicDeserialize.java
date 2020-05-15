import java.io.*;

public class BasicDeserialize {
    public static void main(String[] args) {
        try {
            // by default: enableUnsafeSerialization is set to FALSE, enabling it for Demo
            System.setProperty(
                    "org.apache.commons.collections.enableUnsafeSerialization",
                    "true");

            String fname = "../Serialization/serialized_object.ser";  // good object

            FileInputStream fin = new FileInputStream(fname);
            ObjectInputStream oin = new ObjectInputStream(fin);

            User u = (User) oin.readObject();  // actual deserialization process happens here
            oin.close();
            fin.close();
            System.out.println("The object was read from " + fname + ":");
            System.out.println(u);
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
