package it.uniroma2.cardtracker.entity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListCards extends ArrayList<Card>{
	private static final long serialVersionUID = 7510923847561293845L;
	
	//JSON
    public String toJSONString() {
        try {
            JSONArray jsonArray = new JSONArray();

            for (Card card : this) {
                if (card != null) {
                    jsonArray.put(new JSONObject(card.toJSONString()));
                }
            }

            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public static ListCards fromJSON(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        ListCards cardList = new ListCards();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject cardObj = jsonArray.getJSONObject(i);
            try {
                Card card = Card.fromJSON(cardObj.toString());
                cardList.add(card);
            } catch (JSONException e) {
                continue;
            }
        }

        return cardList;
    }

}
