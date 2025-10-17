
package it.uniroma2.pjdm.entity;
import org.json.JSONException;
import org.json.JSONObject;

public class Deck {
    private int idDeck;      
    private String name;
    private String formato;
    private String email;

    public Deck(int idDeck, String name, String formato, String email) {
        this.idDeck = idDeck;
        this.name = name;
        this.formato = formato;
        this.email = email;
    }

    // Getter and Setter
    public int getIdDeck() {
        return idDeck;
    }

    public void setIdDeck(int idDeck) {
        this.idDeck = idDeck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // JSON
    public String toJSONString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("idDeck", idDeck);  
            jsonObject.put("name", name);
            jsonObject.put("formato", formato);
            jsonObject.put("email", email);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    
    public static Deck fromJSON(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);

        int idDeck = jsonObject.optInt("idDeck", -1);
        String name = jsonObject.optString("name", null);
        String formato = jsonObject.optString("formato", null);
        String email = jsonObject.optString("email", null);

        return new Deck(idDeck, name, formato, email);
    }

}
