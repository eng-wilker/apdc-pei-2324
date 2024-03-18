package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
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
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResurce {
    private final Gson g = new Gson();
    private static final Logger LOG = Logger.getLogger(LoginResurce.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public LoginResurce() {
    } // nothing to be done here

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginData data) {
        LOG.fine("Login attempt by user: " + data.username);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        if (!user.getString("password").equals(DigestUtils.sha256Hex(data.password))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        AuthToken at = new AuthToken(data.username);
        LOG.info("User " + data.username + " logged in.");
        return Response.ok(g.toJson(at)).build();
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

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegister(LoginData data) {
        LOG.fine("Register attempt by user: " + data.username);
        if (data.username.equals("jleitao")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Username already exists.").build();
        }
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = Entity.newBuilder(userKey).set("password", DigestUtils.sha256Hex(data.password)).build();
        datastore.put(user);
        return Response.ok().entity("User registered.").build();
    }

    @POST
    @Path("/register2")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doResgister2(RegisterData data) {
        if (!data.validRegistration()) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid registration data.").build();
        }
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        if (datastore.get(userKey) != null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Username already exists.").build();
        }
        Entity user = Entity.newBuilder(userKey).set("password", DigestUtils.sha256Hex(data.password))
                .set("email", data.email).set("name", data.name).set("timestamp", Timestamp.now()).build();
        datastore.put(user);
        return Response.ok().entity("User registered.").build();
    }

    @POST
    @Path("/login2")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin2(LoginData data) {
        LOG.fine("Login attempt by user: " + data.username);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        if (!user.getString("password").equals(DigestUtils.sha256Hex(data.password))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        AuthToken at = new AuthToken(data.username);
        LOG.info("User " + data.username + " logged in.");
        return Response.ok(g.toJson(at)).build();
    }

    @POST
    @Path("/login/user/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getLoginTimes(LoginData data) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        if (!user.getString("password").equals(DigestUtils.sha256Hex(data.password))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        return Response.ok().entity(g.toJson(user.getTimestamp("timestamp"))).build();
    }
}