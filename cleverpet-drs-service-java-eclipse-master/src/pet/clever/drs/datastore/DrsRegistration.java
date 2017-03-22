package pet.clever.drs.datastore;

import java.util.*;

import com.google.common.collect.*;

/**
 * 
 * @author rrizun
 *
 */
public class DrsRegistration {

  @KeyField
  public long device_ID; // same value as device_ID
  
  /**
   * delta kibbles
   * 
   * decremented on "interaction record" events
   * incremented on "item shipped" notifications
   * 
   * read-only from client perspective
   * 
   * NOTE- interaction record events *decrement* "kibbles" but *increment* "kibbles_average"
   */
  public int kibbles; // aka remainingQuantityInUnit
  
  /** used to calc expectedReplenishmentDate */
  public int kibbles_per_day = -1; // synthetic field not in datastore

  /**
   * current kibbles + 7 day sliding average
   * index 0 contains the current daily count
   * index 1..7 contains the previous daily counts
   * 1. inbound interaction records increment index 0
   * 2. so e.g. if u want the 7 day moving average then sum indexes 1..7 and divide by 7
   * 
   * NOTE- interaction record events *decrement* "kibbles" but *increment* "kibbles_average"
   */
  public final List<Integer> kibbles_average = Lists.newArrayList();

  // // see also: DrsItem.count_per_bag
  // public int remaining; // e.g., 456 //###TODO SO THIS IS A CONCEPT THAT WE
  // (CLEVERPET) MADE UP

  // public Date next_refill_expected;

  // status

//  // true if this registration is registered w/amazon drs
//  // registered is effectively "does amazon know about you?"
//  // updated via amazon drs notifications
//  public boolean registered;

  // iff registered==true
  // true if this registration subscription is enabled wrt amazon drs
  // updated via amazon drs notifications
  public boolean subscribed;

  // ORDER_INPROGRESS
  // true after successful "order_placed" notification
  // false after successful "order canceled" or "item shipped" notification
  //
  // the idea is the "cleverpet drs service"
  // can use this flag to know whether to even bother to call replenish
  //
  // updated via amazon drs notifications
  public boolean order_inprogress;
  
  /** last time average sliding windows was processed */
  public Date last_averaged_at; // read-only from client perspective
  
  /** last time replenish was checked */
  public Date last_replenish_at;
  public String last_replenish_response;

  // last time amazon drs slot status api was called
  public Date last_slot_status_at;
  
  // last time amazon drs device status api was called
  public Date last_device_status_at;
  
  /**
   * expected date when cleverpet drs service will place order
   * 
   * e.g., something like expected empty date minus seven days
   */
  public Date expectedReplenishmentDate;  // synthetic field not in datastore
//  public int remainingQuantityInUnit; // aka "kibbles" delta
  public int originalQuantityInUnit; // from order placed/item shipped notification
//  public double totalQuantityOnHand; // if known
  public Date lastUseDate;  
}
