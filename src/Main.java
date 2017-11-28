import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 25-11-17
 * vigor-bruteforcer created by Dane Naebers
 */
public class Main {
    private static ExecutorService pool;
    private static RequestTask reqTask;
    private static Map<String, Result> results;
    private static Properties config;
    private static ArrayList<String> passwords;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException {

        pool = Executors.newFixedThreadPool(8);
        scanner = new Scanner(System.in);
        initialize();
        bruteForce();
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
        int listSize = passwords.size() / 8;
        ArrayList<List<String>> lists = new ArrayList<>();
        for (int i = 0; i < passwords.size(); i += listSize) {
            lists.add(passwords.subList(i, Math.min(i + listSize, passwords.size())));
        }
/*
        for (int i = 0; i < 1; i++) {
            reqTask = new RequestTask(config.getProperty("reqUrl"), config.getProperty("username"), lists.get(i)) {
                @Override
                protected Map<String, Result> call() throws Exception {
                    makeRequest();
                    return this.results;
                }
            };
            pool.submit(reqTask);
        }

        pool.execute(() -> {
            try {
                results = new HashMap<>();
                results.putAll(reqTask.get());
                for(String s : results.keySet())
                {
                    System.out.println(s + "     :     " + results.get(s));
                    if (results.get(s) == Result.TRUE) {
                        System.out.println("Password found:  " + s);
                        scanner.nextLine();
                        System.exit(0);
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        */
    }
}
