package server.api.routes.web;

import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import protocol.web.Protocol;
import server.webserver.WebVerticle;



public class Confidentiality {

    private final static String htmlContent = "<html><h1>R&egrave;gles de confidentialit&eacute; Centrale Fitness</h1>" +
            "<p>Nous n'utilisons aucune de vos donn&eacute;es personnelles &agrave; des fins commerciales.</p>" +
            "</html>";

    public Confidentiality(WebVerticle webVerticle) {
        webVerticle.getRouter().route(HttpMethod.GET, Protocol.Path.CONFIDENTIALITYRULES.path).handler(routingContext -> {

            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            response.putHeader("content-type", "text/html").end(htmlContent);
        });
    }
}
