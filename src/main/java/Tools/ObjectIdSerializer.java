package Tools;

import com.google.gson.*;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;

public class ObjectIdSerializer implements JsonSerializer<ObjectId> {
    @Override
    public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
