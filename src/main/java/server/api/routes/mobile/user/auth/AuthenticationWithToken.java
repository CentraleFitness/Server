package server.api.routes.mobile.user.auth;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.mobile.Protocol;
import protocol.ResponseObject;
import Tools.Token;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AuthenticationWithToken {
	public AuthenticationWithToken(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION_TOKEN.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
			User user;

			try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);

				user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN,
						Token.decodeToken(rToken).getIssuer());
				if (!Objects.equals(user.getField(User.Field.TOKEN), rToken)) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
					return;
				}
				if (!Optional.ofNullable(user.getField(User.Field.IS_ACTIVE)).map(ff -> (Boolean)ff).orElse(false)) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_INNACTIVE.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_INNACTIVE.message);
					return;
				}
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
			} catch (Exception e) {
				sending = new ResponseObject(true);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
				LogManager.write(e);
			} finally {
				response.end(new GsonBuilder().create().toJson(sending));
			}
		});
	}
}
