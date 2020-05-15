import java.io.*;
import java.util.Base64;

public class BasicDosDeserialize {
    public static void main(String[] args) {
        // argument: base64 serialized obj as command line
        byte[] userBytes = new byte[0];
        if (args[0].startsWith("rO0AB")) {
            userBytes = Base64.getDecoder().decode(args[0].getBytes());
        } else {
            System.out.println("\nCaught an exception... Invalid object?\n");
            System.exit(0);
        }
        ByteArrayInputStream bIn = new ByteArrayInputStream(userBytes);
        try {
            ObjectInputStream oIn = new ObjectInputStream(bIn);
            System.out.println("here");
            User asinha = (User) oIn.readObject();  // actual deserialization

            oIn.close();
            bIn.close();
            System.out.println(asinha);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println();
            e.printStackTrace();
            System.out.println("\nCaught an exception... Invalid object?\n");
        }

    }
}
