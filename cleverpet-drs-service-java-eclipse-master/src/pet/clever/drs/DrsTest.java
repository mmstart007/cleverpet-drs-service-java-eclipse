package pet.clever.drs;

import com.sun.jersey.api.client.*;

public class DrsTest {
  static Client httpClient = Client.create();
  static String base_url = "http://localhost:8888/api";

  public static void main(String[] args) throws Exception {

    httpClient.resource(String.format("%s%s", base_url, "/client/drs/registrations/1029384756/refresh_token")).post("Uim1bu6Aengowahph4looWuedahmuCai1shahquob4poohahGeelephaic3Ec7zu7Xoingeen5uvaiSah9Phohwainaif2kaeph4Tei4aYa0ixaepeeGaiz9aegaixeiPov9pheeZaiZeh1tha0oMie6yiesaeth9sie5ooh5ha9maXaeShaeshuchae4Ilahgoh1Aev8xeis3ThohtoPhohCahHai9eicheh6Nee9AishieNgeiri0aib");

    DeviceRegisteredNotification deviceRegistered = new DeviceRegisteredNotification();
//    httpClient.resource(String.format("%s%s", base_url, "/service/drs/handle_sns"));
  }
}
