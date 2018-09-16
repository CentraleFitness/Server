package server.api.routes.mobile.customprogram;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

import Tools.LogManager;
import Tools.Token;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.Database.Collections;
import model.entities.CustomProgram;
import model.entities.Fitness_Center;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class CustomProgramGetRange {
    public CustomProgramGetRange(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CUSTOMPROGRAM_GET_RANGE.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rSportCenterId = (String) received.get(Protocol.Field.SPORTCENTERID.key);
                Integer rStart = (Integer) received.get(Protocol.Field.START.key);
                Integer rEnd = (Integer) received.get(Protocol.Field.END.key);
                Map rFilters = (Map) received.get(Protocol.Field.FILTERS.key);

                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    return;
                }
                if (rStart == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.START.key);
                    return;
                }
                if (rEnd == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.END.key);
                    return;
                }
                if (rSportCenterId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.SPORTCENTERID.key);
                    return;
                }
                if (rFilters == null) rFilters = new TreeMap<String, String>();
                JWT token = Token.decodeToken(rToken);
                if (token == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    return;
                }
                User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, token.getIssuer());
                if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    return;
                }
                ObjectId fitnessCenterId = new ObjectId(rSportCenterId);
				Fitness_Center fitness_Center = (Fitness_Center) Database.find_entity(Collections.Fitness_Centers,
						Fitness_Center.Field.ID, fitnessCenterId);
                if (fitness_Center == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.CENTER_NOT_FOUND.code);
                    LogManager.write(Protocol.Status.CENTER_NOT_FOUND.message);
                    return;
                }
                @SuppressWarnings("unchecked")
				List<Object> customProgramsList =
                        (List<Object>) Database
                                .collections
                                .get(Database.Collections.CustomPrograms)
                                .find(eq(CustomProgram.Field.FITNESS_CENTER_ID.get_key(), fitnessCenterId))
                                .sort(new BasicDBObject(CustomProgram.Field.CREATION_DATE.get_key(), 1))
                                //.filter(eq(CustomProgram.Field.NAME.get_key(), rFilters.get("name")))
                                .skip(rStart)
                                .limit(rEnd)
                                .into(new ArrayList())
                                .stream()
                                .map(doc -> {
                                    CustomProgram customProgram = new CustomProgram((Document) doc);
                                    Map toRet = new TreeMap<>();
                                    toRet.put(Protocol.Field.CUSTOMPROGRAMID.key, customProgram.getField(CustomProgram.Field.ID));
                                    toRet.put(Protocol.Field.NAME.key, customProgram.getField(CustomProgram.Field.NAME));
                                    return toRet;
                                })
                        .collect(Collectors.toList());
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                sending.put(Protocol.Field.CUSTOMPROGRAMS.key, customProgramsList);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            } finally {
                response.end(new GsonBuilder().create().toJson(sending));
            }
        });
    }
}
