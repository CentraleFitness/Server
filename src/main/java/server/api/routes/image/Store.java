package server.api.routes.image;

import Tools.CompressionUtils;
import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.image.Protocol;
import server.image.ImageVerticle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Map;

public class Store {
    public Store(ImageVerticle imageVerticle) {
        imageVerticle.getRouter().route(HttpMethod.POST, Protocol.Path.STORE.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rPicture = (String) received.get(Protocol.Field.B64_PICTURE.key);

                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rPicture == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.B64_PICTURE.key);
                    break label;
                }
                if (!rToken.equals(imageVerticle.getToken())) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Bad token");
                    break label;
                }
                byte[] rawPicture = Base64.getDecoder().decode(rPicture);
                byte[] compressedPicture = CompressionUtils.compress(rawPicture);
                ObjectId pictureId = new ObjectId();
                FileUtils.writeByteArrayToFile(new File(imageVerticle.getRoot() + "/" + pictureId.toString()), compressedPicture);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.PICUTRE_ID.key, pictureId.toString());
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
