package pet.clever.drs;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.ws.rs.*;

import org.joda.time.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.gson.*;
import com.sun.jersey.api.*;
import com.sun.jersey.api.client.*;

import pet.clever.drs.datastore.*;

class SlotStatusApiRequest {
//"expectedReplenishmentDate" : "2015-12-28T10:00:00Z",
//"remainingQuantityInUnit" : 3.5,
//"originalQuantityInUnit" : 10,
//"totalQuantityOnHand" : 20,
//"lastUseDate" : "2015-12-21T10:00:00Z"
  public Date expectedReplenishmentDate; // when we'll reorder
  public int originalQuantityInUnit; // aka bag size
  public int remainingQuantityInUnit; // aka kibbles
//  public double totalQuantityOnHand; // if known
  
  /** Slot use date. The last time that product was consumed from this slot. */
  public Date lastUseDate;  
}

class DeviceStatusApiRequest {
//{
//"mostRecentlyActiveDate" : "2015-12-21T10:00:00Z"
//}
  /**
   * Most recently active date: the last time the device was known to be active
   * and connected to your cloud or app.
   */
  public Date mostRecentlyActiveDate; // e.g., "2015-12-21T10:00:00Z"
}

// ----------------------------------------------------------------------
// amazon drs notification schema...
// ----------------------------------------------------------------------

class NotificationInfo {
  public String notificationType;
  public String LWAClientId;
  public Date notificationTime;
  public String notifictionId;
  public String version;
}

class DeviceIdentifier {
  public String serialNumber;
}
class ProductIdentifier {
  public String modelId;
}
class DeviceInfo {
  public DeviceIdentifier deviceIdentifier;
  public ProductIdentifier productIdentifier;
}

class CustomerInfo {
  public String directedCustomerId;
}

class SubscriptionInfo {
  //"subscriptionInfo": {
  //"slotsSubscriptionStatus": {
  //  "vacuumBag": true,
  //  "vacuumFilter": true
  //}
  //},
  public JsonObject slotsSubscriptionStatus;
}

class ProductInfo {
  public double quantity;
  public String unit;
}
class OrderInfo {
  public String instanceId;
  public String slotId;
  public List<ProductInfo> productInfo;
  public Date estimatedDeliveryDate;
}

class AbstractNotification {
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
}

class DeviceRegisteredNotification {
  //{
  //"notificationInfo": {
  //  "notificationType": "DeviceRegisteredNotification",
  //  "LWAClientId": "amzn1.application-oa2-client.6b68e3c7ba3c434baa9283ad8747d469",
  //  "notificationTime": "2015-10-01T02:23:52.747Z",
  //  "notificationId": "amzn1.dash.notification.v1.4eab8f47-f876-4650-a74a-5ca90bb657c0",
  //  "version": "2015-06-05"
  //},
  //"deviceInfo": {
  //  "deviceIdentifier": {
  //    "serialNumber": "mySerial"
  //  },
  //  "productIdentifier": {
  //    "modelId": "myDeviceModel"
  //  }
  //},
  //"customerInfo": {
  //  "directedCustomerId": "amzn1.account.AEYAPUSDAKRB7KDV5WSAAIAIVMSA"
  //}
  //}
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
}

class DeviceDeregisteredNotification {
  //{
  //"notificationInfo": {
  //  "notificationType": "DeviceDeregisteredNotification",
  //  "LWAClientId": "amzn1.application-oa2-client.6b68e3c7ba3c434baa9283ad8747d469",
  //  "notificationTime": "2015-09-29T18:03:28.669Z",
  //  "notificationId": "amzn1.dash.notification.v1.3b060d60-f5e1-49d1-93f9-4020f0060297",
  //  "version": "2015-06-05"
  //},
  //"deviceInfo": {
  //  "deviceIdentifier": {
  //    "serialNumber": "mySerial"
  //  },
  //  "productIdentifier": {
  //    "modelId": "myDeviceModel"
  //  }
  //},
  //"customerInfo": {
  //  "directedCustomerId": "amzn1.account.AFOM77AMO524CJY3HPKB2GIJCBAQ"
  //}
  //}
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
}

class SubscriptionChangedNotification {
  //{
  //  "notificationInfo": {
  //    "notificationType": "SubscriptionChangedNotification",
  //    "LWAClientId": "amzn1.application-oa2-client.1bf9d3fa83bc4c50968ecf595ace1127",
  //    "notificationTime": "2015-09-30T23:27:50.771Z",
  //    "notificationId": "amzn1.dash.notification.v1.067cb532-ec2d-4ab4-a0ed-6e3da599cb83",
  //    "version": "2015-06-05"
  //  },
  //  "deviceInfo": {
  //    "deviceIdentifier": {
  //      "serialNumber": "mySerial"
  //    },
  //    "productIdentifier": {
  //      "myDeviceModel": "DrsBountySampleDevice"
  //    }
  //  },
  //  "subscriptionInfo": {
  //    "slotsSubscriptionStatus": {
  //      "vacuumBag": true,
  //      "vacuumFilter": true
  //    }
  //  },
  //  "customerInfo": {
  //    "directedCustomerId": "amzn1.account.AEZPSLGB77YDGN55ESXOP2WWA3ZQ"
  //  }
  //}
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
  public SubscriptionInfo subscriptionInfo;
}

class OrderPlacedNotification {
  //"notificationInfo": {
  //"notificationType": "OrderPlacedNotification",
  //"LWAClientId": "amzn1.application-oa2-client.6b68e3c7ba3c434baa9283ad8747d469",
  //"notificationTime": "2015-10-01T20:19:39.959Z",
  //"notificationId": "amzn1.dash.notification.v1.66142640-5d57-4efd-947e-4a4825970971",
  //"version": "2015-06-05"
  //},
  //"deviceInfo": {
  //"deviceIdentifier": {
  //  "serialNumber": "mySerial"
  //},
  //"productIdentifier": {
  //  "modelId": "myDeviceModel"
  //}
  //},
  //"customerInfo": {
  //"directedCustomerId": "amzn1.account.AHIJYKW2N4EGMV3GWWH4P4GA2O5Q"
  //},
  //"orderInfo": {
  //  "instanceId": "amzn1.dash.v1.o.ci44MzUwODFkNy1iMmViLTQ1MTUtYWIwYS03NDIzYmNmNWU3NzkuZjZmOWI2NjgtMTUyOS00YTQ1LWFlNDEtZTY2ZGE5ZGRlZDhj",
  //  "slotId": "vacuumBag",
  //  "productInfo": [
  //    {
  //      "quantity": 12.0,
  //      "unit": "Count"
  //    }
  //  ],
  //  "estimatedDeliveryDate": "2015-10-07T06:59:59Z"
  //}
  //}
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
  public OrderInfo orderInfo;
}

class OrderCancelledNotification {
  //{
  //"notificationInfo": {
  //  "notificationType": "OrderCancelledNotification",
  //  "LWAClientId": "amzn1.application-oa2-client.6b68e3c7ba3c434baa9283ad8747d469",
  //  "notificationTime": "2015-10-01T20:20:29.461Z",
  //  "notificationId": "amzn1.dash.notification.v1.93f419a5-da08-45d4-9abb-eb88a1bafb1d",
  //  "version": "2015-06-05"
  //},
  //"deviceInfo": {
  //  "deviceIdentifier": {
  //    "serialNumber": "mySerial"
  //  },
  //  "productIdentifier": {
  //    "modelId": "myDeviceModel"
  //  }
  //},
  //"customerInfo": {
  //  "directedCustomerId": "amzn1.account.AHIJYKW2N4EGMV3GWWH4P4GA2O5Q"
  //},
  //"orderInfo": {
  //  "instanceId": "amzn1.dash.v1.o.ci44MzUwODFkNy1iMmViLTQ1MTUtYWIwYS03NDIzYmNmNWU3NzkuZjZmOWI2NjgtMTUyOS00YTQ1LWFlNDEtZTY2ZGE5ZGRlZDhj",
  //  "slotId": "vacuumBag"
  //}
  //}
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
  public OrderInfo orderInfo;
}

class ItemShippedNotification {
  //{
  //"notificationInfo": {
  //  "notificationType": "ItemShippedNotification",
  //  "LWAClientId": "amzn1.application-oa2-client.6b68e3c7ba3c434baa9283ad8747d469",
  //  "notificationTime": "2015-10-01T20:43:28.626Z",
  //  "notificationId": "amzn1.dash.notification.v1.aaa0d823-4021-4e67-9ecc-ba4ee5467ae5",
  //  "version": "2015-06-05"
  //},
  //"deviceInfo": {
  //  "deviceIdentifier": {
  //    "serialNumber": "mySerial"
  //  },
  //  "productIdentifier": {
  //    "modelId": "myDeviceModel"
  //  }
  //},
  //"customerInfo": {
  //  "directedCustomerId": "amzn1.account.AHIJYKW2N4EGMV3GWWH4P4GA2O5Q"
  //},
  //"orderInfo": {
  //  "instanceId": "amzn1.dash.v1.o.ci4wMWZlODRhNC0wNGM2LTQxNzMtYmQ1OS00MmY3MjRlODNiM2UuMWQ1ODYyZWQtZTU4OS00ODdiLWJkODYtODQyYTBmZDMxM2Ex",
  //  "slotId": "vacuumBag",
  //  "productInfo": [
  //    {
  //      "quantity": 12.0,
  //      "unit": "Count"
  //    }
  //  ],
  //  "estimatedDeliveryDate": "2015-10-07T06:59:59Z"
  //}
  //}
  public NotificationInfo notificationInfo;
  public DeviceInfo deviceInfo;
  public CustomerInfo customerInfo;
  public OrderInfo orderInfo;
}

/**
 * 
 * @author rrizun
 *
 */
class RunDrsIncremental implements DeferredTask {
  private final long last_device_ID;
  public RunDrsIncremental(long last_device_ID) {
    this.last_device_ID = last_device_ID;
  }
  @Override
  public void run() {
    DrsListener.drsService.processRegistrationsIncremental(last_device_ID);
  }
  private static final long serialVersionUID = 1L;
}

/**
 * 
 * @author rrizun
 *
 */
class DeferredReplenishApi implements DeferredTask {
  private final long device_ID;
  public DeferredReplenishApi(long device_ID) {
    this.device_ID = device_ID;
  }
  @Override
  public void run() {
    // 1. call replenish api
    final JsonElement response = DrsListener.drsService.replenish(device_ID);
    // 2. write replenish api response to datastore
    new DsHelper(DatastoreServiceFactory.getDatastoreService()).readModifyWrite(DrsRegistration.class, device_ID, new ReadModifyWrite<DrsRegistration>() {
      @Override
      public void modify(DrsRegistration registration) {
        registration.last_replenish_response = ""+response;
      }
    });
  }
  private static final long serialVersionUID = 1L;
}

/**
 * 
 * @author rrizun
 *
 */
class DeferredSlotStatusApi implements DeferredTask {
  private final long device_ID;
  public DeferredSlotStatusApi(long device_ID) {
    this.device_ID = device_ID;
  }
  @Override
  public void run() {
    DrsListener.drsService.callSlotStatusApi(device_ID);
  }
  private static final long serialVersionUID = 1L;
}

/**
 * 
 * @author rrizun
 *
 */
class DeferredDeviceStatusApi implements DeferredTask {
  private final long device_ID;
  public DeferredDeviceStatusApi(long device_ID) {
    this.device_ID = device_ID;
  }
  @Override
  public void run() {
    DrsListener.drsService.callDeviceStatusApi(device_ID);
  }
  private static final long serialVersionUID = 1L;
}

/**
 * 
 * @author rrizun
 *
 */
public class DrsService {

  private static final String DRYDOGFOODKIBBLE1_SLOT_ID = "DryDogFoodKibble1";
  
  private final Client httpClient;
  private final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
  
  /**
   * ctor
   * 
   * @param configs
   * @param drsSdk
   */
  public DrsService(Client httpClient) {
    this.httpClient = httpClient;
  }
  
  private DrsSdk drsSdk() {
    //###TODO client_id and client_secret from datastore!!
    //###TODO client_id and client_secret from datastore!!
    //###TODO client_id and client_secret from datastore!!
    return new DrsSdk(httpClient, null, null);
    //###TODO client_id and client_secret from datastore!!
    //###TODO client_id and client_secret from datastore!!
    //###TODO client_id and client_secret from datastore!!
  }
  
  private Queue cloudQueue() {
    return QueueFactory.getQueue("cloud");
  }
  
  private TaskHandle scheduleOnce(DeferredTask task) {
    TaskOptions taskOptions = TaskOptions.Builder.withDefaults()
        .payload(task);
    RetryOptions retryOptions = RetryOptions.Builder.withDefaults()
        .taskRetryLimit(0);
    return cloudQueue().add(taskOptions.retryOptions(retryOptions));
  }

  private final Supplier<Configs> configsSupplier = Suppliers.memoizeWithExpiration(new Supplier<Configs>() {
    @Override
    public Configs get() {
      log.info(Log.str("configSupplier.get"));
      JsonObject jsonObject = new JsonObject();
      for (Config config : new DsHelper(ds).query(Config.class).list())
        jsonObject.addProperty(config.name, config.value);
      return new Gson().fromJson(jsonObject, Configs.class);
    }
  }, 1, TimeUnit.MINUTES);
  
  public Configs getConfigs() {
    return configsSupplier.get();
  }
  
  /**
   * handle spark/particle.io event webhook
   * 
   * @param sparkEvent
   */
  public void sparkEvent(SparkEvent sparkEvent) {
    new DsHelper(ds).write(sparkEvent);
  }

  public String getConfig(String name) {
    Config config = new DsHelper(ds).get(Config.class, name);
    if (config!=null)
      return config.value;
    throw new WebApplicationException(404);
  }
  
  public void setConfig(String name, String value) {
    Config config = new Config();
    config.name = name;
    config.value = value;
    new DsHelper(ds).write(config);
  }
  
  public List<DrsRegistration> getRegistrations() {
    return new DsHelper(ds).query(DrsRegistration.class).list();
  }
  
  /**
   * get registration
   * 
   * @param device_ID
   * @return
   */
  public DrsRegistration getRegistration(long device_ID) {
    int kibbles_per_day_minimum = getConfigs().drs_kibbles_per_day_minimum;
    Device device = new DsHelper(ds).get(Device.class, device_ID);
    if (device!=null) {
      DateTimeZone deviceZone = DateTimeZone.forOffsetMillis(1000*device.time_zone);
      DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
      if (registration!=null) {
        // synthetic field not in datastore
        registration.kibbles_per_day = kibblesPerDay(registration, kibbles_per_day_minimum);
        // synthetic field not in datastore
        registration.expectedReplenishmentDate = calcExpectedReplenishmentDate(registration, deviceZone, kibbles_per_day_minimum);
      }
      return registration;
    }
    return null;
  }

  /**
   * get amazon drs access token
   * 
   * @param device_ID
   * @return
   * @throws Exception
   */
  public String getAccessToken(long device_ID) throws Exception {
    Device deviceEntity = new DsHelper(ds).get(Device.class, device_ID);
    if (deviceEntity != null) {
      DrsRegistration registrationEntity = new DsHelper(ds).get(DrsRegistration.class, device_ID);
      if (registrationEntity != null) {
        DrsRegistrationRefreshToken refreshTokenEntity = new DsHelper(ds).get(DrsRegistrationRefreshToken.class, device_ID);
        if (refreshTokenEntity != null) {
          String client_id = refreshTokenEntity.client_id;
          String refresh_token = refreshTokenEntity.refresh_token;
          return drsSdk().access_token(client_id, refresh_token);
        }
      }
    }
    return null;
  }
  
          //  /**
          //   * if has kibbles then
          //   *  if has history then return calc else return later
          //   * else return now
          //   * 
          //   * @param device
          //   * @param registration
          //   * @return
          //   */
          //  private Date calcExpectedReplenishmentDatezzz(DrsRegistration registration, DateTimeZone deviceZone) {
          //
          //    //###TODO HACK HACK HACK
          ////    registration.kibbles = 1500;                
          ////    registration.kibbles_average.clear();
          ////    registration.kibbles_average.addAll(ImmutableList.of(5,25,50,75));
          //    //###TODO HACK HACK HACK
          //    
          //    if (registration.kibbles > 0) {
          //      // is there any history?
          //      if (registration.kibbles_average.size() > 1) {
          //        // yes
          //        int sum = 0;
          //        for (int i = 1; i < registration.kibbles_average.size(); ++i)
          //          sum += registration.kibbles_average.get(i);
          //        int avg_daily = sum / (registration.kibbles_average.size()-1);
          //        if (avg_daily > 0)
          //          return new DateTime(deviceZone).plusDays(registration.kibbles/avg_daily).minusDays(7).withTimeAtStartOfDay().toDate();
          //      }
          //      return null;
          ////      return new DateTime(Long.MAX_VALUE, deviceZone).withTimeAtStartOfDay().toDate();
          //    }
          //
          //    // outOfKibbles! return NOW!!
          //    return new DateTime(deviceZone).withTimeAtStartOfDay().toDate();
          //  }

  /**
   * kibblesPerDay
   * 
   * @param registration
   * @param kibbles_per_day_minimum
   * @return
   */
  private int kibblesPerDay(DrsRegistration registration, final int kibbles_per_day_minimum) {
    List<Integer> list = Lists.newArrayList(registration.kibbles_average);
    
    if (list.size() > 0)
      list.remove(0); // remove current day
    
    if (list.size()==0)
      list.add(kibbles_per_day_minimum); // be sure there is at least one day
    
    int sum = 0;
    for (int i = 0; i < list.size(); ++i) {
      int kibbles = list.get(i);
      if (kibbles < kibbles_per_day_minimum)
        kibbles = kibbles_per_day_minimum;
      sum += kibbles;
    }
    
    return sum / list.size();
  }
  
  /**
   * if has kibbles then
   *  if has history then return calc else return later
   * else return now
   * 
   * @param device
   * @param registration
   * @return
   */
  private Date calcExpectedReplenishmentDate(DrsRegistration registration, DateTimeZone deviceZone, final int kibbles_per_day_minimum) {

    if (registration.kibbles > 0) {
      int kibbles_per_day = kibblesPerDay(registration, kibbles_per_day_minimum);
      if (kibbles_per_day > 0) {
        DateTime now = new DateTime();
        DateTime result = new DateTime(deviceZone).plusDays(registration.kibbles/kibbles_per_day).minusDays(7);
        if (result.compareTo(now) < 0)
          result = now;
        return result.withTimeAtStartOfDay().toDate();
      }
      return null;
    }

    // outOfKibbles! return NOW!!
    return new DateTime(deviceZone).withTimeAtStartOfDay().toDate();

  }

  /**
   * handle drs registration event from lwa
   *
   * @param device_ID
   * @param refresh_token
   */
  public void setRefreshToken(long device_ID, String client_id, String refresh_token) {
    log.info(Log.str("setRefreshToken", device_ID, refresh_token));
    // unconditionally write refresh token to datastore
    DrsRegistrationRefreshToken refreshToken = new DrsRegistrationRefreshToken();
    refreshToken.device_ID = device_ID;
    refreshToken.client_id = client_id;
    refreshToken.refresh_token = refresh_token;
    new DsHelper(ds).write(refreshToken);
  }

  /**
   * getSubscriptionInfo
   *
   * @param device_ID
   *          cleverpet device ID
   * @return
   */
  public JsonElement getSubscriptionInfo(long device_ID) {
    //###TODO security: should really take a String user_ID argument here and verify user owns device
    DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
    if (registration == null)
      throw new WebApplicationException(404);
    DrsRegistrationRefreshToken refreshToken = new DsHelper(ds).get(DrsRegistrationRefreshToken.class, device_ID);
    if (refreshToken == null)
      throw new WebApplicationException(404);
    return drsSdk().getSubscriptionInfo(refreshToken.client_id, refreshToken.refresh_token);
  }

  /**
   * replenish
   *
   * @param device_ID
   *          cleverpet device ID
   * @return
   */
  public JsonElement replenish(long device_ID) {
    //###TODO security: should really take a String user_ID argument here and verify user owns device
    log.info(Log.str("replenish", device_ID));
    DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
    if (registration==null)
      throw new WebApplicationException(404);
    log.info(Log.str("replenish", registration));
    DrsRegistrationRefreshToken refreshToken = new DsHelper(ds).get(DrsRegistrationRefreshToken.class, device_ID);
    if (refreshToken != null) {
      JsonElement jsonElement = drsSdk().replenish(refreshToken.client_id, refreshToken.refresh_token, DRYDOGFOODKIBBLE1_SLOT_ID);
      log.info(Log.str(jsonElement));
      return jsonElement;
    }
    throw new WebApplicationException(404);
  }
  
  /**
   * callSlotStatusApi
   * 
   * @param device_ID
   * @return
   * @throws Exception
   */
  public void callSlotStatusApi(long device_ID) {
    
    log.info(Log.str(device_ID));
    
    int kibbles_per_day_minimum = getConfigs().drs_kibbles_per_day_minimum;
    
    Device device = new DsHelper(ds).get(Device.class, device_ID);
    if (device == null)
      throw new NotFoundException("device not found");

    DateTimeZone deviceZone = DateTimeZone.forOffsetMillis(1000*device.time_zone);

    DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
    if (registration == null)
      throw new NotFoundException("drs registration not found");
    
    DrsRegistrationRefreshToken refreshToken = new DsHelper(ds).get(DrsRegistrationRefreshToken.class, device_ID);
    if (refreshToken == null)
      throw new RuntimeException("refresh token not found");

    SlotStatusApiRequest slotStatusApiRequest = new SlotStatusApiRequest();
    
    slotStatusApiRequest.expectedReplenishmentDate  = calcExpectedReplenishmentDate(registration, deviceZone, kibbles_per_day_minimum);
    slotStatusApiRequest.originalQuantityInUnit = registration.originalQuantityInUnit;
    slotStatusApiRequest.remainingQuantityInUnit = registration.kibbles;
    slotStatusApiRequest.lastUseDate = registration.lastUseDate;
    
    drsSdk().callSlotStatusApi(refreshToken.client_id, refreshToken.refresh_token, DRYDOGFOODKIBBLE1_SLOT_ID, slotStatusApiRequest);
  }

  /**
   * callDeviceStatusApi
   * 
   * @param device_ID
   * @return
   */
  public void callDeviceStatusApi(long device_ID) {
    log.info(Log.str(device_ID));
    
    Device device = new DsHelper(ds).get(Device.class, device_ID);
    if (device == null)
      throw new NotFoundException("device not found");

    DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
    if (registration == null)
      throw new NotFoundException("drs registration not found");
    
    DrsRegistrationRefreshToken refreshToken = new DsHelper(ds).get(DrsRegistrationRefreshToken.class, device_ID);
    if (refreshToken == null)
      throw new RuntimeException("refresh token not found");

    DeviceStatusApiRequest deviceStatusApiRequest = new DeviceStatusApiRequest();
    
    deviceStatusApiRequest.mostRecentlyActiveDate = device.last_modified; //###TODO double/triple check that this is a reasonable value for this field
    
    drsSdk().callDeviceStatusApi(refreshToken.client_id, refreshToken.refresh_token, DRYDOGFOODKIBBLE1_SLOT_ID, deviceStatusApiRequest);
  }

  /**
   * handleDrsNotification
   * 
   * handle amazon drs notifications
   * 
   * https://github.com/CleverPet/cleverpet-ios-app/commit/2acaddb73f09d08febc7c95118afa0b377b79e43
   * 
   * @param drsNotification
   * 
   * ###TODO USE PUBLISH SNS MESSAGES TO A SQS QUEUE
   * ###TODO USE PUBLISH SNS MESSAGES TO A SQS QUEUE
   * ###TODO USE PUBLISH SNS MESSAGES TO A SQS QUEUE
   */
  public void handleDrsNotification(JsonObject drsNotification) throws Exception {
    
    ///###TODO RACE CONDITION OBSERVED BETWEEN DEVICEREGISTERED AND SUBSCRIPTIONCHANGED EVENTS
    /// (a) put this whole thing in a datastore transaction? (doubtful)
    /// -or-
    /// (b) throw exception if registration not found and lean on amazon sns for retry (probably)
    /// needs pondering..
    
    /// and deviceDeregistered should delete the entity (not just mark it as unregistered)
    
    /// 2016-08-04 actually, both! do both (a) transaction and (b) throw if registration not found
    
    log.info(Log.str("handleDrsNotification", drsNotification));

    AbstractNotification abstractNotification = new Gson().fromJson(drsNotification, AbstractNotification.class);

    long device_ID = Long.parseLong(abstractNotification.deviceInfo.deviceIdentifier.serialNumber);
    
    // do we know this device?
    Device device = new DsHelper(ds).get(Device.class, device_ID);
    if (device!=null) { // yes!!

      // DeviceRegisteredNotification
      if ("DeviceRegisteredNotification".equals(abstractNotification.notificationInfo.notificationType)) {
        // create a new device registration iff not exists
        DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
        if (registration == null) { //###TODO subtle transaction issue here..
          registration = new DrsRegistration();
          registration.device_ID = device_ID;
          new DsHelper(ds).write(registration);
        }
      }
      
      // SubscriptionChangedNotification
      if ("SubscriptionChangedNotification".equals(abstractNotification.notificationInfo.notificationType)) {
        // update subscribed status
        SubscriptionChangedNotification subscriptionChangedNotification = new Gson().fromJson(drsNotification, SubscriptionChangedNotification.class);
        // begin read-modify-write transaction
        final Transaction txn = ds.beginTransaction();
        try {
          DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);

          // if registration not found then just throw
          // and lean on amazon sns to retry to workaround distributed race conditions
          // wrt DeviceRegistered and SubscriptionChanged
          if (registration==null)
            throw new Exception("registration==null");
          
          // keep track of subscription status
          registration.subscribed = subscriptionChangedNotification.subscriptionInfo.slotsSubscriptionStatus.get(DRYDOGFOODKIBBLE1_SLOT_ID).getAsBoolean();

          // if subscribed then reset kibbles
          if (registration.subscribed) {
            registration.kibbles = 0;
          }
          
          new DsHelper(ds).write(registration);

          txn.commit();
        } finally {
          if (txn.isActive())
            txn.rollback();
        }
      }

      // OrderPlacedNotification
      if ("OrderPlacedNotification".equals(abstractNotification.notificationInfo.notificationType)) {
        OrderPlacedNotification orderPlacedNotification = new Gson().fromJson(drsNotification, OrderPlacedNotification.class);
        
        int kibbles_per_pound = getConfigs().drs_kibbles_per_pound;

        // begin read-modify-write transaction
        final Transaction txn = ds.beginTransaction();
        try {
          DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
          registration.order_inprogress = true;

          // remember bag size
          registration.originalQuantityInUnit = 0;
          registration.originalQuantityInUnit += kibbles_per_pound * orderPlacedNotification.orderInfo.productInfo.get(0).quantity;
          
          new DsHelper(ds).write(registration);
          txn.commit();
        } finally {
          if (txn.isActive())
            txn.rollback();
        }
      }
      
      // OrderCancelledNotification
      if ("OrderCancelledNotification".equals(abstractNotification.notificationInfo.notificationType)) {
        // update order_inprogress status
        // begin read-modify-write transaction
        final Transaction txn = ds.beginTransaction();
        try {
          DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
          
          if (registration.order_inprogress!=true)
            throw new Exception("order_inprogress!=true");
          
          registration.order_inprogress = false;
          registration.kibbles += registration.originalQuantityInUnit/4; // increment 1/4 bag
          new DsHelper(ds).write(registration);
          txn.commit();
        } finally {
          if (txn.isActive())
            txn.rollback();
        }
      }
      
      // ItemShippedNotification
      if ("ItemShippedNotification".equals(abstractNotification.notificationInfo.notificationType)) {
//        ItemShippedNotification itemShippedNotification = new Gson().fromJson(drsNotification, ItemShippedNotification.class);
//        
//        int kibbles_per_pound = configs.drs_kibbles_per_pound();
        
        // update (a) order_inprogress status and (b) kibble count
        // begin read-modify-write transaction
        final Transaction txn = ds.beginTransaction();
        try {
          DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);

          if (registration.order_inprogress!=true)
            throw new Exception("order_inprogress!=true");
          
          registration.order_inprogress = false;

          //###TODO INCREMENTING KIBBLE COUNT LIKE THIS IS NOT AN IDEMPOTENT OPERATION
          //###TODO THIS IS BAD IF AMAZON SNS RETRIES POSTING A NOTIFICATION
          
          //###TODO DOUBLE CHECK THAT UNITS ARE LBS
          registration.kibbles += registration.originalQuantityInUnit;
          
          //###TODO INCREMENTING KIBBLE COUNT LIKE THIS IS NOT AN IDEMPOTENT OPERATION
          //###TODO THIS IS BAD IF AMAZON SNS RETRIES POSTING A NOTIFICATION
          
          //###TODO POSSIBLE SOLUTION: KEEP TRACK OF SOME SORT OF UNIQUE ORDER ID IN A "ORDERS SHIPPED" COLLECTION
          //###TODO AN ON 2ND TIME AROUND (I.E., RETRY) CHECK TO SEE IF ORDER ID WAS ALREADY PROCESSED AND IF SO THEN DON'T DECREMENT

          new DsHelper(ds).write(registration);
          txn.commit();
        } finally {
          if (txn.isActive())
            txn.rollback();
        }
      }

      // DeviceDeregisteredNotification
      if ("DeviceDeregisteredNotification".equals(abstractNotification.notificationInfo.notificationType)) {
        // unconditionally delete existing device registration
        new DsHelper(ds).delete(DrsRegistration.class, device_ID);
      }
      
    }
  }

  /**
   * handleInteractionRecordEvent
   * 
   * handle interaction records published/subscribed to/from google pub/sub
   * 
   * @param device_ID
   * @param food_eaten
   */
  public void handleInteractionRecordEvent(long device_ID, boolean food_eaten) {

    log.info(Log.str("handleInteractionRecordEvent", device_ID, food_eaten));

    // begin read-modify-write
    final Transaction txn = ds.beginTransaction();
    try {
      DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
      if (registration!=null) {
        if (registration.subscribed) {
          
          log.info(Log.str("handleInteractionRecordEvent", "subscribed", registration));
          
          if (food_eaten) {
            // delta
            registration.kibbles--;
            
            // daily averages
//            if (registration.kibbles_average==null)
//              registration.kibbles_average = Lists.newArrayList();
            if (registration.kibbles_average.size()==0)
              registration.kibbles_average.add(0);
            registration.kibbles_average.set(0, registration.kibbles_average.get(0) + 1);
            
            // Slot  use date.   The last  time  that  product was consumed  from  this  slot.
            registration.lastUseDate = new Date();
            
            new DsHelper(ds).write(registration);
            
          }
        }
      }
      txn.commit();
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }

  }

  /**
   * process drs registration
   * 
   * check to see if there is any work to do wrt the given registration and do it!
   * 
   * @param device_ID
   */
  public void processRegistration(long device_ID) {
    
    log.info(Log.str("processRegistration", device_ID));

    Configs configs = getConfigs();

    Device device = new DsHelper(ds).get(Device.class, device_ID);
    
    if (device == null) {
      
      log.warning(Log.str("device not found", device_ID));
      
    } else {
      
      DateTimeZone deviceZone = DateTimeZone.forOffsetMillis(1000*device.time_zone);
      
      // need to pluck these outside of datastore transaction below..
      String average_cron = configs.drs_average_schedule;
      String process_cron = configs.drs_replenish_schedule;
      String slot_status_schedule = configs.drs_slot_status_schedule;
      String device_status_schedule = configs.drs_device_status_schedule;
      
      int kibbles_per_day_minimum = configs.drs_kibbles_per_day_minimum;

      // begin read-modify-write
      final Transaction txn = ds.beginTransaction();
      try {
        DrsRegistration registration = new DsHelper(ds).get(DrsRegistration.class, device_ID);
        if (registration.subscribed) {

          // STEP 1 process average
          doCheckAverageInDatastoreTransaction(registration, deviceZone, average_cron);
          
          // STEP 2 process reorder
          doCheckReorderInDatastoreTransaction(registration, deviceZone, process_cron, kibbles_per_day_minimum);
          
          // STEP 3 check to see if we need to call the slot status api
          checkSlotStatusApi(registration, deviceZone, slot_status_schedule);

          // STEP 4 check to see if we need to call the device status api
          checkDeviceStatusApi(registration, deviceZone, device_status_schedule);
          
          new DsHelper(ds).write(registration);
        }
        txn.commit();
      } finally {
        if (txn.isActive())
          txn.rollback();
      }
    }
  }
  
  // check for average
  // @param average_cron cron expression for how often to logically average registration
  // "InDatastoreTransaction" means that this function is called from inside a datastore transaction
  // so therefore need to refrain from making arbitrary datastore calls
  private void doCheckAverageInDatastoreTransaction(DrsRegistration registration, DateTimeZone deviceZone, String average_cron) {
    
    final DateTime now = new DateTime();

    DateTime last_averaged_at = new DateTime(0); // for first time around
    if (registration.last_averaged_at!=null)
      last_averaged_at = new DateTime(registration.last_averaged_at);

    // time to shift daily average sliding windows?
    CronSequenceGenerator averageCron = new CronSequenceGenerator(average_cron, deviceZone.toTimeZone());
    DateTime next_average_date = new DateTime(averageCron.next(last_averaged_at.toDate()), deviceZone);
    if (next_average_date.compareTo(now) < 0) {
      // yes!!

      registration.kibbles_average.add(0, 0); // shift daily average sliding windows
      // truncate.. keep current (index 0) + 7 day sliding average (index 1..7)
      if (registration.kibbles_average.size()>8)
        registration.kibbles_average.remove(8);
      
      // update last_averaged_at
      registration.last_averaged_at = now.toDate();
    }
  }
  
  // check for reorder
  // @param process_cron cron expression for how often to logically process registration
  // "InDatastoreTransaction" means that this function is called from inside a datastore transaction
  // so therefore need to refrain from making arbitrary datastore calls
  private void doCheckReorderInDatastoreTransaction(DrsRegistration registration, DateTimeZone deviceZone, String process_cron, int kibbles_per_day_minimum) {
    
    final DateTime now = new DateTime();

    DateTime last_replenish_at = new DateTime(0); // for first time around
    if (registration.last_replenish_at!=null)
      last_replenish_at = new DateTime(registration.last_replenish_at);
  
    // time to process?
    CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(process_cron, deviceZone.toTimeZone());
    DateTime next_replenish_at = new DateTime(cronSequenceGenerator.next(last_replenish_at.toDate()), deviceZone);
    if (next_replenish_at.compareTo(now) < 0) {
      // yes!
  
      // re-order?
      Date expectedReplenishmentDate = calcExpectedReplenishmentDate(registration, deviceZone, kibbles_per_day_minimum);
      if (expectedReplenishmentDate!=null) { // ###TODO this null check should be unneccessary now..
        if (expectedReplenishmentDate.compareTo(new Date()) < 0) {
          // yes!
          scheduleOnce(new DeferredReplenishApi(registration.device_ID));
          // Note- don't set order_inprogress here.. instead let OrderPlacedNotification set order_inprogress=true
        }
      }

      // update last_replenish_at
      registration.last_replenish_at = now.toDate();
    }
  }
  
  /**
   * checkSlotStatusApi
   * 
   * check to see if the slot status api needs to be called wrt the logical slot_status_schedule
   * 
   * if so then schedule it!!
   * 
   * @param registration
   * @param deviceZone
   * @param slot_status_schedule the logical schedule at which to call the slot status api
   */
  private void checkSlotStatusApi(DrsRegistration registration, DateTimeZone deviceZone, String slot_status_schedule) {

    final Date now = new Date();
    final long device_ID = registration.device_ID;

    Date last_slot_status_at = new Date(0); // for first time around
    if (registration.last_slot_status_at!=null)
      last_slot_status_at = new DateTime(registration.last_slot_status_at).toDate();
  
    CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(slot_status_schedule, deviceZone.toTimeZone());
    Date next_slot_status_at = cronSequenceGenerator.next(last_slot_status_at);
    // time to call amazon drs slot status api?
    if (next_slot_status_at.compareTo(now) < 0) {
      // yes!
      scheduleOnce(new DeferredSlotStatusApi(device_ID));
      registration.last_slot_status_at = now;
    }
  }

  /**
   * checkDeviceStatusApi
   * 
   * check to see if the device status api needs to be called wrt the logical device_status_schedule
   * 
   * if so then schedule it!!
   * 
   * @param registration
   * @param deviceZone
   * @param device_status_schedule the logical schedule at which to call the device status api
   */
  private void checkDeviceStatusApi(DrsRegistration registration, DateTimeZone deviceZone, String device_status_schedule) {

    final Date now = new Date();
    final long device_ID = registration.device_ID;

    Date last_device_status_at = new Date(0); // for first time around
    if (registration.last_device_status_at!=null)
      last_device_status_at = new DateTime(registration.last_device_status_at).toDate();
  
    CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(device_status_schedule, deviceZone.toTimeZone());
    Date next_device_status_at = cronSequenceGenerator.next(last_device_status_at);
    // time to call amazon drs device status api?
    if (next_device_status_at.compareTo(now) < 0) {
      // yes!
      scheduleOnce(new DeferredDeviceStatusApi(device_ID));
      registration.last_device_status_at = now;
    }
  }

  /**
   * runDrs
   * 
   * for cronJob
   * 
   * kick off "process registrations" process
   * 
   */
  public TaskHandle runDrs() {
    return scheduleOnce(new RunDrsIncremental(0)); // start w/device_ID=0
  }

  /**
   * runDrsIncremental
   * 
   * processRegistrationsIncremental
   * 
   * for taskQueue
   * 
   * @param last_device_ID
   */
  public void processRegistrationsIncremental(long last_device_ID) {
    
    log.info(Log.str("processRegistrationsIncremental", last_device_ID));

    final String kind = DrsRegistration.class.getSimpleName();

    // get next entities
    Query query = new Query(kind).setKeysOnly();
    if (last_device_ID!=0)
        query.setFilter(Query.FilterOperator.GREATER_THAN.of("__key__", KeyFactory.createKey(kind, last_device_ID)));
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    List<Entity> entityList = ds.prepare(query).asList(fetchOptions);
    for (Entity entity : entityList) {
      long device_ID = entity.getKey().getId();
        try {
          processRegistration(device_ID);
        } catch (Exception e) {
          log.severe(Log.str(device_ID, e));
        }
    }

    // kick off next batch of work
    if (entityList.size()>0)
      scheduleOnce(new RunDrsIncremental(Iterables.getLast(entityList).getKey().getId()));

  }

  private boolean has(String s) {
    return Strings.nullToEmpty(s).length() > 0;
  }

  private final Logger log = Logger.getLogger(this.getClass().getName());
}
