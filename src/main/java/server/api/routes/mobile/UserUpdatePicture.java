package server.api.routes.mobile;

import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Picture;
import model.entities.User;
import protocol.Protocol;
import protocol.mobile.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class UserUpdatePicture {
    public UserUpdatePicture(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PICTURE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            String pic64;
            Database database = Database.getInstance();

            try {
                user = (User) database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if ((pic64 = (String) received.get(Protocol.Field.PICTURE.key)) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                }
                else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    Picture pic = (Picture) database.new_entity(Database.Collections.Pictures);
                    pic.setField(Picture.Field.PICTURE, pic64);
                    user.setField(User.Field.PICTURE_ID, pic.getField(Picture.Field.PICTURE_ID));
                    database.update_entity(Database.Collections.Pictures, pic);
                    database.update_entity(Database.Collections.Users, user);
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}
