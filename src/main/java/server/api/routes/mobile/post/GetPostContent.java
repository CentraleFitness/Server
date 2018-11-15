package server.api.routes.mobile.post;

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
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Picture;
import model.entities.Post;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class GetPostContent {
	public GetPostContent(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.GET_POSTCONTENT.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

			label: try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);
				String rPostId = (String) received.get(Protocol.Field.POSTID.key);

				if (rToken == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
					break label;
				}
				if (rPostId == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.POSTID.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.POSTID.key);
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
				ObjectId postId = new ObjectId(rPostId);
				Post post = (Post) Database.find_entity(Database.Collections.Posts, Post.Field.ID, postId);
				if (post == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.POST_NOT_FOUND.code);
					LogManager.write(Protocol.Status.POST_NOT_FOUND.message);
					break label;
				}
				ObjectId pictureId = (ObjectId) Optional.ofNullable(post.getField(Post.Field.POSTERID))
						.map(posterId -> {
							try {
								User u;
								u = (User) Database.find_entity(Collections.Users, User.Field.ID, posterId);
								if (u != null)
									return u.getField(User.Field.PICTURE_ID);
								Fitness_Center_Manager fCM = (Fitness_Center_Manager) Database.find_entity(
										Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, posterId);
								if (fCM != null)
									return fCM.getField(Fitness_Center_Manager.Field.PICTURE_ID);
								Fitness_Center fC = (Fitness_Center) Database.find_entity(Collections.Fitness_Centers,
										Fitness_Center.Field.ID, posterId);
								if (fC != null)
									return fC.getField(Fitness_Center.Field.PICTURE_ID);
							} catch (InvocationTargetException | NoSuchMethodException | InstantiationException
									| IllegalAccessException e) {
								return null;
							}
							return null;
						}).orElse(null);
				Picture picture = Optional.ofNullable(pictureId).map(id -> {
					try {
						return (Picture) Database.find_entity(Collections.Pictures, Picture.Field.ID, pictureId);
					} catch (InvocationTargetException | NoSuchMethodException | InstantiationException
							| IllegalAccessException e) {
						return null;
					}
				}).orElse(null);
				sending = new ResponseObject(false);
				Boolean isCenter = Optional.ofNullable(post.getField(Post.Field.IS_CENTER)).map(bobo -> (Boolean) bobo)
						.orElse(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
				sending.put(Protocol.Field.POSTTYPE.key, post.getField(Post.Field.TYPE));
				sending.put(Protocol.Field.POSTDATE.key, post.getField(Post.Field.DATE));
				sending.put(Protocol.Field.POSTICON.key,
						Optional.ofNullable(picture).map(pic -> pic.getField(Picture.Field.PICTURE)).orElse(null));
				sending.put(Protocol.Field.POSTCONTENT.key, post.getField(Post.Field.CONTENT));
				sending.put(Protocol.Field.NAME.key,
						!isCenter
								? Optional
										.ofNullable(Database.find_entity(Collections.Users, User.Field.ID,
												post.getField(Post.Field.POSTERID)))
										.map(uposter -> ((User) uposter).getField(User.Field.LOGIN)).orElse("")
								: Optional
										.ofNullable(Database.find_entity(Collections.Fitness_Centers,
												Fitness_Center.Field.ID, post.getField(Post.Field.FITNESS_CENTERT_ID)))
										.map(fcposter -> ((Fitness_Center)fcposter).getField(Fitness_Center.Field.NAME))
										.orElse(null));
				sending.put("isCenter", isCenter);
			} catch (Exception e) {
				sending = new ResponseObject(true);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
				LogManager.write(e);
			}
			response.end(new GsonBuilder().create().toJson(sending));
		});
	}
}
