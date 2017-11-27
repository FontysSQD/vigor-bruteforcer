import javafx.concurrent.Task;
import java.io.IOException;
import java.util.*;

/**
 * 25-11-17
 * vigor-bruteforcer created by Dane Naebers
 */
public abstract class RequestTask extends Task<Map<String, Result>> implements Observer {
    private RequestClient reqClient;
    private String url;
    private String username;
    private List<String> passwordList;
    public Map<String, Result> results;

    public RequestTask(String url, String username, List<String> passwordList) throws IOException {
        results = new HashMap<>();
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
        results.putAll((Map) o);
    }
}
