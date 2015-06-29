package com.example.dzm.internet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dzm on 6/2/2015.
 */
public class HttpTask extends AsyncTask<URL, Integer, Boolean> {
    public HttpTask(Context appContext,ArrayList< ArrayMap<String, String>> aList){
        theContext = appContext;
        courseList = aList;
        uniIdsKey = new StringBuilder(CourseInfo.UNIVERSITIES).append(CourseInfo.Universities.UniversityEleFields.ID).toString();
        uniSNsKey = new StringBuilder(CourseInfo.UNIVERSITIES).append(CourseInfo.Universities.UniversityEleFields.SHORTNAME).toString();
    }

    @Override
    protected Boolean doInBackground(URL... params) {
        uniInfo = new HashMap<Integer, String>();
        listString = new ArrayList<String[]>();
        try {
            HttpURLConnection aConnection = (HttpURLConnection)params[0].openConnection();
            aConnection.setConnectTimeout(10000);
            aConnection.setReadTimeout(10000);
            aConnection.connect();
            JsonReader jReader = getJsonReader(aConnection);
            readTheData(jReader);
            aConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        formFinalData();
        if(courseList.size()>0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean gotData){
        Intent intent = new Intent(Filter.COURSEFILTER);
        intent.putExtra(theContext.getResources().getString(R.string.got_data), gotData);
        LocalBroadcastManager.getInstance(theContext).sendBroadcast(intent);
    }

    private void formFinalData(){
        for(ArrayMap<String, String> aMap:courseList){
            aMap.put(uniSNsKey, uniIdsToShortNames(aMap.get(uniIdsKey), uniInfo));
        }

    }

    private JsonReader getJsonReader(HttpURLConnection aConnection) throws IOException {
        InputStream aStream = aConnection.getInputStream();
        InputStreamReader reader = new InputStreamReader(aStream);
        BufferedReader aBufferedReader = new BufferedReader(reader);
        return new JsonReader(aBufferedReader);
    }

    private void readTheData(JsonReader jReader) throws IOException {
        jReader.beginObject();
        while(jReader.hasNext()){
            String ele = jReader.nextName();
            if (ele.equals(CourseInfo.Courses.ELEMENTS)){
                dealEle(jReader);
            }else if(ele.equals(CourseInfo.Courses.LINKED)){
                dealLinked(jReader);
            }else {
                jReader.skipValue();
            }
        }
        jReader.endObject();
        jReader.close();

    }

    private void dealLinked(JsonReader jReader) throws IOException{
        jReader.beginObject();
        while(jReader.hasNext()){
            if (jReader.nextName().equals(CourseInfo.UNIVERSITIES)){
                jReader.beginArray();
                while (jReader.hasNext()){
                    dealUniItem(jReader);
                }
                jReader.endArray();
            }else {
                jReader.skipValue();
            }
        }
        jReader.endObject();
    }

    private void dealUniItem(JsonReader jReader) throws IOException {
        int id = -1;
        String shortName = new String();
        jReader.beginObject();                              //university
        while (jReader.hasNext()){
            String uniItem = jReader.nextName();
            if(uniItem.equals(CourseInfo.Universities.UniversityEleFields.ID)){
                id = jReader.nextInt();
            }else if(uniItem.equals(CourseInfo.Universities.UniversityEleFields.SHORTNAME)){
                shortName = jReader.nextString();
            }else{
                jReader.skipValue();
            }
        }
        jReader.endObject();                                     //university
        uniInfo.put(id, shortName);
    }

    private void dealEle(JsonReader jReader) throws IOException {
        jReader.beginArray();
        while (jReader.hasNext()){
            dealCourseItem(jReader);
        }
        jReader.endArray();

    }

    private void dealCourseItem(JsonReader jReader) throws IOException{

        ArrayMap<String, String> aMap = new ArrayMap<String, String>();

        jReader.beginObject();                            //course

        while (jReader.hasNext()){
            String key = jReader.nextName();
            switch(key){
                case CourseInfo.Courses.CoursesEleFields.SHORTNAME:
                case CourseInfo.Courses.CoursesEleFields.WORKLOAD:
                case CourseInfo.Courses.CoursesEleFields.SMALLICON:
                case CourseInfo.Courses.CoursesEleFields.NAME:
                case CourseInfo.Courses.CoursesEleFields.LANGUAGE:
                case CourseInfo.Courses.CoursesEleFields.LARGEICON:
                case CourseInfo.Courses.CoursesEleFields.BRIEF:
                    aMap.put(key, jReader.nextString());
                    break;
                case CourseInfo.Courses.CoursesEleFields.LINKS:
                    jReader.beginObject();                       //link obj
                    while(jReader.hasNext()){
                        String linkedItem = jReader.nextName();
                        if(linkedItem.equals(CourseInfo.UNIVERSITIES)){
                            String uniIds = dealRelativeUniIds(jReader);
                            aMap.put(uniIdsKey, uniIds);
                        }else {
                            jReader.skipValue();
                        }
                    }
                    jReader.endObject();                            //link obj
                    break;
                default:
                    jReader.skipValue();
                    break;
            }

        }
        jReader.endObject();                                    //course

        courseList.add(aMap);
    }

    private String dealRelativeUniIds(JsonReader jReader)throws IOException{

        StringBuilder uniBuilder = new StringBuilder();
        jReader.beginArray();                //the uni related a course
        while (jReader.hasNext()){
            int universityId = jReader.nextInt();
            uniBuilder.append(universityId);
            uniBuilder.append(',');
        }
        jReader.endArray();                     //the uni related a course
        if(uniBuilder.length()!=0){
            uniBuilder.deleteCharAt(uniBuilder.length()-1);
            return uniBuilder.toString();
        }else{
            return "";
        }
    }

    private String uniIdsToShortNames(String ids, HashMap<Integer, String> info){
        StringBuilder shortNames = new StringBuilder();
        String[] uniIds = ids.split(",");
        for (int i=0; i<uniIds.length; i++){
            int id = new Integer(Integer.parseInt(uniIds[i]));
            String shortN = info.get(id);
            shortNames.append(',');
            shortNames.append(shortN);
        }
        shortNames.deleteCharAt(0);
        return shortNames.toString();
    }

    private Context theContext;
    private ArrayList<ArrayMap<String, String>> courseList;
    private HashMap<Integer, String> uniInfo;
    private ArrayList<String[]> listString;
    private String uniIdsKey;
    private String uniSNsKey;
}
