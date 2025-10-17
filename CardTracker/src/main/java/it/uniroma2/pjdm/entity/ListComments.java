package it.uniroma2.pjdm.entity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListComments extends ArrayList<Comment> {
    private static final long serialVersionUID = 987236451092837465L;

    // JSON
    public String toJSONString() {
        try {
            JSONArray jsonArray = new JSONArray();

            for (Comment comment : this) {
                if (comment != null) {
                    jsonArray.put(new JSONObject(comment.toJSONString()));
                }
            }

            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public static ListComments fromJSON(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        ListComments commentList = new ListComments();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject commentObj = jsonArray.getJSONObject(i);
            try {
                Comment comment = Comment.fromJSON(commentObj.toString());
                commentList.add(comment);
            } catch (JSONException e) {
                continue;
            }
        }

        return commentList;
    }
}
