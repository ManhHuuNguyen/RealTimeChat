import java.util.HashMap;

public class Tester {

    public static void main (String[] args){
        String a = "124'";
        System.out.println(filter(a));
    }

    public static String filter(String str){
        for (int i=0; i<str.length(); i++){
            if ((str.charAt(i)=='"') || (str.charAt(i)=='\'')){
                str = str.substring(0, i) + "\\" + str.substring(i);
                i+=1;
            }
        }
        return str;
    }
}
