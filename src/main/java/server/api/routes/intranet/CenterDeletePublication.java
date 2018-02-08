package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import protocol.ProtocolIntranet;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class CenterDeletePublication {
    public CenterDeletePublication(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.CENTER_DELETE_PUBLICATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else if (received.get(ProtocolIntranet.Field.CREATION_DATE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(ProtocolIntranet.Field.TEXT.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                } else {
                    center = (Fitness_Center) database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_NO_CENTER.code);
                    } else {

                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);
                        @SuppressWarnings("unchecked")
                        ArrayList<Fitness_Center.Publication> publications = (ArrayList<Fitness_Center.Publication>) center.getField(Fitness_Center.Field.PUBLICATIONS);
                        Fitness_Center.Publication cur;
                        for(Iterator<Fitness_Center.Publication> i = publications.iterator(); i.hasNext();) {
                            cur = i.next();
                            if (cur.getField(Fitness_Center.Publication.Field.TEXT) == received.get(ProtocolIntranet.Field.TEXT.key) &&
                                    cur.getField(Fitness_Center.Publication.Field.CREATION_DATE) == received.get(ProtocolIntranet.Field.CREATION_DATE.key)) {
                                i.remove();
                                break;
                            }
                        }
                        center.setField(Fitness_Center.Field.PUBLICATIONS, publications);
                        database.update_entity(Database.Collections.Fitness_Centers, center);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}