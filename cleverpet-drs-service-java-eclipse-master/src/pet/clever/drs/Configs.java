package pet.clever.drs;

/**
 * type-safe config access
 */
public class Configs {
  
//  /**
//   * the default amount of kibbles when a subscription is enabled
//   */
//  public int drs_kibbles_default = 10;

  /**
   * logical cron expression for how often to average drs registrations
   * 
   * must be daily, e.g., at midnight, or at 4am, etc..
   */
  public String drs_average_schedule = "0 0 0 * * *";
  
  /**
   * logical cron expression for how often to check for replenish
   * 
   * can be one hour.. can be one day.. whatever makes sense for teh biz
   * 
   * if one hour then, e.g., enable subscription will trigger first-bottle reorder within the hour
   * if one day then, e.g., enable subscription will trigger first-bottle reorder at the end of the day
   */
  public String drs_replenish_schedule = "0 0 0 * * *";
  
  public String drs_slot_status_schedule = "0 0 * * * *"; // hourly
  public String drs_device_status_schedule = "0 0 0 * * *"; // at midnight

//  public int drs_process_registration_batch_limit = 1;
  
  public int drs_kibbles_per_pound = 100;
  
  /**
   * the minimum amount of daily kibble consumption
   * 
   * e.g., even if, e.g., zero kibbles were consumed in a day,
   * 10 kibbles consumed will still be reported
   */
  public int drs_kibbles_per_day_minimum = 10;
}
