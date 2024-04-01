package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.OrderBy;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.checkerframework.checker.units.qual.A;

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
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response doLogin(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
        LOG.fine("Login attempt by user: " + data.username);
        Key userKey = datastore.newKeyFactory().newKey(data.username);
        Key ctrsKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username)).setKind("UserStats")
                .newKey("counters");
        Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
                .setKind("UserLogs").newKey());
        Transaction txn = datastore.newTransaction();
        try {
            Entity user = txn.get(userKey);
            if (user == null) {
                LOG.warning("Failed login attempt for username: " + data.username);
                return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
            }
            Entity stats = txn.get(ctrsKey);
            if (stats == null) {
                stats = Entity.newBuilder(ctrsKey).set("user_stats_logins", 0L).set("user_stats_failed", 0L)
                        .set("user_first_login", Timestamp.now()).set("user_last_login", Timestamp.now()).build();
            }
            String hashedPWD = user.getString("user_pwd");
            if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
                Entity log = Entity.newBuilder(logKey).set("user_login_ip", request.getRemoteAddr())
                        .set("user_login_host", request.getRemoteHost())
                        .set("user_login_latlon",
                                StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
                                        .setExcludeFromIndexes(true).build())
                        .set("user_login_country",
                                headers.getHeaderString("X-AppEngine-Country"))
                        .set("user_login_date", Timestamp.now()).set("user_login_city",
                                headers.getHeaderString("X-AppEngine-City"))
                        .build();

                Entity ustats = Entity.newBuilder(ctrsKey)
                        .set("user_stats_logins", 1L + stats.getLong("user_stats_logins"))
                        .set("user_stats_failed", 0L)
                        .set("user_first_login", stats.getTimestamp("user_first_login"))
                        .set("user_last_login", Timestamp.now()).build();

                txn.put(user, ustats, log);
                txn.commit();
                AuthToken at = new AuthToken(data.username);
                LOG.info("User " + data.username + " logged in.");
                return Response.ok(g.toJson(at)).build();
            } else {
                Entity ustats = Entity.newBuilder(ctrsKey).set("user_stats_logins", stats.getLong("user_stats_logins"))
                        .set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
                        .set("user_first_login", stats.getTimestamp("user_first_login"))
                        .set("user_last_login", stats.getTimestamp("user_last_login"))
                        .set("user_last_attempt", Timestamp.now()).build();
                txn.put(ustats);
                txn.commit();
                LOG.warning("Failed login attempt for username: " + data.username);
                return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
            }
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            txn.rollback();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error performing login.").build();

        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error performing login.").build();
            }
        }

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
    @Path("/getLast24Login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getLast24Login(LoginData data) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        if (!user.getString("password").equals(DigestUtils.sha256Hex(data.password))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect username or password.").build();
        }
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("username")
                .setFilter(
                        StructuredQuery.PropertyFilter.eq("username", data.username))
                .build();
        QueryResults<Entity> logs = datastore.run(query);
        int count = 0;
        while (logs.hasNext()) {
            logs.next();
            count++;
        }
        return Response.ok().entity(g.toJson(count)).build();

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