package better.wip.entities;

public class TestEntity implements Entity<TestEntity>{
	@Override
	public EntityFactory<TestEntity> getFactory() {
		// TODO Auto-generated method stub
		return TestEntityFactory.INSTANCE;
	}
}
