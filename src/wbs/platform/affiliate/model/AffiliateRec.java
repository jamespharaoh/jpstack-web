package wbs.platform.affiliate.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.TypeField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class AffiliateRec
	implements MinorRecord<AffiliateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	// description

	@DescriptionField
	String description;

	@TypeField
	AffiliateTypeRec type;

	// compare to

	@Override
	public
	int compareTo (
			Record<AffiliateRec> otherRecord) {

		AffiliateRec other =
			(AffiliateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getParentObjectId (),
				other.getParentObjectId ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// object hooks

	public static
	class AffiliateHooks
		extends AbstractObjectHooks<AffiliateRec> {

		@Inject
		AffiliateTypeDao affiliateTypeDao;

		@Inject
		Database database;

		@Inject
		ObjectTypeDao objectTypeDao;

		Set<Integer> parentObjectTypeIds =
			new HashSet<Integer> ();

		@PostConstruct
		public
		void init () {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

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

		@Override
		public
		void createSingletons (
				ObjectHelper<AffiliateRec> affiliateHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parentObject) {

			if (! parentObjectTypeIds.contains (
					parentHelper.objectTypeId ()))
				return;

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			List<AffiliateTypeRec> affiliateTypes =
				affiliateTypeDao.findByParentObjectType (
					parentType);

			for (AffiliateTypeRec affiliateType
					: affiliateTypes) {

				affiliateHelper.insert (
					new AffiliateRec ()
						.setType (affiliateType)
						.setCode (affiliateType.getCode ())
						.setDescription (affiliateType.getDescription ())
						.setParentObjectType (parentType)
						.setParentObjectId (parentObject.getId ()));

			}

		}

	}

}
