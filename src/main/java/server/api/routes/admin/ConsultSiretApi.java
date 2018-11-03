package server.api.routes.admin;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.util.JSON;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.*;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ConsultSiretApi {

    public String InputStreamToString(InputStream inputStream) {
        String result;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        catch (Exception e) {
            System.out.println("Error reading InputStream");
            result = null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    System.out.println("Error closing InputStream");
                }
            }
        }
        return result;
    }

    public ConsultSiretApi(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.CONSULT_SIRET.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) routingContext.request().getParam(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), routingContext.request().getParam(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (routingContext.request().getParam(Protocol.Field.SIRET.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    String api_key = "bf387250a734234389eb6d21e96660db:aa37da76c33d45d0034e75743e9afbf2";
                    String credentials = Base64.encodeBase64String(api_key.getBytes());

                    URL url = new URL("https://www.numero-de-siret.com/api/siret?siret=" + routingContext.request().getParam(Protocol.Field.SIRET.key));
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Authorization", "Basic " + credentials);
                    int status = con.getResponseCode();
                    LogManager.write("STATUS: " + status);
                    String resultContent = InputStreamToString(con.getInputStream());
                    sending.put(Protocol.Field.INFO.key, JSON.parse(resultContent));
                    con.disconnect();

                }

            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }


}
