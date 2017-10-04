package za.co.myconcepts.instaclone.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import za.co.myconcepts.instaclone.model.Message;

public class MessageJSONParser {
    public static List<Message> parseFeed(String content){

        try {
            JSONArray ar = new JSONArray(content);
            List<Message> messageList = new ArrayList<>();

            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);
                Message message = new Message();

                //Parsing ID's
                message.setMessage(obj.getString("message"));

                messageList.add(message);

            }

            return messageList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
