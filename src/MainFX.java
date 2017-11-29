import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * vigor-bruteforcer
 * Created by Dane Naebers on 28-11-2017.
 */
public class MainFX extends Application {

    private ExecutorService pool;
    private ArrayList<RequestTask> reqTasks;
    private Properties config;
    private ArrayList<String> passwords;
    private Stage stage;
    private Map<String, Result> mainResults;

    private int maxChars;
    private int passwordsDone;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("vigor-bruteforcer.fxml"));
        Scene scene = new Scene(root, 788, 475);
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

    public void printResult(Map<String, Result> result) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                passwordsDone += 1;
                lblPasswordsDone.setText(String.valueOf(passwordsDone));
                lblPasswordsLeft.setText(String.valueOf(passwords.size() - passwordsDone));
                for (String s : result.keySet()) {
                    guiList.getItems().add(s + " = " + result.get(s));
                    if (result.get(s).equals(Result.TRUE)) {
                        pool.shutdown();
                    }
                }
            }
        });
    }

    private void dictionaryAttack() throws IOException {
        pool = Executors.newFixedThreadPool(8);
        lblPasswordsLeft.setText(String.valueOf(passwords.size()));
        int listSize = passwords.size() / 8;
        ArrayList<List<String>> lists = new ArrayList<>();
        for (int i = 0; i < passwords.size(); i += listSize) {
            lists.add(passwords.subList(i, Math.min(i + listSize, passwords.size())));
        }

        for (int i = 0; i < 8; i++) {
            pool.submit(new RequestTask(this, config.getProperty("reqUrl"), config.getProperty("username"), lists.get(i)) {
                @Override
                protected Map<String, Result> call() throws Exception {
                    makeRequest();
                    return this.result;
                }
            });
        }

        pool.execute(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        guiList.getItems().add("Bruteforce completed!");
                    }
                });
            }
        });
    }

    private void bruteforceAttack() {
        String[] TARGET_ALL_UC_LC_AND_NUMBERS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "a", "B", "b", "C", "c", "D", "d", "E", "e", "F", "f", "G", "g", "H", "h", "I", "i", "J", "j", "K", "k", "L", "l", "M", "m", "N", "n", "O", "o", "P", "p", "Q", "q", "R", "r", "S", "s", "T", "t", "U", "u", "V", "v", "W", "w", "X", "x", "Y", "y", "Z", "z"};
        for (int i = 0; i < TARGET_ALL_UC_LC_AND_NUMBERS.length; i++) {
            String password = TARGET_ALL_UC_LC_AND_NUMBERS[i];
            passwords.add(password);
            createPassword(TARGET_ALL_UC_LC_AND_NUMBERS, password);
        }
        System.out.println("test");
    }

    private void createPassword(String[] TARGET_ALL_UC_LC_AND_NUMBERS, String password) {
        if (password.length() < maxChars) {
            for (int e = 0; e < TARGET_ALL_UC_LC_AND_NUMBERS.length; e++) {
                String p = password + TARGET_ALL_UC_LC_AND_NUMBERS[e];
                passwords.add(p);
                createPassword(TARGET_ALL_UC_LC_AND_NUMBERS, p);
            }
        }
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
    private Label lblPasswordsLeft;
    @FXML
    private Label lblPasswordsDone;

    @FXML
    private ChoiceBox cbMode;

    @FXML
    private ChoiceBox cbCharPositions;


    @FXML
    public void initialize() {
        cbMode.getItems().removeAll(cbMode.getItems());
        cbCharPositions.getItems().removeAll(cbCharPositions.getItems());

        cbMode.getItems().addAll("Use Password File", "Use Bruteforce");
        cbCharPositions.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    }

    @FXML
    protected void startBruteforce(ActionEvent event) throws IOException {
        guiList.getItems().clear();
        lblPasswordsLeft.setText("0");
        lblPasswordsDone.setText("0");
        if (cbMode.getValue().equals("Use Password File")) {
            loadPasswordFile(new File(config.getProperty("passFilePath")));
            dictionaryAttack();
        } else if (cbMode.getValue().equals("Use Bruteforce")) {
            passwords = new ArrayList<>();
            maxChars = Integer.valueOf(cbCharPositions.getValue().toString());
            bruteforceAttack();
        }
    }

    @FXML
    protected void stopBruteforce(ActionEvent event) {
        if (pool != null) {
            pool.shutdownNow();
            guiList.getItems().addAll("Attack stopped");
        }
    }

    @FXML
    protected void loadPasswords(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(stage);
        txtPwdFilePath.setText(file.toString());
        config.setProperty("passFilePath", file.toString());
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
