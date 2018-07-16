package server.api.routes.mobile.user.profile;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;

import Tools.LogManager;
import Tools.Token;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.Database.Collections;
import model.entities.Fitness_Center;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class Affiliate {
    public Affiliate(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.AFFILIATE.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rAffiliationToken = (String) received.get(Protocol.Field.AFFILIATIONTOKEN.key);
                
                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rAffiliationToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.AFFILIATIONTOKEN.key);
                    break label;
                }
                JWT token = Token.decodeToken(rToken);
                if (token == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    break label;
                }
                User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, token.getIssuer());
                if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    break label;
                }
                ObjectId fitnessCenterId = new ObjectId(rAffiliationToken);
                Fitness_Center fitness_Center = (Fitness_Center) Database.find_entity(Collections.Fitness_Centers, Fitness_Center.Field.ID, fitnessCenterId);
                if (fitness_Center == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.CENTER_NOT_FOUND.code);
                    LogManager.write(Protocol.Status.CENTER_NOT_FOUND.message);
                    break label;
                }
                user.setField(User.Field.FITNESS_CENTER_ID, fitnessCenterId);
                Database.update_entity(Collections.Users, user);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                sending.put(Protocol.Field.SPORTCENTERID.key, fitness_Center.getField(Fitness_Center.Field.ID));
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
