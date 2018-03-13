package server.api.routes.image;

import Tools.CompressionUtils;
import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.apache.commons.io.FileUtils;
import protocol.ResponseObject;
import protocol.image.Protocol;
import server.image.ImageVerticle;

import java.io.File;
import java.util.Base64;
import java.util.Map;

public class Get {
    public Get(ImageVerticle imageVerticle) {
        imageVerticle.getRouter().route(HttpMethod.GET, Protocol.Path.GET.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                String param = routingContext.pathParam("param");
                if (param == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing param " + Protocol.Field.TEMPORARY_URL.key);
                    break label;
                }
                String pictureId = imageVerticle.getUrls().getIfPresent(param);
                if (pictureId== null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Bad Get param");
                    break label;
                }
                byte[] compressedPicture = FileUtils.readFileToByteArray(new File(imageVerticle.getRoot() + "/" + pictureId));
                byte[] rawPicture = CompressionUtils.decompress(compressedPicture);
                String b64Picture = Base64.getEncoder().encodeToString(rawPicture);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.B64_PICTURE.key, b64Picture);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().disableHtmlEscaping().create().toJson(sending));
        });
    }
}
