package wbs.console.forms.object;

import static wbs.utils.etc.LogicUtils.booleanInverseFunction;
import static wbs.utils.etc.ResultUtils.mapSuccess;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import fj.data.Either;

public
interface EntityFinder <
	EntityType extends Record <EntityType>
> {

	Class <EntityType> entityClass ();

	EntityType findEntity (
			Transaction parentTransaction,
			Long id);

	List <EntityType> findAllEntities (
			Transaction parentTransaction);

	default
	List <EntityType> findAllNotDeletedEntities (
			Transaction parentTransaction) {

		return findAllEntities (
			parentTransaction)

			.stream ()

			.filter (
				entity ->
					getNotDeletedCheckParents (
						parentTransaction,
						entity))

			.collect (
				Collectors.toList ());

	}

	Either <Boolean, String> getDeletedOrError (
			Transaction parentTransaction,
			EntityType entity,
			boolean checkParents);

	default
	Either <Boolean, String> getDeletedOrErrorCheckParents (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity) {

		return getDeletedOrError (
			parentTransaction,
			entity,
			true);

	}

	default
	Either <Boolean, String> getDeletedOrErrorNoCheckParents (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity) {

		return getDeletedOrError (
			parentTransaction,
			entity,
			false);

	}

	default
	Either <Boolean, String> getNotDeletedOrErrorCheckParents (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity) {

		return mapSuccess (
			getDeletedOrError (
				parentTransaction,
				entity,
				true),
			booleanInverseFunction ());

	}

	default
	Either <Boolean, String> getNotDeletedOrErrorNoCheckParents (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity) {

		return mapSuccess (
			getDeletedOrError (
				parentTransaction,
				entity,
				false),
			booleanInverseFunction ());

	}

	Boolean getDeleted (
			Transaction parentTransaction,
			EntityType entity,
			boolean checkParents);

	default
	Boolean getDeletedCheckParents (
			Transaction parentTransaction,
			EntityType entity) {

		return getDeleted (
			parentTransaction,
			entity,
			true);

	}

	default
	Boolean getDeletedNoCheckParents (
			Transaction parentTransaction,
			EntityType entity) {

		return getDeleted (
			parentTransaction,
			entity,
			false);

	}

	default
	Boolean getNotDeleted (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity,
			boolean checkParents) {

		return ! getDeleted (
			parentTransaction,
			entity,
			checkParents);

	}

	default
	Boolean getNotDeletedCheckParents (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity) {

		return ! getDeleted (
			parentTransaction,
			entity,
			true);

	}

	default
	Boolean getNotDeletedNoCheckParents (
			@NonNull Transaction parentTransaction,
			@NonNull EntityType entity) {

		return ! getDeleted (
			parentTransaction,
			entity,
			false);

	}

}
