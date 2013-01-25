package org.deephacks.westty.example.auth;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.deephacks.westty.jpa.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Consumes({ APPLICATION_JSON })
@Produces({ "application/json" })
@Path("/jaxrs/auth-service")
@Singleton
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @Inject
    private EntityManager em;

    @Transactional
    @POST
    @Path("cookieLogin")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response cookieLogin(@FormParam("username") String username,
            @FormParam("password") String password) {
        String sessionId = sessionLogin(username, password);
        NewCookie cookie = new NewCookie("sessionId", sessionId);
        return Response.seeOther(uri("/auth/index.html")).cookie(cookie).build();
    }

    private static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @POST
    @Path("sessionLogin")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public String sessionLogin(@FormParam("username") String username,
            @FormParam("password") String password) {
        return login(username, password);
    }

    private String login(String username, String password) {
        if (username == null) {
            throw new RuntimeException("Username must be provided");
        }
        if (password == null) {
            throw new RuntimeException("Password must be provided");
        }

        UserEntity user = em.find(UserEntity.class, username);
        if (user == null) {
            log.warn("User does not exist {}", username);
            throw new RuntimeException("User does not exist");
        }
        if (user.getPassword().equals(password)) {
            SessionEntity session = new SessionEntity();
            em.persist(session);
            return session.getId();
        } else {
            log.warn("Wrong password {}", password);
            throw new RuntimeException("Wrong password");
        }
    }

    @Transactional
    @POST
    @Path("createUser")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public void createUser(@FormParam("username") String username,
            @FormParam("password") String password) {
        if (username == null) {
            throw new RuntimeException("Username must be provided");
        }
        if (password == null) {
            throw new RuntimeException("Password must be provided");
        }

        em.persist(new UserEntity(username, password));
    }

    @Transactional
    @POST
    @Path("cookieProtected")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public String cookieProtected(@CookieParam("sessionId") String sessionId) {
        validate(sessionId);
        return "success";
    }

    @Transactional
    @POST
    @Path("sessionProtected")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public String sessionProtected(@CookieParam("sessionId") String sessionId) {
        validate(sessionId);
        return "success";
    }

    private void validate(String sessionId) {
        if (sessionId == null) {
            throw new RuntimeException("No session found");
        }
        SessionEntity session = em.find(SessionEntity.class, sessionId);
        if (session == null) {
            throw new RuntimeException("No session found");
        }
    }

    @Provider
    public class AuthServiceExceptionHandler implements ExceptionMapper<Exception> {

        public Response toResponse(Exception ex) {
            log.warn("Exception occured", ex);
            return Response.serverError().status(400).entity(ex.getMessage()).build();
        }
    }
}
