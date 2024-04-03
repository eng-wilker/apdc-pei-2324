package pt.unl.fct.di.apdc.firstwebapp.utils;

public class LoginData {

    public String username;
    public String password;

    public LoginData() {

    }

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean isValid() {
        return username != null && password != null;
    }

    public boolean isPasswordValid() {
        return password != null && password.length() >= 8 && password.matches(".*\\d.*")
                && password.matches(".*[a-z].*") && password.matches(".*[A-Z].*");
    }
}
