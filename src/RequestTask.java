import javafx.concurrent.Task;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public abstract class RequestTask extends Task<RequestResult> implements Observer {
    private RequestClient reqClient;
    private String username;
    private List<String> passwordList;
    public RequestResult result;

    public RequestTask(String path, String username, List<String> passwordList) throws IOException {
        this.username = username;
        this.passwordList = passwordList;
        reqClient = new RequestClient(path);
        reqClient.addObserver(this::update);
    }

    public void makeRequest() throws IOException {
        synchronized(this) {
            for(String s : passwordList) {
                reqClient.makeRequest(username, s);
            }
        }
    }

    public void update(Observable o, Object arg) {
        result = (RequestResult) arg;
    }
}
