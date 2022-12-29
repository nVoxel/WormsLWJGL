import application.Application;

import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Do you want to be the host? (y/n)");
        
        if (scanner.next().equals("y")){
            new Application().run();
        }
        else {
            System.out.println("Enter the server IP address:");
            System.out.println("e.g. 127.0.0.1");
            
            String serverIP = scanner.next();
            
            new Application().run(serverIP);
        }
    }
}
