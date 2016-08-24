package wbs.console.forms;

import java.util.List;

import wbs.framework.entity.record.Record;

public
interface EntityFinder<Entity extends Record<Entity>> {

	Entity findEntity (
			Long id);

	List<Entity> findEntities ();

}
