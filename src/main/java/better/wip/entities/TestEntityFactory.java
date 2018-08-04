package better.wip.entities;

public enum TestEntityFactory implements EntityFactory<TestEntity> {
	INSTANCE();

	@Override
	public TestEntity newInstance() {
		return new TestEntity();
	}
	
}
