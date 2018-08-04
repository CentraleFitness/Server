package model.better.wip;

import java.util.function.Supplier;

import model.better.wip.collection.impl.TestEntities;
import model.better.wip.entity.impl.TestEntity;

public class Database {

	static int toto = 0;

	public Database() {
		toto += 1;
		System.out.println(toto);
	}

	public static void main(String[] args) {
		TestEntity testEntity = TestEntities.INSTANCE.newEntity();
		Supplier<Database> newDB = ()->new Database();
		newDB.get();
		newDB.get();
		newDB.get();
		newDB.get();
		newDB.get();
	}
}
