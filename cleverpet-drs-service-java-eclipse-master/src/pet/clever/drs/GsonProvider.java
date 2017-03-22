package pet.clever.drs;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.google.gson.*;
import com.sun.jersey.api.representation.*;
import com.sun.jersey.core.provider.*;

@Provider
@Produces("application/json")
public class GsonProvider extends AbstractMessageReaderWriterProvider<Object> {
    static Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return new JsonPrimitive(iso8601Format.format(src));
        }
    }).setPrettyPrinting().create();

    @Override
    public boolean isReadable(Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType) {
        return !Form.class.isAssignableFrom(cls) && !InputStream.class.isAssignableFrom(cls);
    }

    @Override
    public Object readFrom(Class<Object> cls, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in) throws IOException, WebApplicationException {
        return gson.fromJson(new InputStreamReader(in), cls);
    }

    @Override
    public boolean isWriteable(Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType) {
        return !Form.class.isAssignableFrom(cls) && !InputStream.class.isAssignableFrom(cls);
    }

    @Override
    public void writeTo(Object object, Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException, WebApplicationException {
        new PrintWriter(out, true).println(gson.toJson(object));
    }
}