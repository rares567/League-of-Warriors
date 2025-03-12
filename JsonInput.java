import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class JsonInput {
    public static ArrayList<Account> deserializeAccounts() {
        String accountPath = "./src/accounts.json";
        try {
            String content = new String((Files.readAllBytes(Paths.get(accountPath))));
            JSONObject obj = new JSONObject(content);
            JSONArray accountsArray = (JSONArray) obj.get("accounts");

            ArrayList<Account> accounts = new ArrayList<>();
            for (int i=0; i < accountsArray.length(); i++) {
                JSONObject accountJson = (JSONObject) accountsArray.get(i);
                Account.Information.InformationBuilder builder = null;

                // Credentials
                try {
                    JSONObject credentialsJson = (JSONObject) accountJson.get("credentials");
                    String email = (String) credentialsJson.get("email");
                    String password = (String) credentialsJson.get("password");

                    builder = new Account.Information.InformationBuilder(email, password);
                } catch (JSONException e) {
                    System.out.println("! This account doesn't have all credentials !");
                }

                // name, country, games_number
                builder.setName((String) accountJson.get("name"));
                builder.setCountry((String) accountJson.get("country"));
                int gamesNumber = Integer.parseInt((String)accountJson.get("maps_completed"));

                // Favorite games
                try {
                    JSONArray games = (JSONArray) accountJson.get("favorite_games");
                    for (int j = 0; j < games.length(); j++) {
                        builder.addFavoriteGame((String) games.get(j));
                    }
                } catch (JSONException e) {
                    System.out.println("! This account doesn't have favorite games !");
                }

                // Characters
                ArrayList<Character> characters = new ArrayList<>();
                try {
                    JSONArray charactersListJson = (JSONArray) accountJson.get("characters");
                    for (int j = 0; j < charactersListJson.length(); j++) {
                        JSONObject charJson = (JSONObject) charactersListJson.get(j);
                        String cname = (String) charJson.get("name");
                        String profession = (String) charJson.get("profession");
                        String level = (String) charJson.get("level");
                        int lvl = Integer.parseInt(level);
                        Integer experience = (Integer) charJson.get("experience");

                        characters.add(CharacterFactory.create(profession, cname, experience, lvl));
                    }
                } catch (JSONException e) {
                    System.out.println("! This account doesn't have characters !");
                }

                Account.Information information = builder.build();
                Account account = new Account(characters, gamesNumber, information);
                accounts.add(account);
            }
            return accounts;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}