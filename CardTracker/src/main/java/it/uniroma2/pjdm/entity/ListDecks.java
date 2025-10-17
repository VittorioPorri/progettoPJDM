package it.uniroma2.pjdm.entity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListDecks extends ArrayList<Deck> {
    private static final long serialVersionUID = 3201948576129384750L;

    // JSON
    public String toJSONString() {
        try {
            JSONArray jsonArray = new JSONArray();

            for (Deck deck : this) {
                if (deck != null) {
                    jsonArray.put(new JSONObject(deck.toJSONString()));
                }
            }

            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public static ListDecks fromJSON(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        ListDecks deckList = new ListDecks();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject deckObj = jsonArray.getJSONObject(i);
            try {
                Deck deck = Deck.fromJSON(deckObj.toString()); 
                deckList.add(deck);
            } catch (JSONException e) {
                continue;
            }
        }

        return deckList;
    }
}
