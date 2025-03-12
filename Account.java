import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

class Credentials {
    private final String email;
    private String password;
    public Credentials(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public boolean setPassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(password)) {
            password = newPassword;
            return true;
        }
        return false;
    }
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
    public String toString() {
        return email + " " + password;
    }
}

class Account {
    static class Information {
        final Credentials creds;
        final SortedSet<String> favoriteGames;
        final String name;
        final String country;
        private Information(InformationBuilder builder) {
            creds = builder.creds;
            favoriteGames = builder.favoriteGames;
            name = builder.name;
            country = builder.country;
        }
        public String toString() {
            return creds + "\n" + favoriteGames + "\n" + name + "\n" + country;
        }
        static class InformationBuilder {
            private final Credentials creds;
            private final SortedSet<String> favoriteGames;
            private String name;
            private String country;
            public InformationBuilder(String email, String password) {
                creds = new Credentials(email, password);
                favoriteGames = new TreeSet<>();
            }
            public InformationBuilder setName(String name) {
                this.name = name;
                return this;
            }
            public InformationBuilder setCountry(String country) {
                this.country = country;
                return this;
            }
            public InformationBuilder addFavoriteGame(String game) {
                favoriteGames.add(game);
                return this;
            }
            public Information build() {
                return new Information(this);
            }
        }
    }
    Information info;
    ArrayList<Character> characters;
    int gamesPlayed;
    public Account(ArrayList<Character> characters, int gamesPlayed, Information info) {
        this.info = info;
        this.characters = characters;
        this.gamesPlayed = gamesPlayed;
    }
    public String getEmail() {
        return info.creds.getEmail();
    }
    public boolean setPassword(String oldPassword, String newPassword) {
        return info.creds.setPassword(oldPassword, newPassword);
    }
    public boolean checkPassword(String password) {
        return info.creds.checkPassword(password);
    }
    public String toString() {
        return info + "\n" + characters + "\n" + gamesPlayed + "\n";
    }
}