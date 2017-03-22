package pet.clever.drs;

import com.google.appengine.api.*;
import com.google.appengine.api.datastore.*;
import com.google.common.base.*;
import com.sun.jersey.spi.container.*;

import pet.clever.drs.datastore.*;

public class NamespaceFilter implements ContainerRequestFilter {
  static Supplier<Config> configSupplier = Suppliers.memoize(new Supplier<Config>() {
    @Override
    public Config get() {
      return new DsHelper(DatastoreServiceFactory.getDatastoreService()).get(Config.class, "datastore_namespace");
    }
  });
  @Override
  public ContainerRequest filter(ContainerRequest request) {
    Config config = configSupplier.get();
    if (config != null)
      NamespaceManager.set(config.value);
    return request;
  }
}
