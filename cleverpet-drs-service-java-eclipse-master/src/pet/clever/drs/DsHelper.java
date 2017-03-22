package pet.clever.drs;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.gson.*;

import pet.clever.drs.datastore.*;

/**
 *
 * @author rrizun
 *
 */
public class DsHelper {

  private final DatastoreService ds;

  /**
   * ctor
   * 
   * @param ds
   */
  public DsHelper(DatastoreService ds) {
    this.ds = ds;
  }

  /**
   * kind
   * 
   * @param classOfT
   * @return
   */
  private String kind(Class<?> classOfT) {
    String kind = classOfT.getSimpleName();
    if (kind.endsWith("Entity"))
      kind = kind.substring(0, kind.length() - "Entity".length());
    return kind;
  }

  /**
   * get
   *
   * returns null if entity not found
   * 
   * @param classOfT
   * @param id
   * @param <T>
   * @return
   * @throws Exception
   */
  public <T> T get(final Class<T> classOfT, long id) {
    Key key = KeyFactory.createKey(classOfT.getSimpleName(), id);
    return Maps.transformValues(ds.get(ImmutableList.of(key)), new Function<Entity, T>() {
      @Override
      public T apply(Entity input) {
        return parse(input, classOfT);
      }
    }).get(key);
  }

  /**
   * get
   *
   * returns null if entity not found
   * 
   * @param classOfT
   * @param name
   * @param <T>
   * @return
   * @throws Exception
   */
  public <T> T get(final Class<T> classOfT, String name) {
    Key key = KeyFactory.createKey(classOfT.getSimpleName(), name);
    return Maps.transformValues(ds.get(ImmutableList.of(key)), new Function<Entity, T>() {
      @Override
      public T apply(Entity input) {
        return parse(input, classOfT);
      }
    }).get(key);
  }

  /**
   * write
   * 
   * @param src
   */
  public void write(Object src) {
    String kind = src.getClass().getSimpleName();
    log.info(Log.str("write", kind, src));
    ds.put(render(src));
  }
  
  /**
   * delete
   * 
   * @param classOfT
   * @param name
   */
  public <T> void delete(final Class<T> classOfT, long id) {
    ds.delete(KeyFactory.createKey(classOfT.getSimpleName(), id));
  }
  
  /**
   * delete
   * 
   * @param classOfT
   * @param name
   */
  public <T> void delete(final Class<T> classOfT, String name) {
    ds.delete(KeyFactory.createKey(classOfT.getSimpleName(), name));
  }

  /**
   * parse
   * 
   * @param entity
   * @param classOfT
   * @return
   */
  private <T> T parse(Entity entity, Class<T> classOfT) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("key", KeyFactory.keyToString(entity.getKey()));
    for (Field field : classOfT.getDeclaredFields()) {
      KeyField keyField = field.getAnnotation(KeyField.class);
      if (keyField != null) {
        if (entity.getKey().getId() != 0)
          jsonObject.addProperty(field.getName(), entity.getKey().getId());
        else
          jsonObject.addProperty(field.getName(), entity.getKey().getName());
      }
    }
    for (String key : entity.getProperties().keySet()) {
      Object object = entity.getProperties().get(key);
      if (object instanceof com.google.appengine.api.datastore.Text)
        object = ((com.google.appengine.api.datastore.Text) object).getValue();
      if (object != null)
        jsonObject.add(key, new Gson().toJsonTree(object));
    }
    return new Gson().fromJson(jsonObject, classOfT);
  }
  
  /**
   * render
   * 
   * @param src
   * @return
   */
  private Entity render(Object src) {
    String kind = src.getClass().getSimpleName();
    Entity entity = new Entity(kind);
    JsonObject jsonObject = new Gson().toJsonTree(src).getAsJsonObject();
    for (Field field : src.getClass().getDeclaredFields()) {
      KeyField keyField = field.getAnnotation(KeyField.class);
      if (keyField != null) {
        JsonPrimitive jsonPrimitive = jsonObject.remove(field.getName()).getAsJsonPrimitive();
        if (jsonPrimitive.isNumber())
          entity = new Entity(kind, jsonPrimitive.getAsLong());
        else
          entity = new Entity(kind, jsonPrimitive.getAsString());
      }
    }
    for (Field field : src.getClass().getDeclaredFields()) {
      JsonElement jsonElement = jsonObject.get(field.getName());
      if (jsonElement != null)
        entity.setProperty(field.getName(), new Gson().fromJson(jsonElement, field.getGenericType()));
    }
    return entity;
  }

  class DsQuery<T> {
    private final Class<T> classOfT;
    private Query.Filter filter;
    private String propertyName;
    private SortDirection sortDirection;
    public DsQuery(Class<T> classOfT) {
      this.classOfT = classOfT;
    }
    public DsQuery<T> filter(Query.Filter filter) {
      this.filter = filter;
      return this;
    }
    public DsQuery<T> sort(String propertyName, SortDirection sortDirection) {
      this.propertyName = propertyName;
      this.sortDirection = sortDirection;
      return this;
    }
    public List<T> list() {
      String kind = null;
      if (kind == null)
        kind = classOfT.getSimpleName();
      Query query = new Query(kind);
      if (filter != null)
        query.setFilter(filter);
      if (propertyName != null)
        query.addSort(propertyName, sortDirection);
      return Lists.transform(ds.prepare(query).asList(FetchOptions.Builder.withDefaults()/*.limit(limit)*/), new Function<Entity, T>() {
        @Override
        public T apply(Entity entity) {
          return parse(entity, classOfT);
        }
      });
    }
  }
  
  // new DsHelper(ds).query(User.class).list();
  public <T> DsQuery<T> query(Class<T> classOfT) {
    return new DsQuery<T>(classOfT);
  }

  /**
   * readModifyWrite
   * 
   * @param classOfT
   * @param id
   * @param readModifyWrite
   */
  public <T> T readModifyWrite(Class<T> classOfT, long id, ReadModifyWrite<T> readModifyWrite) {
//    String kind = kind(classOfT);
    // begin read-modify-write transaction
    final Transaction txn = ds.beginTransaction();
    try {
      T object = new DsHelper(ds).get(classOfT, id);
      readModifyWrite.modify(object);
      new DsHelper(ds).write(object);
      txn.commit();
      return object;
    } finally {
      if (txn.isActive())
        txn.rollback();
    }
  }

  private final Logger log = Logger.getLogger(this.getClass().getName());
}