package pet.clever.drs;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

public class BadRequestException extends WebApplicationException {
  public BadRequestException(String message) {
    super(Response.status(400).entity(message).build());
  }
}
