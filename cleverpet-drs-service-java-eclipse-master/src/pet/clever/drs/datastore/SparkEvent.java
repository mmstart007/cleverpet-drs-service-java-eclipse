package pet.clever.drs.datastore;

import java.util.*;

public class SparkEvent {

//  event=spark%2Fstatus&data=online&published_at=2016-08-26T22%3A10%3A50.808Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fdevice%2Fapp-hash&data=10111D2DC19A0DDE03A057C58236AD8B1C67449B5C7AF72ECB3962D973148783&published_at=2016-08-26T22%3A10%3A50.869Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fstatus&data=auto-update&published_at=2016-08-26T22%3A10%3A55.922Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fflash%2Fstatus&data=started%20&published_at=2016-08-26T22%3A10%3A57.090Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fflash%2Fstatus&data=success%20&published_at=2016-08-26T22%3A11%3A18.313Z&coreid=330024000f47343432313031&fw_version=21
//      event=spark%2Fstatus&data=online&published_at=2016-08-26T22%3A11%3A22.870Z&coreid=330024000f47343432313031&userid=57c08e6259a150e64b34c313&fw_version=21
  
  public String event;
  public String data;
  public Date published_at;
  public String coreid;
  public String fw_version;
}
