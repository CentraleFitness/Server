package server.api.routes.image;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import protocol.ResponseObject;
import protocol.image.Protocol;
import server.image.ImageVerticle;

import java.util.Map;

public class Delete {
    public Delete(ImageVerticle imageVerticle) {
        imageVerticle.getRouter().route(HttpMethod.POST, Protocol.Path.DELETE.path).handler(routingContext -> {

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
                FileUtils.deleteQuietly(FileUtils.getFile(imageVerticle.getRoot() + "/" + FilenameUtils.getBaseName(rPictureId)));
                sending = new ResponseObject(false);
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
