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
import org.apache.commons.lang3.text.WordUtils;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.Map;
import java.util.Objects;

public class CreateAccount {

    /**
     * HTTP POST
     * @path: REGISTRATION:
     * @param: EMAIL, PASSWORD, FIRSTNAME, LASTNAME
     * @return: STATUS, TOKEN
     */

    public CreateAccount(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.ACCOUNT.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.PHONE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PHONE.code);

                } else if (received.get(Protocol.Field.EMAIL.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL.code);

                } else if (received.get(Protocol.Field.PASSWORD.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);

                } else if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_FIRSTNAME.code);

                } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LASTNAME.code);

                } else if (Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, received.get(Protocol.Field.EMAIL.key)) == null) {

                    Long time = System.currentTimeMillis();

                    admin = (Administrator) Database.new_entity(Database.Collections.Administrators);
                    admin.setField(Administrator.Field.FIRSTNAME, WordUtils.capitalize((String)received.get(Protocol.Field.FIRSTNAME.key)));
                    admin.setField(Administrator.Field.LASTNAME, ((String)received.get(Protocol.Field.LASTNAME.key)).toUpperCase());
                    admin.setField(Administrator.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                    admin.setField(Administrator.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                    admin.setField(Administrator.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                    admin.setField(Administrator.Field.TOKEN, new Token((String) received.get(Protocol.Field.EMAIL.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    admin.setField(Administrator.Field.CREATION_DATE, time);
                    admin.setField(Administrator.Field.UPDATE_DATE, time);

                    Database.update_entity(Database.Collections.Administrators, admin);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Protocol.Field.ADMINISTRATOR_ID.key, admin.getField(Administrator.Field.ID));
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL_TAKEN.code);
                    LogManager.write("Email already taken");
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
