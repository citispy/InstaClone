package za.co.myconcepts.instaclone.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import za.co.myconcepts.instaclone.model.Image;

public class ImageJSONParser {
    public static List<Image> parseFeed(String content){

        try {
            JSONArray ar = new JSONArray(content);
            List<Image> imageList = new ArrayList<>();

            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);
                Image image = new Image();

                //Parsing ID's
                image.setUsername(obj.getString("username"));
                image.setImage_url(obj.getString("image_url"));
                image.setImage_id(obj.getString("image_id"));
                image.setUser_id(obj.getString("user_id"));
                image.setLatitude(obj.getString("latitude"));
                image.setLongitude(obj.getString("longitude"));
                image.setCityName(obj.getString("city_name"));

                imageList.add(image);

            }

            return imageList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
