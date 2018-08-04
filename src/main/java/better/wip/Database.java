package better.wip;

import better.wip.entities.TestEntity;
import better.wip.entities.TestEntityFactory;

public enum Database {
	INSTANCE();
	
	public static Collection<TestEntity> TEST_ENTITIES = new Collection<TestEntity>(TestEntityFactory.INSTANCE, "testentities");

	public static void main(String[] args) {
		TestEntity testEntity = Database.TEST_ENTITIES.newEntity();
	}
}
