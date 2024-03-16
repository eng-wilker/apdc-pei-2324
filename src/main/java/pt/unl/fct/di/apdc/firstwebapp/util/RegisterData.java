package pt.unl.fct.di.apdc.firstwebapp.util;

import com.google.cloud.*;

public class RegisterData {
    public String username;
    public String password;
    public String email;
    public String name;
    public String confirmation;
    public  Timestamp timestamp;

    public RegisterData() {
    }

    public RegisterData(String username, String password, String email, String name, String confirmation, Timestamp timestamp) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.confirmation = confirmation;
        this.timestamp = timestamp;
    }
    public boolean validRegistration() {
        return password.equals(confirmation)&&username!=null&&password!=null&&email!=null&&name!=null;
    }
}
