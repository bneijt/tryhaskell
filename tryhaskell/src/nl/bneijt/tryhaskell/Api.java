package nl.bneijt.tryhaskell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class Api {

    public JSONObject send(String haskellCode) {
        String urlEncodedHaskell = URLEncoder.encode(haskellCode);
        String possiblyJson = connect("http://tryhaskell.org/haskell.json?method=eval&pad=handleJSON&expr="
                + urlEncodedHaskell + "&random=" + Math.random());
        if (possiblyJson != null) {
            Log.d("JSON", possiblyJson);
            try {
                if (possiblyJson.length() < "handleJSON(".length()) {
                    return null;
                }
                String jsonBody = possiblyJson.substring(
                        "handleJSON(".length(), possiblyJson.length() - 1);
                return (JSONObject) new JSONTokener(jsonBody).nextValue();
            } catch (JSONException e) {
                Log.d(Api.class.getName(), "Error parsing result", e);
                return null;
            }
        }
        return null;
    }

    public static String connect(String url) {
        String result = null;
        HttpClient httpclient = new DefaultHttpClient();

        // Prepare a request object

        HttpGet httpget = new HttpGet(url);
        // Execute the request
        try {
            HttpResponse response = httpclient.execute(httpget);

            // Examine the response status
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                result = convertStreamToString(instream);
                instream.close();
            }

        } catch (Exception e) {
        }
        return result;
    }

    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
