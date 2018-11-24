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
import Tools.PasswordAuthentication;
import Tools.Token;

import java.util.Map;
import java.util.Optional;

public class AuthenticationWithCredentials {
	public AuthenticationWithCredentials(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
			User user;

			try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rLogin = (String) received.get(Protocol.Field.LOGIN.key);
				String rPassword = (String) received.get(Protocol.Field.PASSWORD.key);

				if ((user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN,
						rLogin)) == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_CREDENTIALS.message);
					return;
				}
				if (!new PasswordAuthentication().authenticate((rPassword).toCharArray(),
						(String) user.getField(User.Field.PASSWORD_HASH))) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_CREDENTIALS.message);
					return;
				}
				if (!Optional.ofNullable(user.getField(User.Field.IS_ACTIVE)).map(ff -> (Boolean) ff).orElse(false)) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_INNACTIVE.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_INNACTIVE.message);
					return;
				}				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
				user.setField(User.Field.TOKEN, new Token(rLogin, rPassword).generate());
				sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
				Database.update_entity(Database.Collections.Users, user);
			} catch (Exception e) {
				sending = new ResponseObject(true);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
				LogManager.write(e);
			} finally {
				response.end(new GsonBuilder().create().toJson(sending));
			}
		});
	}
}
