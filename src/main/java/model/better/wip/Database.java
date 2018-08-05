package model.better.wip;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public enum Database {
	CentraleFitness("centralefitness", "localhost:27017");

	private final MongoClient dbClient;
	private final MongoDatabase db;
	private final String name;
	private final String address;
	
	private Database(String dbName, String dbAddress) {
		this.name = dbName;
		this.address = dbAddress;
		this.dbClient = new MongoClient(this.address);
		this.db = this.dbClient.getDatabase(this.name());
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public MongoDatabase getMongoDB() {
		return this.db;
	}
	
	public static void main(String[] args) {
		MongoCollection<Document> coll = Database.CentraleFitness.getMongoDB().getCollection("");
	}
}
