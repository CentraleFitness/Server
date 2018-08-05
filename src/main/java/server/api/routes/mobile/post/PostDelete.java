package server.api.routes.mobile.post;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.filefilter.AndFileFilter;
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
import model.entities.ElectricProduction;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Picture;
import model.entities.Post;
import model.entities.SportSession;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class PostDelete {
    public PostDelete(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.POST_DELETE.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rPostId = (String) received.get(Protocol.Field.POSTID.key);
                
                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    return;
                }
                if (rPostId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.POSTID.key);
                    return;
                }
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
                ObjectId postId = new ObjectId(rPostId);
				Post post = (Post) Database.find_entity(Collections.Posts, and(eq(Post.Field.ID.get_key(), postId),
						eq(Post.Field.POSTERID.get_key(), user.getField(User.Field.ID))));
				if (post != null) Database.delete_entity(Collections.Posts, Post.Field.ID, post.getField(Post.Field.ID));  
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
