package pet.clever.drs;

import java.util.*;
import java.util.logging.*;

import javax.ws.rs.*;

import com.google.appengine.api.datastore.*;
import com.google.common.net.*;
import com.sun.jersey.spi.container.*;

import pet.clever.drs.datastore.*;

/**
 * 
 * client - per user routes (use cleverpet user auth token)
 * public - public routes (no auth required), e.g., lwa callback, sns callback, ir callback, etc..
 * service - per service routes, i.e., admin stuff
 *
 */
public class AuthFilter implements ContainerRequestFilter {
  
  private final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    String path = request.getRequestUri().getPath();

    log.info(Log.str("auth", path));

    // client requests (e.g., ios app, android app)
    if (path.startsWith("/api/client")) {
      String authorization = request.getHeaderValue("Authorization");
      if (authorization != null) {
        StringTokenizer st = new StringTokenizer(authorization);
        if (st.hasMoreTokens()) {
          st.nextToken();
          if (st.hasMoreTokens()) {
            String auth_token = st.nextToken();
            for (User user : new DsHelper(ds).query(User.class).filter(Query.FilterOperator.EQUAL.of("auth_tokens", auth_token)).list()) {
              request.getRequestHeaders().add("X-cleverpet-user-id", user.user_ID);
              return request;
            }
          }
        }
      }
    }

    if (path.startsWith("/api/public"))
      return request;
    
    // service requests, e.g., from "gcp pubsub" and "amazon sns"
    if (path.startsWith("/api/service")) {

      // dev server
      for (String host : request.getRequestHeaders().get("Host")) {
        if ("localhost".equals(HostAndPort.fromString(host).getHostText()))
          return request;
      }

      // ###TODO verify request from app engine
      return request;
    }

    throw new WebApplicationException(401);
  }

  private final Logger log = Logger.getLogger(this.getClass().getName());

}
