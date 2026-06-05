import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestJavaImageDL {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://covers.openlibrary.org/b/isbn/9780132350884-M.jpg");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // Optional
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                System.out.println("Bytes available: " + is.available());
            } else {
                InputStream is = connection.getErrorStream();
                if (is != null) System.out.println("Error stream bytes: " + is.available());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
