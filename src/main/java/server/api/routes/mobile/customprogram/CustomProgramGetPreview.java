package server.api.routes.mobile.customprogram;

import java.lang.reflect.InvocationTargetException;
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
import model.entities.CustomProgram;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class CustomProgramGetPreview {
	public CustomProgramGetPreview(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.CUSTOMPROGRAM_GET_PREVIEW.path).handler(routingContext -> {

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
				CustomProgram customProgram = (CustomProgram) Database.find_entity(Collections.CustomPrograms,
						CustomProgram.Field.ID, customProgramId);
				if (customProgram == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.CUSTOM_PROGRAM_NOT_FOUND.code);
					LogManager.write(Protocol.Status.CUSTOM_PROGRAM_NOT_FOUND.message);
					return;
				}
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
				sending.put(Protocol.Field.LOGO.key, customProgram.getField(CustomProgram.Field.PICTURE));
				sending.put(Protocol.Field.NOTE.key,
						Optional.ofNullable(customProgram.getField(CustomProgram.Field.NOTE)).orElse("5"));
				sending.put(Protocol.Field.DURATION.key, customProgram.getField(CustomProgram.Field.TOTAL_TIME));
				sending.put(Protocol.Field.CREATOR.key, java.util.Optional
						.ofNullable(customProgram.getField(CustomProgram.Field.CREATOR_ID)).map(id -> {
							String name = "unknown";
							try {
								User u = (User) Database.find_entity(Collections.Users, User.Field.ID, id);
								Fitness_Center_Manager fcm = (Fitness_Center_Manager) Database.find_entity(
										Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, id);
								if (u != null) {
									name = (String) u.getField(User.Field.LOGIN);
								} else if (fcm != null) {
									name = (String) Optional
											.ofNullable(fcm.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID))
											.map(centerid -> {
												try {
													return Database.find_entity(Collections.Fitness_Centers,
															Fitness_Center.Field.ID, centerid);
												} catch (InvocationTargetException | NoSuchMethodException
														| InstantiationException | IllegalAccessException e) {
													e.printStackTrace();
												}
												return null;
											})
											.map(center -> ((Fitness_Center) center)
													.getField(Fitness_Center.Field.NAME))
											.orElse("no data");
								} else {
									name = "no data";
								}
							} catch (InvocationTargetException | NoSuchMethodException | InstantiationException
									| IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return name;
							}
							return name;
						}).orElse("unknown"));
				sending.put(Protocol.Field.NBSTEPS.key, customProgram.getField(CustomProgram.Field.NB_ACTIVITIES));
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
