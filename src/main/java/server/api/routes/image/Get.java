package server.api.routes.image;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import protocol.ResponseObject;
import protocol.image.Protocol;
import server.image.ImageVerticle;

import java.util.Map;

public class Get {
    public Get(ImageVerticle imageVerticle) {
        final String fRoot = imageVerticle.getRoot();
        imageVerticle.getRouter().route(HttpMethod.POST, Protocol.Path.GET.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            System.out.println(fRoot);

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();

                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
