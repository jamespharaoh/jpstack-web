package wbs.platform.affiliate.logic;

import static wbs.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.affiliate.model.AffiliateTypeDao;
import wbs.platform.affiliate.model.AffiliateTypeRec;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class AffiliateHooks
	implements ObjectHooks <AffiliateRec> {

	// singleton dependencies

	@SingletonDependency
	AffiliateTypeDao affiliateTypeDao;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeDao objectTypeDao;

	// state

	Map <Long, List <Long>> affiliateTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"AffiliateHooks.init ()",
					this);

		) {

			affiliateTypeIdsByParentTypeId =
				affiliateTypeDao.findAll ().stream ().collect (
					Collectors.groupingBy (

				affiliateType ->
					affiliateType.getParentType ().getId (),

				Collectors.mapping (
					affiliateType ->
						affiliateType.getId (),
					Collectors.toList ()))

			);

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper<AffiliateRec> affiliateHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				affiliateTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createSingletons");

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long affiliateTypeId
				: affiliateTypeIdsByParentTypeId.get (
					parentHelper.objectTypeId ())
		) {

			AffiliateTypeRec affiliateType =
				affiliateTypeDao.findRequired (
					affiliateTypeId);

			affiliateHelper.insert (
				taskLogger,
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
