package server.api.routes.mobile.post.comment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
import model.entities.Post;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class PostCommentGetRange {
	public PostCommentGetRange(Router router) {
		router.route(HttpMethod.POST, Protocol.Path.POST_COMMENT_GET_RANGE.path).handler(routingContext -> {

			ResponseObject sending = null;
			HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

			try {
				Map<String, Object> received = routingContext.getBodyAsJson().getMap();
				String rToken = (String) received.get(Protocol.Field.TOKEN.key);
				String rTargetId = (String) received.get(Protocol.Field.POSTID.key);
				Integer rStart = (Integer) received.get(Protocol.Field.START.key);
				Integer rEnd = (Integer) received.get(Protocol.Field.END.key);

				if (rToken == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
					LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
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
				ObjectId targetId = new ObjectId(rTargetId);
				Post post = (Post) Database.find_entity(Collections.Posts, Post.Field.ID, targetId);
				if (post == null) {
					sending = new ResponseObject(true);
					sending.put(Protocol.Field.STATUS.key, Protocol.Status.POST_NOT_FOUND.code);
					LogManager.write(Protocol.Status.POST_NOT_FOUND.message);
					return;
				}
				List<ObjectId> comments = (List<ObjectId>) post.getField(Post.Field.COMMENTS);
				if (comments == null) {
					comments = new ArrayList<>();
					post.setField(Post.Field.COMMENTS, comments);
				}
				List<ObjectId> deletedComments = new ArrayList<>();
				List<Map<String, Object>> commentsContents = new ArrayList<>();
				comments.stream().skip(rStart).limit(rEnd).forEach(commentId -> {
					try {
						Map<String, Object> commentContent = new TreeMap<>();
						Post comment = (Post) Database.find_entity(Collections.Posts, Post.Field.ID, commentId);
						if (comment == null ||
								(comment.getField(Post.Field.IS_DELETED) != null &&
										(Boolean) comment.getField(Post.Field.IS_DELETED) == true)) {

							deletedComments.add(commentId);
							return;
						}
						User posterUser = (User) Database.find_entity(Collections.Users, User.Field.ID,
								comment.getField(Post.Field.POSTERID));
						commentContent.put(Protocol.Field.COMMENTID.key, comment.getId().toString());
						commentContent.put(Protocol.Field.NAME.key, comment.getField(Post.Field.POSTERNAME));
						commentContent.put(Protocol.Field.IS_CENTER.key, (comment.getField(Post.Field.IS_CENTER) != null && (Boolean)comment.getField(Post.Field.IS_CENTER)));
						commentContent.put(Protocol.Field.IS_MINE.key, comment.getField(Post.Field.POSTERID).toString().equals(user.getField(User.Field.ID).toString()));
						commentContent.put(Protocol.Field.COMMENTCONTENT.key,
								(String) comment.getField(Post.Field.CONTENT));
						commentContent.put(Protocol.Field.DATE.key, (Long) comment.getField(Post.Field.DATE));

						ArrayList<ObjectId> is_reported = (ArrayList<ObjectId>)comment.getField(Post.Field.IS_REPORTED);

						if (is_reported == null) {
							is_reported = new ArrayList<>();
						}

						commentContent.put(Protocol.Field.REPORTED_BY_ME.key, is_reported.contains(user.getField(User.Field.ID)));

						commentsContents.add(commentContent);
					} catch (InvocationTargetException | NoSuchMethodException | InstantiationException
							| IllegalAccessException e) {
					}
				});
				comments.removeAll(deletedComments);
				Database.update_entity(Collections.Posts, post);
				sending = new ResponseObject(false);
				sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
				sending.put(Protocol.Field.COMMENTS.key, commentsContents);
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
