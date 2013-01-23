package org.deephacks.westty.example.login;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.deephacks.westty.jpa.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Consumes({ APPLICATION_JSON })
@Produces({ "application/json" })
@Path("auth-service")
@Singleton
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @Inject
    private EntityManager em;

    @Transactional
    @GET
    @Path("login")
    public String login(@FormParam("username") String username,
            @FormParam("password") String password) {
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

    @Provider
    public class AuthServiceExceptionHandler implements ExceptionMapper<Exception> {

        public Response toResponse(Exception ex) {
            return Response.serverError().status(400).entity(ex.getMessage()).build();
        }
    }

}
