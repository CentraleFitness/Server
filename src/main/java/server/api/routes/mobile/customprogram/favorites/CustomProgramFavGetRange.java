package server.api.routes.mobile.customprogram.favorites;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
import model.entities.CustomProgram;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class CustomProgramFavGetRange {
	public CustomProgramFavGetRange(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.CUSTOMPROGRAM_FAV_GET_RANGE.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

			label: try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);

				if (rToken == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
					break label;
				}
				JWT token = Token.decodeToken(rToken);
				if (token == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
					break label;
				}
				User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN,
						token.getIssuer());
				if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
					break label;
				}
				HashSet<ObjectId> favorites = (HashSet) user.getField(User.Field.FAVORITES_CUSTOM_PROGRAMS);
				if (favorites == null) {
					favorites = new HashSet();
					user.setField(User.Field.FAVORITES_CUSTOM_PROGRAMS, favorites);
					Database.update_entity(Collections.Users, user);
				}
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
				sending.put(Protocol.Field.CUSTOMPROGRAMID.key, favorites.stream().map(id -> {
					TreeMap cp = null;
					try {
						CustomProgram customProgram = (CustomProgram) Database.find_entity(Collections.CustomPrograms,
								CustomProgram.Field.ID, id);
						if (customProgram == null) return null;
							cp = new TreeMap<>();
							cp.put(Protocol.Field.CUSTOMPROGRAMID, id);
							cp.put(Protocol.Field.NAME, customProgram.getField(CustomProgram.Field.NAME));
						
					} catch (InvocationTargetException | NoSuchMethodException | InstantiationException
							| IllegalAccessException e) {
					}
					return cp;
				}).collect(Collectors.toList()));
			} catch (Exception e) {
				sending = new ResponseObject(true);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
				LogManager.write(e);
			}
			response.end(new GsonBuilder().create().toJson(sending));
		});
	}
}
