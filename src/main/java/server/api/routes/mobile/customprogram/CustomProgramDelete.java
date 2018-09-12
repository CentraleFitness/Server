package server.api.routes.mobile.customprogram;

import java.util.Map;

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

public class CustomProgramDelete {
	public CustomProgramDelete(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.CUSTOMPROGRAM_DELETE.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

			try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);
				String rCustomProgramId = (String) received.get(Protocol.Field.CUSTOMPROGRAMID.key);

				if (rToken == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
					return;
				}
				if (rCustomProgramId == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.CUSTOMPROGRAMID.key);
					return;
				}
				JWT token = Token.decodeToken(rToken);
				if (token == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
					return;
				}
				User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN,
						token.getIssuer());
				if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
					LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
					return;
				}
				ObjectId customProgramId = new ObjectId(rCustomProgramId);
				CustomProgram customProgram;
				if (customProgramId == null || (customProgram = (CustomProgram) Database
						.find_entity(Collections.CustomPrograms, CustomProgram.Field.ID, customProgramId)) == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.CUSTOM_PROGRAM_NOT_FOUND.code);
					LogManager.write(Protocol.Status.CUSTOM_PROGRAM_NOT_FOUND.message);
					return;
				}
				if (!customProgram.getField(CustomProgram.Field.ID).toString()
						.equals(user.getField(User.Field.ID).toString())) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.CUSTOM_PROGRAM_NOT_OWNER.code);
					LogManager.write(Protocol.Status.CUSTOM_PROGRAM_NOT_OWNER.message);
					return;
				}
				Database.delete_entity(Collections.CustomPrograms, CustomProgram.Field.ID, customProgramId);
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
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
