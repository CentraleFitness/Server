package server.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import model.Database;
import org.bson.Document;
import sun.security.pkcs11.Secmod;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by hadrien on 14/03/2017.
 *
 * Tuto : http://tutorials.jenkov.com/vert.x/tcp-server.html
 */
public class ModuleServer extends AbstractVerticle {

    private int port;
    private NetServer netServer;
    private Database database = null;

    public ModuleServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {

    }
    public void setDatabase(Database database) {
        this.database = database;
    }
}
