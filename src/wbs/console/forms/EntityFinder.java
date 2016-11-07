package wbs.console.forms;

import java.util.List;
import java.util.stream.Collectors;

import wbs.framework.entity.record.Record;

public
interface EntityFinder <
	EntityType extends Record <EntityType>
> {

	Class <EntityType> entityClass ();

	EntityType findEntity (
			Long id);

	List <EntityType> findAllEntities ();

	default
	List <EntityType> findAllNotDeletedEntities () {

		return findAllEntities ().stream ()

			.filter (
				this::getNotDeletedCheckParents)

			.collect (
				Collectors.toList ());

	}

	Boolean getDeleted (
			EntityType entity,
			boolean checkParents);

	default
	Boolean getDeletedCheckParents (
			EntityType entity) {

		return getDeleted (
			entity,
			true);

	}

	default
	Boolean getDeletedNoCheckParents (
			EntityType entity) {

		return getDeleted (
			entity,
			false);

	}

	default
	Boolean getNotDeleted (
			EntityType entity,
			boolean checkParents) {

		return ! getDeleted (
			entity,
			checkParents);

	}

	default
	Boolean getNotDeletedCheckParents (
			EntityType entity) {

		return ! getDeleted (
			entity,
			true);

	}

	default
	Boolean getNotDeletedNoCheckParents (
			EntityType entity) {

		return ! getDeleted (
			entity,
			false);

	}

}
