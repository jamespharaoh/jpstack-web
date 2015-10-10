package wbs.console.forms;

import java.util.List;

import wbs.framework.record.Record;

public
interface EntityFinder<Entity extends Record<Entity>> {

	Entity findEntity (
			int id);

	List<Entity> findEntities ();

}
