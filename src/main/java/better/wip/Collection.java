package better.wip;

import better.wip.entities.Entity;
import better.wip.entities.EntityFactory;

public class Collection<E extends Entity<E>> {

	private final EntityFactory<E> entityFactory;
	private final String collectionName;
	
	public Collection(EntityFactory<E> entityFactory, String collectionName) {
		this.collectionName = collectionName;
		this.entityFactory = entityFactory;
	}
	
	public E newEntity() {
		return entityFactory.newInstance();
	}
	
	public void deleteEntity(E entity) {
		
	}
}
