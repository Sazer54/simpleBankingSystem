/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package SImple_Banking_System_Unbound;

import java.util.Scanner;

public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        args = new String[]{"-fileName", "card.s3db"};
        Controller controller = new Controller();
        controller.work(args);
    }
}