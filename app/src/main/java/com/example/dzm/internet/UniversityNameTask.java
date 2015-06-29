package com.example.dzm.internet;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dzm on 6/13/2015.
 */
public class UniversityNameTask extends AsyncTask<String, Integer, String> {
    public UniversityNameTask(Context context, TextView view){
        appContext = context;
        prefix = "https://api.coursera.org/api/catalog.v1/universities?fields=name&q=query&id=";
        nameView = view;
    }

    @Override
    protected String doInBackground(String... params) {
        String universityIds = params[0];
        String[] uIds = parseUniversityIds(universityIds);
        StringBuilder uNames = new StringBuilder();
        for(String uId:uIds){
            String query = new StringBuilder(prefix).append(uId).toString();
            try {
                HttpsURLConnection universityConn = (HttpsURLConnection)new URL(query).openConnection();
                universityConn.connect();
                String queryResult = new BufferedReader(new InputStreamReader(universityConn.getInputStream())).readLine();
                try {
                    JSONObject university = new JSONObject(queryResult);
                    String universityName = university.getJSONArray(CourseInfo.Universities.ELEMENTS).getJSONObject(0).getString(CourseInfo.Universities.UniversityEleFields.NAME);

                    uNames.append(',').append(universityName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                universityConn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(uNames.length()==0){
            return null;
        }else{
            uNames.deleteCharAt(0);
            return uNames.toString();
        }

    }

    @Override
    protected void onPostExecute(String s) {
        nameView.setText(s);
    }

    private String[] parseUniversityIds(String ids){
        return ids.split(",");
    }

    private String prefix;
    private Context appContext;
    private TextView nameView;
}
