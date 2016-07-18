package wbs.platform.affiliate.model;

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
class AffiliateHooks
	implements ObjectHooks<AffiliateRec> {

	// dependencies

	@Inject
	AffiliateTypeDao affiliateTypeDao;

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	// state

	Map<Long,List<Long>> affiliateTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"AffiliateHooks.init ()",
				this);

		affiliateTypeIdsByParentTypeId =
			affiliateTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (
					affiliateType -> (long)
						affiliateType.getParentType ().getId (),
					Collectors.mapping (
						affiliateType -> (long)
							affiliateType.getId (),
						Collectors.toList ())));

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<AffiliateRec> affiliateHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				affiliateTypeIdsByParentTypeId.keySet (),
				(long) parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long affiliateTypeId
				: affiliateTypeIdsByParentTypeId.get (
					(long) parentHelper.objectTypeId ())
		) {

			AffiliateTypeRec affiliateType =
				affiliateTypeDao.findRequired (
					affiliateTypeId);

			affiliateHelper.insert (
				affiliateHelper.createInstance ()

				.setAffiliateType (
					affiliateType)

				.setCode (
					affiliateType.getCode ())

				.setDescription (
					affiliateType.getDescription ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

	}

}
