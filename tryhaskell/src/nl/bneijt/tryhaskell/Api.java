package nl.bneijt.tryhaskell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class Api {

    public JSONObject send(String haskellCode) {
        HttpPost requestObject;
        try {
            requestObject = Api.requestObjectFor(haskellCode);
        } catch (UnsupportedEncodingException e1) {
            Log.d("JSON", "Unsupported encoding");
            return null;
        }

        String possiblyJson = connect(requestObject);
        if (possiblyJson != null) {
            Log.d("JSON", possiblyJson);
            try {
                return (JSONObject) new JSONTokener(possiblyJson).nextValue();
            } catch (JSONException e) {
                Log.d(Api.class.getName(), "Error parsing result", e);
                return null;
            }
        }
        return null;
    }

    public static HttpPost requestObjectFor(String haskellCode) throws UnsupportedEncodingException {

        HttpPost post = new HttpPost("http://tryhaskell.org/eval");
        List<BasicNameValuePair> data = Arrays.asList(new BasicNameValuePair("exp", haskellCode));

        post.setEntity(new UrlEncodedFormEntity(data));
        return post;
    }

    public static String connect(HttpPost httpPost) {
        String result = null;
        HttpClient httpclient = new DefaultHttpClient();
        // Execute the request
        try {
            HttpResponse response = httpclient.execute(httpPost);

            // Examine the response status
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
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
