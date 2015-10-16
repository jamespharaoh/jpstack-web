package wbs.platform.affiliate.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class AffiliateHooks
	extends AbstractObjectHooks<AffiliateRec> {

	// dependencies

	@Inject
	AffiliateTypeDao affiliateTypeDao;

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	// state

	Set<Integer> parentObjectTypeIds =
		new HashSet<Integer> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<ObjectTypeRec> objectTypes =
			objectTypeDao.findAll ();

		for (ObjectTypeRec objectType
				: objectTypes) {

			List<AffiliateTypeRec> affiliateTypes =
				affiliateTypeDao.findByParentObjectType (
					objectType);

			if (affiliateTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			ObjectHelper<AffiliateRec> affiliateHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parentObject) {

		if (
			! parentObjectTypeIds.contains (
				parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<AffiliateTypeRec> affiliateTypes =
			affiliateTypeDao.findByParentObjectType (
				parentType);

		for (
			AffiliateTypeRec affiliateType
				: affiliateTypes
		) {

			affiliateHelper.insert (
				new AffiliateRec ()

				.setAffiliateType (
					affiliateType)

				.setCode (
					affiliateType.getCode ())

				.setDescription (
					affiliateType.getDescription ())

				.setParentType (
					parentType)

				.setParentId (
					parentObject.getId ())

			);

		}

	}

}
