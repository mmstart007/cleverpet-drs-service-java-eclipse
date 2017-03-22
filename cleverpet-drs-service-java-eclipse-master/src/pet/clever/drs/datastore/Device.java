package pet.clever.drs.datastore;

import java.util.*;

public class Device {
  @KeyField
  public long device_ID;
  public Date created;
  public Date last_modified;
  public int time_zone; // e.g., -25200
}
