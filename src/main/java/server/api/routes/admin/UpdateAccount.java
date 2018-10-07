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
import java.util.Objects;

public class UpdateAccount {
    public UpdateAccount(Router router) {
        router.route(HttpMethod.PUT, Protocol.Path.ACCOUNT.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.ADMINISTRATOR_ID.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.EMAIL.key) == null) {
                    LogManager.write("Missing email field");
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL.code);

                } else if (received.get(Protocol.Field.PASSWORD.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);
                    LogManager.write("Missing password field");

                } else if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_FIRSTNAME.code);
                    LogManager.write("Missing first name field");

                } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LASTNAME.code);
                    LogManager.write("Missing last name field");

                } else {

                    Long time = System.currentTimeMillis();

                    admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.ID, received.get(Protocol.Field.ADMINISTRATOR_ID.key));
                    admin.setField(Administrator.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME.key));
                    admin.setField(Administrator.Field.LASTNAME, received.get(Protocol.Field.LASTNAME.key));
                    admin.setField(Administrator.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                    admin.setField(Administrator.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                    admin.setField(Administrator.Field.TOKEN, new Token((String) received.get(Protocol.Field.EMAIL.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    admin.setField(Administrator.Field.UPDATE_DATE, time);

                    Database.update_entity(Database.Collections.Administrators, admin);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
