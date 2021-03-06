package server.api.routes.mobile.user.profile;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Picture;
import model.entities.User;
import protocol.mobile.Protocol;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class UserUpdatePicture {
    public UserUpdatePicture(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PICTURE.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            String pic64;

            try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rPicture = (String) received.get(Protocol.Field.PICTURE.key);

                user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken(rToken).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), rToken)) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write("Bad token");
                } else if ((pic64 = rPicture) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                    LogManager.write("Missing picture key");
                }
                else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    Picture pic = (Picture) Database.new_entity(Database.Collections.Pictures);
                    pic.setField(Picture.Field.PICTURE, pic64);
                    user.setField(User.Field.PICTURE_ID, pic.getField(Picture.Field.ID));
                    Database.update_entity(Database.Collections.Pictures, pic);
                    Database.update_entity(Database.Collections.Users, user);
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
