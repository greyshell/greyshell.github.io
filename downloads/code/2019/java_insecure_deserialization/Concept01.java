public class Concept01 {
    public static void main(String[] args) {
        try {
            // understand how to execute command in Java
            // exec() is not a static method so we can't call directly without creating an object
            // due to that Runtime.exec("/usr/bin/gnome-calculator"); // => will not NOT work

            // Runtime.getRuntime().exec("/usr/bin/gnome-calculator"); // compact form

            Runtime r = Runtime.getRuntime();  // step 1: need to get the runtime object
            r.exec("/usr/bin/gnome-calculator");  // step 2: call the exec() method with the runtime object

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
