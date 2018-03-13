package server.api.routes.image;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.image.Protocol;
import server.image.ImageVerticle;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.TimeZone;

public class GenerateTemporaryURL {
    public GenerateTemporaryURL(ImageVerticle imageVerticle) {
        imageVerticle.getRouter().route(HttpMethod.POST, Protocol.Path.GENERATE_TEMPORARY_URL.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rPictureId = (String) received.get(Protocol.Field.PICUTRE_ID.key);

                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rPictureId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.PICUTRE_ID.key);
                    break label;
                }
                if (!rToken.equals(imageVerticle.getToken())) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Bad token");
                    break label;
                }
                String temporaryUrl = new ObjectId().toString();
                imageVerticle.getUrls().put(temporaryUrl, rPictureId);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.TEMPORARY_URL.key, temporaryUrl);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}