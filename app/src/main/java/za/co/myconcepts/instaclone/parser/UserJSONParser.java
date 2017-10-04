package za.co.myconcepts.instaclone.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import za.co.myconcepts.instaclone.model.User;

public class UserJSONParser {
    public static List<User> parseFeed(String content){

        try {
            JSONArray ar = new JSONArray(content);
            List<User> userList = new ArrayList<>();

            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);
                User user = new User();

                //Parsing ID's
                user.setUsername(obj.getString("username"));
                user.setUserID(obj.getString("user_id"));
                userList.add(user);

            }

            return userList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
