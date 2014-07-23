package nl.bneijt.tryhaskell;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class TryhaskellActivity extends Activity implements OnKeyListener {
    private Api api;
    private AutoCompleteTextView inputLine;
    private TextView outputConsole;
    private ArrayAdapter<String> suggestions;

    private class TryHaskellTask extends AsyncTask<String, Void, JSONObject> {

        private String haskellLine;

        @Override
        protected JSONObject doInBackground(String... code) {
            haskellLine = code[0];
            return api.send(haskellLine);
        }

        protected void outputColored(String text, int color) {
            outputConsole.append(text);
            Spannable str = (Spannable) outputConsole.getText();
            str.setSpan(new ForegroundColorSpan(color),
                    str.length() - text.length(), str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    if(result.has("success")) {
                        result = result.getJSONObject("success");
                        if (result.has("expr")) {
                            outputConsole.append(result.getString("expr"));
                            outputConsole.append("\n");
                        }

                        if (result.has("value")) {
                            outputConsole.append(result.getString("expr"));
                        }

                        if (result.has("type")) {
                            outputColored(" :: " + result.getString("type"),
                                    Color.CYAN);
                        }

                        if (result.has("stdout")) {
                            JSONArray stdout = result.getJSONArray("stdout");
                            for(int i = 0; i < stdout.length(); ++i) {
                                outputConsole.append("\n");
                                outputConsole.append(stdout.getString(i));
                            }
                        }
                        outputConsole.append("\n");
                    } else {
                        if (result.has("error")) {
                            outputColored(result.getString("error"), Color.RED);
                            outputConsole.append("\n");
                            return;
                        }
                    }
                } else {
                    outputConsole.append("Connection error\n");
                }
            } catch (JSONException e) {
                outputConsole.append("Error\n");
            }
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        api = new Api();
        ArrayList<String> oldSuggestions = null;
        if(savedInstanceState != null ) {
            oldSuggestions = savedInstanceState.getStringArrayList("suggestions");
        }
        if(oldSuggestions == null) {
            oldSuggestions = new ArrayList<String>();
        }
        suggestions = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, oldSuggestions);

        outputConsole = (TextView) findViewById(R.id.outputConsole);
        inputLine = (AutoCompleteTextView) findViewById(R.id.inputLine);

        inputLine.setAdapter(suggestions);
        inputLine.setOnKeyListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Save suggestions to state
        ArrayList<String> suggestionsList = new ArrayList<String>();
        for (int index = 0; index < suggestions.getCount(); index++) {
            suggestionsList.add(suggestions.getItem(index));
        }
        outState.putStringArrayList("suggestions", suggestionsList);
    };
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_UP) {
            String haskellToTry = inputLine.getText().toString();
            new TryHaskellTask().execute(haskellToTry);
            if(suggestions.getPosition(haskellToTry) < 0) {
                if(suggestions.getCount() > 20) {
                    suggestions.remove(suggestions.getItem(0));
                }
                suggestions.add(haskellToTry);
            }
            inputLine.setText("");
            return true;
        }
        return false;
    }
}