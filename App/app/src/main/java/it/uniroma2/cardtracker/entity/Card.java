package it.uniroma2.cardtracker.entity;

import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class Card {
    private int idCard;
    private String name;
    private String image;
    private int quantita;

    public Card(int idCard, String name, byte[] image, int quantita) {
        this.idCard = idCard;
        this.name = name;
        this.image = getImageAsBase64(image);
        this.quantita = quantita;
    }

    public Card(int idCard, String name, String image, int quantita) {
        this.idCard = idCard;
        this.name = name;
        this.image = image;
        this.quantita = quantita;
    }

    // Getter and Setter
    public Card(int idCard, String name, byte[] image) {
        this(idCard, name, image, 1);
    }

    public Card(int idCard, String name, String image) {
        this(idCard, name, image, 1);
    }

    private String getImageAsBase64(byte[] image) {
        if (image == null) return null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return java.util.Base64.getEncoder().encodeToString(image);
        } else {
            return android.util.Base64.encodeToString(image, android.util.Base64.NO_WRAP);
        }
    }


    public int getIdCard() {
        return idCard;
    }

    public void setIdCard(int idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = getImageAsBase64(image);
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    // JSON
    public String toJSONString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("idCard", idCard);
            jsonObject.put("name", name);
            jsonObject.put("quantita", quantita);

            if (image != null && !image.isEmpty()) {
                jsonObject.put("image", image);
            }

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public static Card fromJSON(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);

        int idCard = jsonObject.optInt("idCard", -1);
        String name = jsonObject.optString("name", null);
        String image = jsonObject.optString("image", null);
        int quantita = jsonObject.optInt("quantita", 1);

        return new Card(idCard, name, image, quantita);
    }
}
