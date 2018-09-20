package server.api.routes.mobile.customprogram;

import java.util.Date;
import java.util.List;
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
import model.entities.Fitness_Center;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class CustomProgramCreate {
	public CustomProgramCreate(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.CUSTOMPROGRAM_CREATE.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

			try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);
				String rName = (String) received.get(Protocol.Field.NAME.key);
				String rLogo = (String) received.get(Protocol.Field.LOGO.key);
				Long rNBSteps = (Long) received.get(Protocol.Field.NBSTEPS.key);
				List rSteps = (List) received.get(Protocol.Field.STEPS.key);
				Long rDuration = (Long) received.get(Protocol.Field.DURATION.key);

				if (rToken == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
					return;
				}
				if (rName == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.NAME.key);
					return;
				}
				if (rLogo == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.LOGO.key);
					return;
				}
				if (rNBSteps == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.NBSTEPS.key);
					return;
				}
				if (rSteps == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.STEPS.key);
					return;
				}
				if (rDuration == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.DURATION.key);
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
				ObjectId fitnessCenterId = new ObjectId((String) user.getField(User.Field.FITNESS_CENTER_ID));
				Fitness_Center fitness_Center;
				if (fitnessCenterId == null || (fitness_Center = (Fitness_Center) Database
						.find_entity(Collections.Fitness_Centers, Fitness_Center.Field.ID, fitnessCenterId)) == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.CENTER_NOT_FOUND.code);
					LogManager.write(Protocol.Status.CENTER_NOT_FOUND.message);
					return;
				}
				CustomProgram customProgram = (CustomProgram) Database.new_entity(Collections.CustomPrograms);
				customProgram.setField(CustomProgram.Field.FITNESS_CENTER_ID, fitness_Center);
				customProgram.setField(CustomProgram.Field.CREATOR_ID, user.getField(User.Field.ID));
				customProgram.setField(CustomProgram.Field.CREATION_DATE, new Date());
				customProgram.setField(CustomProgram.Field.PICTURE, rLogo);
				customProgram.setField(CustomProgram.Field.NAME, rName);
				customProgram.setField(CustomProgram.Field.TOTAL_TIME, rDuration);
				customProgram.setField(CustomProgram.Field.ACTIVITIES, rSteps);
				customProgram.setField(CustomProgram.Field.NB_ACTIVITIES, rNBSteps);
				Database.update_entity(Collections.CustomPrograms, customProgram);
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
				sending.put(Protocol.Field.CUSTOMPROGRAMID.key, customProgram.getField(CustomProgram.Field.ID).toString());
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
