import javafx.concurrent.Task;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public abstract class RequestTask extends Task<RequestResult> implements Observer {
    private RequestClient reqClient;
    public RequestResult result;

    public RequestTask(String path) throws IOException {
        reqClient = new RequestClient(path);
        reqClient.addObserver(this::update);
    }

    public void makeRequest(String username, String password) throws IOException {
        synchronized(this) {
            reqClient.makeRequest(username, password);
        }
    }

    public void update(Observable o, Object arg) {
        result = (RequestResult) arg;
        System.out.println(result.password + "   :    " + result.correct);
        if(result.correct) {

        }
    }
}
