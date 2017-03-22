package pet.clever.drs;

import java.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.gson.*;

/**
 * 
 * @author rrizun
 *
 */
public class Log {

  public static String str(Object... args) {
    List<String> list = Lists.newArrayList();
    for (Object arg : args) {
      if (arg instanceof Throwable) {
        list.add(Throwables.getStackTraceAsString((Throwable) arg));
      } else {
        String message = "" + arg;
        JsonElement jsonElement = gson.toJsonTree(arg);
        if (jsonElement.isJsonObject())
          message = gson.toJson(jsonElement);
        list.add(message);
      }
    }
    return Joiner.on(" ").join(list);
  }
  
  static Gson gson = new GsonBuilder().setPrettyPrinting().create();

}
