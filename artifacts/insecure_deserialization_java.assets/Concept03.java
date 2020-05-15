public class Concept03 {
    public static void main(String[] args) {
        try {
            // understanding the basic concept of transformer
            String s = "asinha";
            System.out.println(s);

            // objective is to reverse the string via transformer.transform() method
            MyReverse r_transformer = new MyReverse();  // MyReverse cls implements transformer
            System.out.println(r_transformer.transform(s));  // calling the transform method
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
