package server.api.routes.mobile.post;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.and;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

import Tools.LogManager;
import Tools.Token;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Event;
import model.entities.Post;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class GetPosts {
    public GetPosts(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_POSTS.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rTargetId = (String) received.get(Protocol.Field.TARGETID.key);
                Integer rStart = (Integer) received.get(Protocol.Field.START.key);
                Integer rEnd = (Integer) received.get(Protocol.Field.END.key);

                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rTargetId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TARGETID.key);
                    break label;
                }
                if (rStart == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.START.key);
                    break label;
                }
                if (rEnd == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.END.key);
                    break label;
                }
                JWT token = Token.decodeToken(rToken);
                if (token == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    break label;
                }
                User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, token.getIssuer());
                if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    break label;
                }
                ObjectId targetId = new ObjectId(rTargetId);
                List<Map<String, String>> postList =
                        (List<Map<String, String>>) Database
                                .collections
                                .get(Database.Collections.Posts)
                                .find(and(
                                        eq(Post.Field.FITNESS_CENTERT_ID.get_key(), targetId),

                                        or(
                                            eq(Post.Field.IS_DELETED.get_key(), false),
                                            eq(Post.Field.IS_DELETED.get_key(), null)
                                        ),

                                        or(eq(Post.Field.IS_COMMENT.get_key(), false),
                                                eq(Post.Field.IS_COMMENT.get_key(), null))
                                ))
                                .sort(new BasicDBObject(Post.Field.DATE.get_key(), 1))
                                .skip(rStart)
                                .limit(rEnd)
                                .into(new ArrayList())
                                .stream()
                                .map(doc -> {
                                    Post post = new Post((Document) doc);
                                    Map<String, String> fields = new HashMap<>();
                                    fields.put(Protocol.Field.POSTID.key, ((ObjectId)post.getField(Post.Field.ID)).toString());
                                    fields.put(Protocol.Field.POSTTYPE.key, (String)post.getField(Post.Field.TYPE));
                                    if (post.getField(Post.Field.EVENT_ID) != null) {
                                        fields.put(Protocol.Field.EVENTID.key, post.getField(Post.Field.EVENT_ID).toString());
                                    }
                                    return fields;
                                })
                                .collect(Collectors.toList());
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                sending.put(Protocol.Field.POSTS.key, postList);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
