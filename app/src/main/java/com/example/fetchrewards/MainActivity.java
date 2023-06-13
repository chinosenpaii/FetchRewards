package com.example.fetchrewards;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private HashMap<String, List<String>> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        itemList = new HashMap<>();

        FetchDataTask fetchDataTask = new FetchDataTask();
        fetchDataTask.execute("https://fetch-hiring.s3.amazonaws.com/hiring.json");
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    String name = item.optString("name");
                    String listId = item.optString("listId");
                    Log.d("MainActivity", "Item name: " + name);


                    // Filter out items with blank or null name
                    if (name != null && !name.isEmpty()) {
                        if (!itemList.containsKey(listId)) {
                            itemList.put(listId, new ArrayList<>());
                        }
                        itemList.get(listId).add(name);
                    }
                }

                // Sort the results by listId and name
                List<String> sortedListIds = new ArrayList<>(itemList.keySet());
                Collections.sort(sortedListIds);
                for (String listId : sortedListIds) {
                    List<String> names = itemList.get(listId);
                    Collections.sort(names);

                    // Display the items grouped by listId
                    adapter.add("List ID: " + listId);
                    for (String name : names) {
                        adapter.add("- " + name);
                    }
                    adapter.add(""); // Add an empty line between list groups
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error parsing JSON data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}