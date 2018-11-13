package server.api.routes.mobile.sportsession;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.Database.Collections;
import model.entities.ElectricProduction;
import model.entities.SportSession;
import model.entities.User;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

import java.util.ArrayList;
import java.util.Map;

public class UserGetTotalproduction {
	public UserGetTotalproduction(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.USER_GET_TOTALPRODUCTION.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
			LogManager.write("UserGetTotalProduction {");
			try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);
				if (rToken == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
					return;
				}
				User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN,
						Token.decodeToken(rToken).getIssuer());
				if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
					LogManager.write("Bad token");
					return;
				}
				ObjectId user_id = (ObjectId) user.getField(User.Field.ID);
				LogManager.write("\telectric productions {");
				Double production = Database.find_entities(Collections.ElectricProductions, ElectricProduction.Field.USER_ID, user_id)
						.stream().map(entity -> (double)((ElectricProduction) entity)
								.getField(ElectricProduction.Field.PRODUCTION_TOTAL)).peek(prod -> LogManager.write("\t"+prod.toString() + " ")).reduce((tt, cc) -> tt + cc).orElse(0D);
				LogManager.write("\t}");
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.PRODUCTION.key, production);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
			} catch (Exception e) {
				sending = new ResponseObject(true);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
				LogManager.write(e);
			} finally {
				LogManager.write("}");
				response.end(new GsonBuilder().create().toJson(sending));
			}
		});
	}
}
