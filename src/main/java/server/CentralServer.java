package server;

import Tools.LogManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import model.Database;
import server.Settings;
import server.image.ImageVerticle;
import server.intranet.IntranetVerticle;
import server.mobile.MobileVerticle;
import server.module.ModuleVerticle;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by hadrien on 13/03/2017.
 *
 * Tuto : http://vertx.io/docs/vertx-core/java/
 */
public class CentralServer {

    private Vertx mVertx = null;
    private Settings mSettings = null;
    private Database mDatabase = null;
    private HashMap<String, AbstractVerticle> mVerticles = null;

    public CentralServer() {
        try {
            System.out.println("...CentralServer creation...");

            mDatabase = Database.getInstance();
            mSettings = new Gson().fromJson(new JsonParser().parse(new JsonReader(new FileReader("ServerSettings.ini"))), Settings.class);
            mVertx = Vertx.vertx();
            mVerticles = new HashMap<>();

            if (Boolean.parseBoolean(mSettings.get("EnableLogManager")) == true) LogManager.enable();

            mVerticles.put(MobileVerticle.class.getName(), new MobileVerticle(Integer.parseInt(mSettings.get("Mobile Server Http Port"))));
            mVerticles.put(ModuleVerticle.class.getName(), new ModuleVerticle(Integer.parseInt(mSettings.get("Module Server Http Port"))));
            mVerticles.put(IntranetVerticle.class.getName(), new IntranetVerticle(Integer.parseInt(mSettings.get("Intranet Server Http Port"))));
            mVerticles.put(ImageVerticle.class.getName(), new ImageVerticle(Integer.parseInt(mSettings.get("Image Server Http Port"))));

            ((IntranetVerticle) mVerticles.get(IntranetVerticle.class.getName())).setDatabase(mDatabase);

            mVerticles.forEach((key, value)-> mVertx.deployVerticle(value));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
