public class User implements java.io.Serializable {
  private String name;
  private final String nickname;  // final => NO impact, WILL be serialized
  static int uid;  // static => NOT serialize this field, int default value = 0
  private transient String password; // transient => NOT serialize this field, String default value = null
  private double weight;
  private Car c;

  public User(String name, String nickname, int uid, String password, double weight) {
    this.name = name;
    this.nickname = nickname;
    this.uid = uid;
    this.password = password;
    this.weight = weight;
  }

  // use this constructor if we want to wrap another object
  public User(String name, String nickname, int uid, String password, double weight, Car c) {
    this.name = name;
    this.nickname = nickname;
    this.uid = uid;
    this.password = password;
    this.weight = weight;
    this.c = c;
  }

  // this method will be called while printing the object
  public String toString() {
    return name + " : " + nickname + " : " + uid + " : " + password + " : " + weight + " : " + c;
  }
}
