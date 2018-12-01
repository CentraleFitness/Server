package server.api.routes.admin.user;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Fitness_Center_Manager;
import model.entities.User;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.Map;
import java.util.Objects;

public class SetUserAccountActivity {
    public SetUserAccountActivity(Router router) {
        router.route(HttpMethod.PUT, Protocol.Path.USER_ACTIVITY.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.USER_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.IS_ACTIVE.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    User user = (User) Database.find_entity(Database.Collections.Users, User.Field.ID, new ObjectId((String)received.get(Protocol.Field.USER_ID.key)));

                    Long time = System.currentTimeMillis();

                    if (!(Boolean)received.get(Protocol.Field.IS_ACTIVE.key)) {
                        user.setField(Fitness_Center_Manager.Field.TOKEN, "");
                    }

                    user.setField(Fitness_Center_Manager.Field.IS_ACTIVE, received.get(Protocol.Field.IS_ACTIVE.key));
                    user.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ACTIVITY, time);
                    user.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_ID, admin.getField(Administrator.Field.ID));

                    Database.update_entity(Database.Collections.Users, user);

                    sending.put(Protocol.Field.ADMINISTRATOR_ID.key, admin.getField(Administrator.Field.ID));
                    sending.put(Protocol.Field.ADMINISTRATOR_NAME.key, admin.getField(Administrator.Field.FIRSTNAME) + " " + admin.getField(Administrator.Field.LASTNAME));
                }

            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
