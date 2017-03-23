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
        System.out.println("...ModuleServer creation...");

        this.netServer = this.vertx.createNetServer();

        this.netServer.connectHandler(netSocket -> {
            System.out.println("Incoming connection!");

            System.out.println(netSocket.getClass());
            netSocket.handler(event -> {
                System.out.println("incoming data:" + event.length());
                System.out.println(event.getString(0, event.length()));
                System.out.println();
                Database.Module module = new Database.Module((Document) this.database.modules.find(eq(Database.Module.Fields.moduleName, "module1")).first());
                module.setWattProduction_instant(((JsonObject) new JsonParser().parse(event.getString(0, event.length()))).get("W").getAsDouble());
                this.database.modules.updateOne(eq(Database.idKey, module.getId()), module.getUpdate());
/*                Buffer buffer = Buffer.buffer();
                buffer.appendString("J'ai bien recu ton message : " + event.getString(0, event.length()));
                netSocket.write(buffer);*/
            });
        });

        this.netServer.listen(this.port);
    }
    public void setDatabase(Database database) {
        this.database = database;
    }
}
