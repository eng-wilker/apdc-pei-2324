package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResurce {
    private static final Logger LOG = Logger.getLogger(LoginResurce.class.getName());
    public LoginResurce() {
    } // nothing to be done here

@POST
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public Response doLogin(LoginData data) {
    LOG.fine("Attempt to login user: " + data.username);
    return Response.ok().build();
}
}