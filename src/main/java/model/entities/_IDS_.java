package model.entities;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.Database;
import org.bson.Document;

import javax.print.Doc;

public class _IDS_ extends Database.Entity {
    public _IDS_() {
        for (Database.Collections col : Database.Collections.values()) put(col.entity_id, "0");
    }

    public _IDS_(Document doc) {super(doc);}
}
