package model.better.wip.collection;

import java.util.function.Supplier;

import model.better.wip.entity.Entity;

public abstract class Collection<C extends Collection<C, E>, E extends Entity<E>> {

	private final Supplier<E> entityFactory;
	private final String collectionName;

	protected Collection(Supplier<E> entityFactory, String collectionName) {
		this.collectionName = collectionName;
		this.entityFactory = entityFactory;
	}

	public E newEntity() {
		return entityFactory.get();
	}

	public void deleteEntity(E entity) {
	}

	public String getName() {
		return collectionName;
	}
}
