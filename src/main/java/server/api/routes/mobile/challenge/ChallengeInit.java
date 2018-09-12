package server.api.routes.mobile.challenge;

import Tools.LogManager;
import Tools.Token;
import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Challenge;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

import java.util.Arrays;
import java.util.Map;

public class ChallengeInit {
    public ChallengeInit(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.CHALLENGE_INIT.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                if (Database.countEntities(Database.Collections.Challenges) == 0){
                    Challenge bikeChallenge = new Challenge();
                    bikeChallenge.setField(Challenge.Field.TYPE, Challenge.ChallengeType.COLLECTIF.value);
                    bikeChallenge.setField(Challenge.Field.TITLE, "Depassez vous en vélo !");
                    bikeChallenge.setField(Challenge.Field.OWNER, "");
                    bikeChallenge.setField(Challenge.Field.STEPS, Arrays.asList(2000, 4000, 6000, 8000));
                    bikeChallenge.setField(Challenge.Field.DESC, "Tentez de vous dépasser en vélo et obtenez le diamant !");
                    bikeChallenge.setField(Challenge.Field.MACHINE, Challenge.Machines.BIKE.value);
                    bikeChallenge.setField(Challenge.Field.ENDDATE, "31/12/2018");
                    bikeChallenge.setField(Challenge.Field.POINTSNEEDED, 12000);

                    Challenge helipticChallenge = new Challenge();
                    helipticChallenge.setField(Challenge.Field.TYPE, Challenge.ChallengeType.COLLECTIF.value);
                    helipticChallenge.setField(Challenge.Field.TITLE, "Depassez vous en héliptique !");
                    helipticChallenge.setField(Challenge.Field.OWNER, "");
                    helipticChallenge.setField(Challenge.Field.STEPS, Arrays.asList(2000, 4000, 6000, 8000));
                    helipticChallenge.setField(Challenge.Field.DESC, "Tentez de vous dépasser en héliptique et obtenez le diamant !");
                    helipticChallenge.setField(Challenge.Field.MACHINE, Challenge.Machines.HELIPTIC.value);
                    helipticChallenge.setField(Challenge.Field.ENDDATE, "31/12/2018");
                    helipticChallenge.setField(Challenge.Field.POINTSNEEDED, 12000);

                    Database.insert_entity(Database.Collections.Challenges, bikeChallenge);
                    Database.insert_entity(Database.Collections.Challenges, helipticChallenge);


                }
            } catch (Exception e) {
                e.printStackTrace();
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
