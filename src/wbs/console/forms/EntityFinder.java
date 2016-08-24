package wbs.console.forms;

import java.util.List;

import wbs.framework.entity.record.Record;

public
interface EntityFinder <Entity extends Record <Entity>> {

	Class <Entity> entityClass ();

	Entity findEntity (
			Long id);

	List <Entity> findAllEntities ();

}
