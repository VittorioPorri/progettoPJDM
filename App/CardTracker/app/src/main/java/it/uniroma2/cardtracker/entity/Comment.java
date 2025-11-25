package it.uniroma2.cardtracker.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
    private int idComment;
    private String text;
    private String email;
    private int idDeck;


    public Comment(int idComment, String text, String email, int idDeck) {
        this.idComment = idComment;
        this.text = text;
        this.email = email;
        this.idDeck = idDeck;
    }

    // Getter e Setter
    public int getIdComment() {
        return idComment;
    }

    public void setIdComment(int idComment) {
        this.idComment = idComment;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIdDeck() {
        return idDeck;
    }

    public void setIdDeck(int idDeck) {
        this.idDeck = idDeck;
    }

    // JSON
    public String toJSONString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("idComment", idComment);
            jsonObject.put("text", text);
            jsonObject.put("email", email);
            jsonObject.put("idDeck", idDeck);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public static Comment fromJSON(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);

        int idComment = jsonObject.optInt("idComment", -1);
        String text = jsonObject.optString("text", null);
        String email = jsonObject.optString("email", null);
        int idDeck = jsonObject.optInt("idDeck", -1);

        return new Comment(idComment, text, email, idDeck);
    }
}
