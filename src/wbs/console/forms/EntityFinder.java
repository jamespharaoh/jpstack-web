package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.booleanInverseFunction;
import static wbs.utils.etc.Misc.mapSuccess;

import java.util.List;
import java.util.stream.Collectors;

import wbs.framework.entity.record.Record;

import fj.data.Either;

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

	Either <Boolean, String> getDeletedOrError (
			EntityType entity,
			boolean checkParents);

	default
	Either <Boolean, String> getDeletedOrErrorCheckParents (
			EntityType entity) {

		return getDeletedOrError (
			entity,
			true);

	}

	default
	Either <Boolean, String> getDeletedOrErrorNoCheckParents (
			EntityType entity) {

		return getDeletedOrError (
			entity,
			false);

	}

	default
	Either <Boolean, String> getNotDeletedOrErrorCheckParents (
			EntityType entity) {

		return mapSuccess (
			getDeletedOrError (
				entity,
				true),
			booleanInverseFunction ());

	}

	default
	Either <Boolean, String> getNotDeletedOrErrorNoCheckParents (
			EntityType entity) {

		return mapSuccess (
			getDeletedOrError (
				entity,
				false),
			booleanInverseFunction ());

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
