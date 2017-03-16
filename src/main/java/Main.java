import env.Environment;

/**
 * Created by bri on 01-Mar-17.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Configure environment");
        Environment.configure();
        System.out.println("Start environment");
        Environment.start();

    }


}
