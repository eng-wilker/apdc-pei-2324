package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResurce {
    private static final Logger LOG = Logger.getLogger(LoginResurce.class.getName());
    public LoginResurce() {
    } // nothing to be done here
}
