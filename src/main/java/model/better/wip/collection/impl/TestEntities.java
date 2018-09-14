package model.better.wip.collection.impl;

import java.util.function.Supplier;

import model.better.wip.collection.Collection;
import model.better.wip.entity.impl.TestEntity;

public class TestEntities extends Collection<TestEntities, TestEntity> {

	
	protected TestEntities(Supplier<TestEntity> entityFactory, String collectionName) {
		super(entityFactory, collectionName);
		// TODO Auto-generated constructor stub
	}

	public static final TestEntities INSTANCE = new TestEntities(()-> new TestEntity(), "testEntities");
}
