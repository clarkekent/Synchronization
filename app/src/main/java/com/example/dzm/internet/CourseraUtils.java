package com.example.dzm.internet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dzm on 6/8/2015.
 */
public class CourseraUtils {
    public static JSONArray toJArray(String wholeJSONString) throws JSONException {
        JSONObject courseraJSONObject = new JSONObject(wholeJSONString);
        return courseraJSONObject.getJSONArray(BaseFields.ELEMENTS);
    }

    public static JSONObject getItem(int id, JSONArray eles) throws JSONException {
        JSONObject item;
        for(int i=0; i<eles.length(); i++){
            item = eles.getJSONObject(i);
            if(id == item.getInt(CourseInfo.BaseEleFields.ID)){
                return item;
            }
        }
        return null;
    }
}
