package pet.clever.drs;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.joda.time.*;

import com.google.common.io.*;
import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.representation.*;

import pet.clever.drs.datastore.*;

@Path("/public")
public class PublicResource {

  private final Client httpClient = DrsListener.httpClient;
  private final DrsService drsService = DrsListener.drsService;
  
//  event=spark%2Fstatus&data=online&published_at=2016-08-26T22%3A10%3A50.808Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fdevice%2Fapp-hash&data=10111D2DC19A0DDE03A057C58236AD8B1C67449B5C7AF72ECB3962D973148783&published_at=2016-08-26T22%3A10%3A50.869Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fstatus&data=auto-update&published_at=2016-08-26T22%3A10%3A55.922Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fflash%2Fstatus&data=started%20&published_at=2016-08-26T22%3A10%3A57.090Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fflash%2Fstatus&data=success%20&published_at=2016-08-26T22%3A11%3A18.313Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fstatus&data=online&published_at=2016-08-26T22%3A11%3A22.870Z&coreid=330024000f47343432313031&userid=57c08e6259a150e64b34c313&fw_version=21

  /**
   * handle spark event
   * 
   * @param event
   */
  @POST
  @Path("/spark/event")
  public void spark_event(Form event) {

    SparkEvent sparkEvent = new SparkEvent();
    
    sparkEvent.event = event.getFirst("event");
    sparkEvent.data = event.getFirst("data");
    sparkEvent.published_at = new DateTime(event.getFirst("published_at")).toDate();
    sparkEvent.coreid = event.getFirst("coreid");
    sparkEvent.fw_version = event.getFirst("fw_version");

    drsService.sparkEvent(sparkEvent);

  }

  /**
   * handle_lwa
   *
   * https://images-na.ssl-images-amazon.com/images/G/01/lwa/dev/docs/website-developer-guide._TTH_.pdf
   *
   * @param code
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

  private final Logger log = Logger.getLogger(this.getClass().getName());
}
