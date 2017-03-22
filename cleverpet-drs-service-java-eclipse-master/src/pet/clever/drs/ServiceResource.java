package pet.clever.drs;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.google.appengine.api.taskqueue.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;
import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.representation.*;

import pet.clever.drs.datastore.*;

class GetDrsRegistrationsResponse {
  public List<DrsRegistration> registrations = Lists.newArrayList();
  public GetDrsRegistrationsResponse(List<DrsRegistration> registrations) {
    this.registrations = registrations;
  }
}

//{
//"Type" : "Notification",
//"MessageId" : "cae4efce-55db-51dc-97c6-bfda022f2353",
//"TopicArn" : "arn:aws:sns:us-west-2:747453318199:replenish-dogfood",
//"Subject" : "DRS Notification",
//"Message" : "{\"notificationInfo\":{\"notificationType\":\"ItemShippedNotification\",\"lwaClientId\":\"amzn1.application-oa2-client.564f5fbd1cb74a08a58dffd26820a7c8\",\"notificationTime\":\"2016-07-28T18:57:56.343Z\",\"notificationId\":\"amzn1.dash.notification.v1.e0cda876-e13c-43d3-bade-b5b9d2534b40\",\"version\":\"2015-06-05\"},\"deviceInfo\":{\"deviceIdentifier\":{\"serialNumber\":\"1029384756\"},\"productIdentifier\":{\"modelId\":\"HB01\"}},\"customerInfo\":{\"directedCustomerId\":\"amzn1.account.AEFZGCZLUWOMD66RLDOVEODOAMTA\"},\"orderInfo\":{\"instanceId\":\"amzn1.dash.v2.o.ci43Yjg4NWIxOS1kZWFiLTRjZWYtYTBmMS0wMzI5NDY2MDgwMGUuMThjZDRkNmMtZjVlNy00YTE4LWFhNDctMzM2YTdhM2UyODZk\",\"slotId\":\"DryDogFoodKibble1\",\"productInfo\":[{\"quantity\":11.0,\"unit\":\"pounds\"}],\"estimatedDeliveryDate\":\"2016-08-04T06:59:59Z\"}}",
//"Timestamp" : "2016-07-28T18:57:56.865Z",
//"SignatureVersion" : "1",
//"Signature" : "fpYlthhtHEFQPxeGEEF5HOceB3+2LVyH25TI+40e/FZnlAOPAVGxoDK1uu2z3mXiSsR5Vyt+B2/f7GyxQDkl9NldHNC/9OjLEZZ+a4lVwVJnz9aIPzX05sQBoT5WAEHSsBwMbdkOso9oRQ4qVpOmXOxTAfVp4sGEb1p4TJ0sWwoc0bIKYsdoeDlDGxMjZq3J7PqhdiBB0j5M5yXiImuix7OCInZCpjmdfsQ273Qh1YI3ZC1/HMHsh2y36N0dnhm4XhAasFUg1jtPFzuxYH3jlEYo1dFWCpn8sMtbODtmZNBLKinrN2Tvha+loga6wOh+Iv2+31uhOkF0b5JhXvufcg==",
//"SigningCertURL" : "https://sns.us-west-2.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem",
//"UnsubscribeURL" : "https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:747453318199:replenish-dogfood:891b6649-4f08-4a29-8eb1-d93b0a02863a"
//}
class AmazonNotification {
  public String Type;
  public String MessageId;
  public String TopicArn;
  public String Subject;
  public String Message; // encoded json
  public Date Timestamp;
  public String SignatureVersion;
  public String SigningCertURL;
  public String SubscribeURL;
  public String UnsubscribeURL;
}

class PubSubMsg {
//  {
//    "message":{
//      "data":"cz0zMDAmdD0xNDcwMTkxMzYzJmI9MDQwMCZpZD01NjQzNjA2MDE4NzUyNTEyJmQ9JTdCJTIyZHN0JTIyJTNBJTIyMSUyMiUyQyUyMnRfbHVtJTIyJTNBJTIyNzclMjIlMkMlMjJkX2x1bSUyMiUzQSUyMjExJTIyJTJDJTIyZiUyMiUzQSUyMjM4JTIyJTJDJTIycnQlMjIlM0ElMjIlNUIwJTVEJTIyJTdE",
//      "attributes":{},
//      "message_id":"40668621054092",
//      "publish_time":"2016-08-03T02:34:21.483Z"
//    },
//    "subscription":"projects/dev-erpetcloud2/subscriptions/drs-subscription"
//  }
}

/**
 * 
 * @author rrizun
 *
 */
@Path("/service")
public class ServiceResource {

  @Context
  private UriInfo uriInfo;
  
  private final Client httpClient = DrsListener.httpClient;
  private final DrsService drsService = DrsListener.drsService;
  
  /**
   * gets the cleverbuild_number
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path("build")
  public JsonObject build() throws Exception {
    JsonObject jsonObject = new JsonObject();
    String cleverbuild_number = Resources.toString(Resources.getResource("cleverbuild_number"), Charsets.UTF_8).trim();
    jsonObject.addProperty("cleverbuild_number", cleverbuild_number);
    return jsonObject;
  }
  
  /**
   * get config
   * 
   * @return
   */
  @GET
  @Path("/config")
  public Configs config() {
    return drsService.getConfigs();
  }

  /**
   * get config
   * 
   * @param name
   * @return
   */
  @GET
  @Path("/config/{name}")
  public String config(@PathParam("name") String name) {
    return drsService.getConfig(name);
  }

  /**
   * set config
   * 
   * @param name
   * @param value
   */
  @POST
  @Path("/config/{name}")
  public void config(@PathParam("name") String name, String value) {
    drsService.setConfig(name, value);
  }

  /**
   * get amazon drs registrations
   * 
   * @return
   */
  @GET
  @Path("/drs/registrations")
  public GetDrsRegistrationsResponse getDrsRegistrations() {
    return new GetDrsRegistrationsResponse(drsService.getRegistrations());
  }
  
  /**
   * get amazon drs registration
   * 
   * this is the drs registration info from cleverpet's perspective
   * 
   * @param device_ID
   * @return
   */
  @GET
  @Path("/drs/registrations/{device_ID}")
  public DrsRegistration getDrsRegistration(@PathParam("device_ID") long device_ID) {
    DrsRegistration registration = drsService.getRegistration(device_ID);
    if (registration!=null)
      return registration;
    throw new WebApplicationException(404);
  }

  /**
   * get amazon drs subscription info
   * 
   * this is the drs subscription info from amazon's perspective
   * 
   * @param device_ID
   * @return
   */
  @GET
  @Path("/drs/registrations/{device_ID}/subscription-info")
  public JsonElement getDrsSubscriptionInfo(@PathParam("device_ID") long device_ID) {
    JsonElement subscriptionInfo = drsService.getSubscriptionInfo(device_ID);
    if (subscriptionInfo == null)
      throw new WebApplicationException(404);
    return subscriptionInfo;
  }
  
//  /**
//   * get amazon drs registration access token
//   * 
//   * @param device_ID
//   * @return
//   * @throws Exception
//   */
//  @GET
//  @Path("/drs/registrations/{device_ID}/access_token")
//  public GetAmazonDrsAccessTokenResponse getAccessToken(@PathParam("device_ID") long device_ID) throws Exception {
//    String access_token = drsService.getAccessToken(device_ID);
//    if (access_token!=null)
//      return new GetAmazonDrsAccessTokenResponse(access_token);
//    throw new WebApplicationException(404);
//  }

//  /**
//   * registration.process()
//   * 
//   * @param device_ID
//   */
//  @POST
//  @Path("/drs/registrations/{device_ID}/process")
//  public void processRegistration(@PathParam("device_ID") long device_ID) {
//    drsService.processRegistration(device_ID);
//  }

//  /**
//   * registration.replenish()
//   *
//   * called by scheduleReplenishOnce
//   * 
//   * @param device_ID
//   * @return
//   * @throws Exception
//   */
//  @POST
//  @Path("/drs/registrations/{device_ID}/replenish")
//  public JsonElement replenish(@PathParam("device_ID") long device_ID) throws Exception {
//    log.info(Log.str("replenish", device_ID));
//    return drsService.replenish(device_ID);
//  }

  /**
   * handle_lwa
   *
   * https://images-na.ssl-images-amazon.com/images/G/01/lwa/dev/docs/website-developer-guide._TTH_.pdf
   *
   * @param code
   * 
   * @deprecated
   * ### TODO MOVE TO PUBLIC RESOURCE
   */
  @GET
  @Path("/drs/handle_lwa")
  public Response handle_lwa(@QueryParam("code") String code, @QueryParam("state") String state) throws Exception {
    
    log.info(Log.str("handle_lwa", code, state));

    // decode "state" object from lwa index.html
    JsonObject jsonObject = new Gson().fromJson(URLDecoder.decode(state), JsonObject.class);
    long serial = jsonObject.get("serial").getAsLong();
    String client_id = jsonObject.get("client_id").getAsString();
    String redirect_uri = jsonObject.get("redirect_uri").getAsString();
    
      Form accessTokenRequest = new Form();
      accessTokenRequest.add("grant_type", "authorization_code");
      accessTokenRequest.add("code", code);
      accessTokenRequest.add("client_id", client_id);
      accessTokenRequest.add("client_secret", "245b17766253e3b9935cf0f64b533ddb0a0eb805ba2e53761fb4837dd860ee5a"); //###TODO MOVE THIS TO DATASTORE CONFIG
      accessTokenRequest.add("redirect_uri", redirect_uri);

      AccessTokenResponse accessTokenResponse = httpClient.resource("https://api.amazon.com/auth/o2/token").post(AccessTokenResponse.class, accessTokenRequest);
      long device_ID = serial;
      drsService.setRefreshToken(device_ID, client_id, accessTokenResponse.refresh_token);
      return Response.temporaryRedirect(new URI("https://www.amazon.com")).build();
  }

  /**
   * handle_sns
   * 
   * @param jsonObject
   * 
   * @deprecated
   * 
   * ### TODO MOVE TO PUBLIC RESOURCE
   */
  @POST
  @Path("/drs/handle_sns")
  public void handle_sns(JsonObject jsonObject) throws Exception {
    
//    INFO: {
//      "x-amz-sns-message-type": [
//        "SubscriptionConfirmation"
//      ],
//      "x-amz-sns-message-id": [
//        "6545435f-12bd-4ccd-9a4c-2bd4c5d30cba"
//      ],
//      "x-amz-sns-topic-arn": [
//        "arn:aws:sns:us-west-2:747453318199:replenish-dogfood"
//      ],
//      "Content-Length": [
//        "1602"
//      ],
//      "Content-Type": [
//        "text/plain; charset\u003dUTF-8"
//      ],
//      "Host": [
//        "45.56.92.92:8000"
//      ],
//      "User-Agent": [
//        "Amazon Simple Notification Service Agent"
//      ]
//    }

    //###TODO verify authenticity http://docs.aws.amazon.com/sns/latest/dg/SendMessageToHttp.html
    //###TODO verify authenticity http://docs.aws.amazon.com/sns/latest/dg/SendMessageToHttp.html
    //###TODO verify authenticity http://docs.aws.amazon.com/sns/latest/dg/SendMessageToHttp.html
    
    log.info(Log.str("handle_sns", jsonObject));

    AmazonNotification notification = new Gson().fromJson(jsonObject, AmazonNotification.class);
    
    // auto confirm
    if ("SubscriptionConfirmation".equals(notification.Type))
      httpClient.resource(notification.SubscribeURL).get(InputStream.class).close();

    if ("DRS Notification".equals(notification.Subject))
      drsService.handleDrsNotification(new Gson().fromJson(notification.Message, JsonObject.class));
  }

  // lifted from public abstract class BaseFormProvider<T extends MultivaluedMap<String, String>> extends AbstractMessageReaderWriterProvider<T> {
  private Form parseForm(String encoded) {
    Form form = new Form();
    StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
    String token;
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      int idx = token.indexOf('=');
      if (idx < 0) {
        form.add(URLDecoder.decode(token), null);
      } else if (idx > 0) {
        form.add(URLDecoder.decode(token.substring(0, idx)), URLDecoder.decode(token.substring(idx + 1)));
      }
    }
    return form;
  }
  
  /**
   * interactionRecordEvent
   * 
   * https://cloud.google.com/pubsub/subscriber
   * 
   * @return
   * @throws Exception
   * 
   * @deprecated move to WebhooksResource
   */
  @POST
  @Path("/drs/interactionRecordEvent")
  public void interactionRecordEvent(JsonObject pubSubMsg) {
    
    log.info(Log.str("interactionRecordEvent", pubSubMsg));
    
    String data_base64 = pubSubMsg.get("message").getAsJsonObject().get("data").getAsString();
    String data = new String(BaseEncoding.base64().decode(data_base64));
    Form form = parseForm(data);

    long device_ID = Long.parseLong(""+form.getFirst("id"));
    int b = Integer.parseInt(""+form.getFirst("b"), 16);
    
//    gcloud alpha pubsub subscriptions pull mysubscription --auto-ack
//    ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┬────────────────┬────────────┐
//    │                                                                                     DATA                                                                                    │   MESSAGE_ID   │ ATTRIBUTES │
//    ├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┼────────────────┼────────────┤
//    │ s=300&t=1470178687&b=0410&id=5643606018752512&d=%7B%22dst%22%3A%222%22%2C%22t_lum%22%3A%2273%22%2C%22d_lum%22%3A%2212%22%2C%22f%22%3A%2238%22%2C%22rt%22%3A%22%5B0%5D%22%7D │ 40615660034530 │            │
//    └─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┴────────────────┴────────────┘
//    rrizun@rrizun-XPS-8300:~$
    
    // s=300&t=1470178687&b=0410&id=5643606018752512&d={"dst":"2","t_lum":"73","d_lum":"12","f":"38","rt":"[0]"}

    // b=0410
    // 0000 0100 0001 0000
    
//    boolean food_eaten = (b & 0x0f00) == 0x0f00;
    
//    URI foo = new URI("/api/service/foo");
//    URI bar = foo.resolve("bar");

    boolean food_eaten = BigInteger.valueOf(b).testBit(11);
    drsService.handleInteractionRecordEvent(device_ID, food_eaten);
  }
  
//  @POST
//  @Path("/drs/interactionRecordEventDebug")
//  public void interactionRecordEventDebug(long device_ID) {
//    drsService.handleInteractionRecordEvent(device_ID, true);
//  }

  /**
   * run-drs
   * 
   * i.e., from cronJob
   */
  @GET
  @Path("/drs/run")
  public TaskHandle runDrs() {
    return drsService.runDrs();
  }

  /**
   * run-drs
   * 
   * i.e., from cronJob
   * 
   * @deprecated
   */
  @GET
  @Path("/drs/processRegistrations")
  public TaskHandle processRegistrations() {
    return drsService.runDrs();
  }

  private final Logger log = Logger.getLogger(this.getClass().getName());
}
