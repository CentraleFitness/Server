package server.central;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.Vertx;
import model.Database;
import server.Settings;
import server.mobile.MobileServer;
import server.module.ModuleServer;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by hadrien on 13/03/2017.
 *
 * Tuto : http://vertx.io/docs/vertx-core/java/
 */
public class CentralServer {

    private Vertx vertx;
    private Settings settings;
    private MobileServer mobileServer;
    private ModuleServer moduleServer;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public CentralServer() {
        System.out.println("...CentralServer creation...");

        this.mongoClient = new MongoClient(Database.ip, Database.port);
        this.database = mongoClient.getDatabase(Database.databaseName);

        Gson gson = new Gson();
        JsonElement jsonElement = null;
        JsonParser parser = new JsonParser();

        try {
            jsonElement = parser.parse(new JsonReader(new FileReader("ServerSettings.ini")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        this.settings = gson.fromJson(jsonElement, Settings.class);
        this.vertx = Vertx.vertx();

        this.mobileServer = new MobileServer(Integer.parseInt(this.settings.get("Mobile Server Http Port")));
        this.mobileServer.setDatabase(this.database);
        this.moduleServer = new ModuleServer(Integer.parseInt(this.settings.get("Module Server TCP Port")));

        this.vertx.deployVerticle(this.mobileServer);
        this.vertx.deployVerticle(this.moduleServer);
    }
}
