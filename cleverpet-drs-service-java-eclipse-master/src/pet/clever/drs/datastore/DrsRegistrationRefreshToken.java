package pet.clever.drs.datastore;

public class DrsRegistrationRefreshToken {
  @KeyField
  public long device_ID; // same value as device_ID
  public String client_id;
  public String refresh_token;
//  public DrsRegistrationRefreshToken(long device_ID, String client_id, String refresh_token) {
//    this.device_ID = device_ID;
//    this.client_id = client_id;
//    this.refresh_token = refresh_token;
//  }
}
