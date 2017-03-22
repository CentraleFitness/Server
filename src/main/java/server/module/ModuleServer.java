package server.module;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

/**
 * Created by hadrien on 14/03/2017.
 *
 * Tuto : http://tutorials.jenkov.com/vert.x/tcp-server.html
 */
public class ModuleServer extends AbstractVerticle {

    private int port;
    private NetServer netServer;

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

                Buffer buffer = Buffer.buffer();
                buffer.appendString("J'ai bien recu ton message : " + event.getString(0, event.length()));
                netSocket.write(buffer);
            });
        });

        this.netServer.listen(this.port);
    }
}
