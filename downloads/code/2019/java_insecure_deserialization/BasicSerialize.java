import java.io.*;

public class BasicSerialize {
    public static void main(String[] args) {
        try {
            User asinha = new User("bob", "shell", 2, "splunk", 80.5);  // using 1st constructor
            System.out.println(asinha); // equivalent to asinha.toString()

            String file_name = "serialized_object.ser";
            FileOutputStream fout = new FileOutputStream(file_name);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(asinha);
            oout.close();
            fout.close();
            System.out.println("User object is written to disk as " + file_name);

            System.out.println();

            // case 2: trying to serialize a Object whoes type is not User. For example: String
            String s = new String("Hello");
            System.out.println(s);
            String file_name2 = "serialized_object02.ser";
            FileOutputStream fout2 = new FileOutputStream(file_name2);
            ObjectOutputStream oout2 = new ObjectOutputStream(fout2);
            oout2.writeObject(s);
            oout2.close();
            fout2.close();
            System.out.println("String object is written to disk as " + file_name2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
