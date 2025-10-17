package it.uniroma2.pjdm.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	private String email;
	private String name;
	
	public User(String email, String name) {
		this.email = email;
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	//JSON
    public String toJSONString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);  
            jsonObject.put("name", name);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    
    public static User fromJSON(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);

        String name = jsonObject.optString("name", null);
        String email = jsonObject.optString("email", null);

        return new User(email, name);
    }

	

}
