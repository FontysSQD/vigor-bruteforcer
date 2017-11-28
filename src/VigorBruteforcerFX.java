import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * vigor-bruteforcer
 * Created by Dane Naebers on 28-11-2017.
 */
public class VigorBruteforcerFX extends Application {

    private ExecutorService pool;
    private ArrayList<RequestTask> reqTasks;
    private Properties config;
    private ArrayList<String> passwords;
    private Stage stage;
    private Map<String, Result> mainResults;

    @Override
    public void start(Stage primaryStage) throws Exception {
        pool = Executors.newFixedThreadPool(8);
        reqTasks = new ArrayList<>();
        this.stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("vigor-bruteforcer.fxml"));
        Scene scene = new Scene(root, 600, 444);
        stage.setResizable(false);
        stage.setTitle("vigor-bruteforcer");
        stage.setScene(scene);
        stage.show();
    }

    private void loadPasswordFile(File path) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {
            passwords = new ArrayList<>();
            while (dis.available() > 0) {
                passwords.add(dis.readLine());
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Password File");
            alert.setHeaderText("Password File Loaded");
            alert.showAndWait();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printResult(Map<String, Result> results) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiList.getItems().removeAll();
                for(String s : results.keySet()) {
                    guiList.getItems().add(s + "   =   " + results.get(s));
                }
            }
        });
    }


    @FXML
    private ListView guiList;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPwdFilePath;
    @FXML
    private TextField txtRequestUrl;

    @FXML
    protected void startBruteforce(ActionEvent event) throws IOException {
        /*
        int listSize = passwords.size() / 8;
        ArrayList<List<String>> lists = new ArrayList<>();
        for (int i = 0; i < passwords.size(); i += listSize) {
            lists.add(passwords.subList(i, Math.min(i + listSize, passwords.size())));
        }

        for(int i = 0; i < 8; i++) {
            reqTasks.add(new RequestTask(this, config.getProperty("reqUrl"), config.getProperty("username"), lists.get(i)) {
                @Override
                protected Map<String, Result> call() throws Exception {
                    makeRequest();
                    return this.results;
                }
            });
        }

        for(RequestTask task : reqTasks) {
            pool.submit(task);
        }

        pool.execute(new Runnable() {
            @Override
            public void run() {
                mainResults = new HashMap<>();
                for(RequestTask task : reqTasks) {
                    try {
                        mainResults.putAll(task.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                printResult(mainResults);
            }
        });
        */
        Map<String, Result> test = new HashMap<>();
        test.put("password", Result.FALSE);
        printResult(test);
    }

    @FXML
    protected void loadPasswords(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(stage);
        txtPwdFilePath.setText(file.toString());
        loadPasswordFile(file);
    }

    @FXML
    protected void loadConfig(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Config File", "*.vrbr")
        );
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try (FileInputStream is = new FileInputStream(file)) {
                config = new Properties();
                config.load(is);
                txtRequestUrl.setText(config.getProperty("reqUrl"));
                txtUsername.setText(config.getProperty("username"));
                txtPwdFilePath.setText(config.getProperty("passFilePath"));
                loadPasswordFile(new File(config.getProperty("passFilePath")));
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Load Config");
                alert.setHeaderText("Config file successfully loaded");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void saveConfig(ActionEvent event) {
        String reqUrl = txtRequestUrl.getText();
        String username = txtUsername.getText();
        String passFilePath = txtUsername.getText();
        if (!reqUrl.isEmpty() && !username.isEmpty() && !passFilePath.isEmpty()) {
            config = new Properties();
            config.setProperty("reqUrl", reqUrl);
            config.setProperty("username", username);
            config.setProperty("passFilePath", passFilePath);

            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Config File", "*.vrbr")
            );
            File file = chooser.showSaveDialog(stage);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    config.store(fos, "vigor-bruteforcer configurations");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Save Config");
                    alert.setHeaderText("Config file successfully Saved");
                    alert.showAndWait();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Config");
            alert.setHeaderText("Saving config failed");
            alert.setContentText("Please fill in all fields");
            alert.showAndWait();
        }

    }
}
