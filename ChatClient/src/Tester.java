
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;

public class Tester {

    public static void main (String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter something:");
        String a = "";
        while (scanner.hasNext()){
            a += scanner.nextLine();
        }

        System.out.println(a);
    }


}
