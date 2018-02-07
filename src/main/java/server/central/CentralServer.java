package server.central;

import Tools.LogManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.vertx.core.Vertx;
import model.Database;
import server.Settings;
import server.intranet.IntranetVerticle;
import server.mobile.MobileVerticle;
import server.module.ModuleVerticle;

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
    private MobileVerticle mobileVerticle;
    private ModuleVerticle moduleVerticle;
    private IntranetVerticle intranetVerticle;
    private Database database = null;

    public CentralServer() {
        System.out.println("...CentralServer creation et ma bite en salade...");

        this.database = Database.getInstance();

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

        if (Boolean.parseBoolean(this.settings.get("EnableLogManager")) == true) LogManager.enable();
        this.mobileVerticle = new MobileVerticle(Integer.parseInt(this.settings.get("Mobile Server Http Port")));
        this.moduleVerticle = new ModuleVerticle(Integer.parseInt(this.settings.get("Module Server TCP Port")));
        this.intranetVerticle = new IntranetVerticle(Integer.parseInt(this.settings.get("Intranet Server Http Port")));
        this.intranetVerticle.setDatabase(database);

        this.vertx.deployVerticle(this.mobileVerticle);
        this.vertx.deployVerticle(this.moduleVerticle);
        this.vertx.deployVerticle(this.intranetVerticle);
    }
}
