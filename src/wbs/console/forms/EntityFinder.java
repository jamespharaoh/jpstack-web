package wbs.console.forms;

import java.util.List;
import java.util.stream.Collectors;

import wbs.framework.entity.record.Record;

public
interface EntityFinder <Entity extends Record <Entity>> {

	Class <Entity> entityClass ();

	Entity findEntity (
			Long id);

	List <Entity> findAllEntities ();

	default
	List <Entity> findAllNotDeletedEntities () {

		return findAllEntities ().stream ()

			.filter (
				this::isNotDeleted)

			.collect (
				Collectors.toList ());

	}

	Boolean isDeleted (
			Entity entity);

	default
	Boolean isNotDeleted (
			Entity entity) {

		return ! isDeleted (
			entity);

	}

}
