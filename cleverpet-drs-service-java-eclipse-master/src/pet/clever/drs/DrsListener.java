package pet.clever.drs;

import java.lang.reflect.*;

import javax.servlet.*;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.*;

/**
 * 
 * @author rrizun
 *
 */
public class DrsListener implements ServletContextListener {

  public static final Client httpClient = Client.create(new DefaultClientConfig(GsonProvider.class));
//  public static final DrsSdk drsSdk = new DrsSdk(httpClient);
  public static final DrsService drsService = new DrsService(httpClient);

  @Override
  public void contextInitialized(ServletContextEvent event) {
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }
}
