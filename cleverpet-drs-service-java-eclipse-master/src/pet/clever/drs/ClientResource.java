package pet.clever.drs;

import java.util.logging.*;

import javax.ws.rs.*;

import com.google.common.base.*;

import pet.clever.drs.datastore.*;

class GetAmazonDrsAccessTokenResponse {
  public final String access_token;
  public GetAmazonDrsAccessTokenResponse(String access_token) {
    this.access_token = access_token;
  }
}

@Path("/client/drs")
public class ClientResource {

  // cleverpet user_ID
  @HeaderParam("X-cleverpet-user-id")
  private String user_ID;

  private final DrsService drs_service = DrsListener.drsService;

  /**
   * get registration
   * 
   * @return
   */
  @GET
  @Path("/registrations/{device_ID}")
  public DrsRegistration getRegistration(@PathParam("device_ID") long device_ID) {
    DrsRegistration registration = drs_service.getRegistration(device_ID);
    if (registration!=null)
      return registration;
    throw new WebApplicationException(404);
  }
  
  /**
   * get registration access token
   * 
   * @param device_ID
   * @return
   * @throws Exception
   */
  @GET
  @Path("/registrations/{device_ID}/access_token")
  public GetAmazonDrsAccessTokenResponse getAccessToken(@PathParam("device_ID") long device_ID) throws Exception {
    String access_token = drs_service.getAccessToken(device_ID);
    if (access_token!=null)
      return new GetAmazonDrsAccessTokenResponse(access_token);
    throw new WebApplicationException(404);
  }

  /**
   * setRefreshToken
   *
   * @param device_ID
   * @param refresh_token
   */
  @POST
  @Path("/registrations/{device_ID}/refresh_token/{client_id}")
  public void setRefreshToken(@PathParam("device_ID") long device_ID, @PathParam("client_id") String client_id, String refresh_token) {
    log.info(Log.str("set_refresh_token", device_ID, client_id));
    if (device_ID==0)
      throw new BadRequestException("device_ID");
    if (!has(client_id))
      throw new BadRequestException("client_id");
    if (!has(refresh_token))
      throw new BadRequestException("refresh_token");
    drs_service.setRefreshToken(device_ID, client_id, refresh_token);
  }

  static boolean has(String s) {
    return Strings.nullToEmpty(s).length()>0;
  }
  
  private final Logger log = Logger.getLogger(this.getClass().getName());
}
