package pet.clever.drs;

import java.io.*;
import java.util.logging.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import javax.ws.rs.ext.*;

import com.google.common.base.*;

@Provider
public class RootExceptionMapper implements ExceptionMapper<Exception> {
  public Response toResponse(Exception exception) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter, true);
    if (exception instanceof WebApplicationException) {
      Response response = ((WebApplicationException) exception).getResponse();
      // client side error or server side error?
      if (response.getStatus()<500) {
        // client-side error
        Status status = Status.fromStatusCode(response.getStatus());
        if (status == null)
          writer.println(response.getStatus());
        else
          writer.println(String.format("%s %s", status.getStatusCode(), status.getReasonPhrase()));
        return Response.fromResponse(response).type("text/plain").entity(stringWriter.toString()).build();
        //###TODO ALSO OUTPUT RESPONSE ENTITY IF EXISTS (I.E., SUPPORT BADREQUESTEXCEPTION MESSAGE)
      }
    }
    // always print 5xx errors
    log.severe(Log.str(exception));
    writer.println(Throwables.getStackTraceAsString(exception));
    return Response.serverError().type("text/plain").entity(stringWriter.toString()).build();
  }
  
  private final Logger log = Logger.getLogger(this.getClass().getName());
}