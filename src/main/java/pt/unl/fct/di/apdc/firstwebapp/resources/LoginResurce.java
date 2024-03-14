package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResurce {
    private final Gson g = new Gson();
    private static final Logger LOG = Logger.getLogger(LoginResurce.class.getName());

    public LoginResurce() {
    } // nothing to be done here

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginData data) {
        LOG.fine("Login attempt by user: " + data.username);
        if (data.username.equals("jleitao") && data.password.equals("password")) {
            AuthToken at = new AuthToken(data.username);
            return Response.ok(g.toJson(at)).build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
    }

    @GET
    @Path("/{username}")
    public Response checkUsernameAvailable(@PathParam("username") String username) {
        if (username.equals("jleitao")) {
            return Response.ok().entity(g.toJson(false)).build();
        } else {
            return Response.ok().entity(g.toJson(true)).build();
        }
    }
}