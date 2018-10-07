package server.api.routes.admin;

import Tools.LogManager;
import Tools.PasswordAuthentication;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.Map;

public class AuthenticationWithCredentials {
    public AuthenticationWithCredentials(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                if ((admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, received.get(Protocol.Field.EMAIL.key))) != null) {
                    if (new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), (String) admin.getField(Administrator.Field.PASSWORD_HASH))) {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                        admin.setField(Administrator.Field.TOKEN, new Token((String) received.get(Protocol.Field.EMAIL.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                        sending.put(Protocol.Field.TOKEN.key, (String) admin.getField(Administrator.Field.TOKEN));
                        Database.update_entity(Database.Collections.Administrators, admin);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                    }
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
