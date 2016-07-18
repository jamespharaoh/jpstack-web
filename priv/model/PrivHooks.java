package wbs.platform.priv.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class PrivHooks
	implements ObjectHooks<PrivRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	PrivTypeDao privTypeDao;

	// state

	Map<Long,List<Long>> privTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"privHooks.init ()",
				this);

		// preload object types

		objectTypeDao.findAll ();

		// load priv types and construct index

		privTypeIdsByParentTypeId =
			privTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (
					privType -> (long)
						privType.getParentObjectType ().getId (),
					Collectors.mapping (
						privType -> (long)
							privType.getId (),
						Collectors.toList ())));

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<PrivRec> privHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				privTypeIdsByParentTypeId.keySet (),
				(long) parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long privTypeId
				: privTypeIdsByParentTypeId.get (
					(long) parentHelper.objectTypeId ())
		) {

			PrivTypeRec privType =
				privTypeDao.findRequired (
					privTypeId);

			privHelper.insert (
				privHelper.createInstance ()

				.setPrivType (
					privType)

				.setCode (
					privType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

	}

}