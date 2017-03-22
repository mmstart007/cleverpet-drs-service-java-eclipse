package pet.clever.drs;

import java.io.*;
import java.util.logging.*;

import com.google.common.base.*;
import com.google.common.io.*;
import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.representation.*;

/**
 * DrsSdk
 */
public class DrsSdk {

  private final Client httpClient;
//  private final String client_id;
  private final String client_secret;

  /**
   * ctor
   *
   * @param httpClient
   */
  public DrsSdk(Client httpClient, String client_id, String client_secret) {
    this.httpClient = httpClient;
//    this.client_id = client_id;
    this.client_secret = client_secret;
  }
  
  private String str(InputStream in) {
    try {
      return CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * access_token
   *
   * @param client_id
   * @param refresh_token
   * @return
   * @throws Exception
   */
  public String access_token(String client_id, String refresh_token) {
    Form accessTokenRequest = new Form();
    accessTokenRequest.add("grant_type", "refresh_token");
    accessTokenRequest.add("refresh_token", refresh_token);
    accessTokenRequest.add("client_id", client_id);
    // ###TODO MOVE THIS TO DATASTORE CONFIG
    accessTokenRequest.add("client_secret", "245b17766253e3b9935cf0f64b533ddb0a0eb805ba2e53761fb4837dd860ee5a");
    ClientResponse clientResponse = httpClient.resource("https://api.amazon.com/auth/o2/token").post(ClientResponse.class, accessTokenRequest);
    if (clientResponse.getStatus() < 400)
      return clientResponse.getEntity(AccessTokenResponse.class).access_token;
    throw new RuntimeException(str(clientResponse.getEntityInputStream()));
  }

  /**
   * getSubscriptionInfo
   *
   * @param refresh_token
   * @return
   */
  public JsonElement getSubscriptionInfo(String client_id, String refresh_token) {
    ClientResponse clientResponse = httpClient
        .resource("https://dash-replenishment-service-na.amazon.com/subscriptionInfo")
        .header("Authorization", String.format("Bearer %s", access_token(client_id, refresh_token)))
        .header("x-amzn-type-version", "com.amazon.dash.replenishment.DrsSubscriptionInfoInput@1.0")
        .header("x-amzn-accept-type", "com.amazon.dash.replenishment.DrsSubscriptionInfoResult@1.0")
        .get(ClientResponse.class);
    if (clientResponse.getStatus() < 400)
      return clientResponse.getEntity(JsonElement.class);
    throw new RuntimeException(str(clientResponse.getEntityInputStream()));
  }

  /**
   * replenish
   * 
   * @param refresh_token
   * @param slot_id e.g., DryDogFoodKibble1
   * @return
   * @throws Exception
   */
  public JsonElement replenish(String client_id, String refresh_token, String slot_id) {
    log.info(Log.str("replenish", refresh_token, slot_id));

    ClientResponse clientResponse = httpClient
        .resource(String.format("https://dash-replenishment-service-na.amazon.com/replenish/%s", slot_id))
        .header("Authorization", String.format("Bearer %s", access_token(client_id, refresh_token)))
        .header("x-amzn-type-version", "com.amazon.dash.replenishment.DrsReplenishInput@1.0")
        .header("x-amzn-accept-type", "com.amazon.dash.replenishment.DrsReplenishResult@1.0")
        .post(ClientResponse.class);
    
    if (clientResponse.getStatus() < 400)
      return clientResponse.getEntity(JsonElement.class);
    
    //###TODO TO FIX THIS: ONLY CALL REPLENISH IFF SUBSCRIBED=TRUE
    //###TODO TO FIX THIS: ONLY CALL REPLENISH IFF SUBSCRIBED=TRUE
    //###TODO TO FIX THIS: ONLY CALL REPLENISH IFF SUBSCRIBED=TRUE
//  SEVERE: java.lang.Exception: {"message":"Unable to place order - slot : <DryDogFoodKibble1> is unsubscribed."}
    //###TODO TO FIX THIS: ONLY CALL REPLENISH IFF SUBSCRIBED=TRUE
    //###TODO TO FIX THIS: ONLY CALL REPLENISH IFF SUBSCRIBED=TRUE
    //###TODO TO FIX THIS: ONLY CALL REPLENISH IFF SUBSCRIBED=TRUE
    
    throw new RuntimeException(str(clientResponse.getEntityInputStream()));
  }
  
  /**
   * callSlotStatusApi
   * 
   * @param client_id
   * @param refresh_token
   * @param slot_id
   * @param request
   * 
   * @return
   */
  public void callSlotStatusApi(String client_id, String refresh_token, String slot_id, SlotStatusApiRequest request) {
    
    log.info(Log.str("callSlotStatusApi", refresh_token, slot_id));

    ClientResponse clientResponse = httpClient
        .resource(String.format("https://dash-replenishment-service-na.amazon.com/slotStatus/%s", slot_id))
        .header("Authorization", String.format("Bearer %s", access_token(client_id, refresh_token)))
        .header("x-amzn-type-version", "com.amazon.dash.replenishment.DrsSlotStatusInput@1.0")
        .header("x-amzn-accept-type", "com.amazon.dash.replenishment.DrsSlotStatusResult@1.0")
        .post(ClientResponse.class, request);
    
    if (clientResponse.getStatus() < 400)
      return;
    
    throw new RuntimeException(str(clientResponse.getEntityInputStream()));
  }
  
  /**
   * callDeviceStatusApi
   * 
   * @param client_id
   * @param refresh_token
   * @param slot_id
   * @param request
   * 
   * @return
   */
  public void callDeviceStatusApi(String client_id, String refresh_token, String slot_id, DeviceStatusApiRequest request) {
    
    log.info(Log.str("callDeviceStatusApi", refresh_token, slot_id));

    ClientResponse clientResponse = httpClient
        .resource(String.format("https://dash-replenishment-service-na.amazon.com/deviceStatus/%s", slot_id))
        .header("Authorization", String.format("Bearer %s", access_token(client_id, refresh_token)))
        .header("x-amzn-type-version", "com.amazon.dash.replenishment.DrsDeviceStatusInput@1.0")
        .header("x-amzn-accept-type", "com.amazon.dash.replenishment.DrsDeviceStatusResult@1.0")
        .post(ClientResponse.class, request);
    
    if (clientResponse.getStatus() < 400)
      return;

    throw new RuntimeException(str(clientResponse.getEntityInputStream()));

  }

  private final Logger log = Logger.getLogger(this.getClass().getName());
}
