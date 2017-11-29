package com.d4n3.vigorbruteforcer;

import javafx.concurrent.Task;
import java.io.IOException;
import java.util.*;

/**
 * 25-11-17
 * vigor-bruteforcer created by Dane Naebers
 */
public abstract class RequestTask extends Task<Map<String, Result>> implements Observer {
    private MainFX application;
    private RequestClient reqClient;
    private String url;
    private String username;
    private List<String> passwordList;
    public Map<String, Result> result;

    public RequestTask(MainFX application, String url, String username, List<String> passwordList) throws IOException {
        this.application = application;
        this.url = url;
        this.username = username;
        this.passwordList = passwordList;
    }

    public void makeRequest() throws IOException {
        synchronized(this) {
            for(String s : passwordList) {
                reqClient = new RequestClient(url);
                reqClient.addObserver(this);
                reqClient.makeRequest(username, s);
            }
        }
    }

    public void update(Observable o, Object arg) {
        this.result = new HashMap<>();
        result.putAll((Map) arg);
        application.printResult(result);
    }
}
