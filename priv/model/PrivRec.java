package wbs.platform.priv.model;

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
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.TypeField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.group.model.GroupRec;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.user.model.UserPrivRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PrivRec
	implements MinorRecord<PrivRec> {

	@GeneratedIdField
	Integer id;

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	@TypeField
	PrivTypeRec privType;

	@CollectionField (
		table = "user_priv")
	Set<UserPrivRec> userPrivs =
		new HashSet<UserPrivRec> ();

	@LinkField (
		table = "group_priv")
	Set<GroupRec> groups =
		new HashSet<GroupRec> ();

	// object hooks

	public static
	class PrivHooks
		extends AbstractObjectHooks<PrivRec> {

		@Inject
		Database database;

		@Inject
		ObjectTypeDao objectTypeDao;

		@Inject
		PrivTypeDao privTypeDao;

		Set<Integer> parentObjectTypeIds =
			new HashSet<Integer> ();

		@PostConstruct
		public
		void init () {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					this);

			List<ObjectTypeRec> objectTypes =
				objectTypeDao.findAll ();

			for (ObjectTypeRec objectType : objectTypes) {

				List<PrivTypeRec> privTypes =
					privTypeDao.findByParentObjectType (
						objectType);

				if (privTypes.isEmpty ())
					continue;

				parentObjectTypeIds.add (
					objectType.getId ());

			}

		}

		@Override
		public
		void createSingletons (
				ObjectHelper<PrivRec> privHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! parentObjectTypeIds.contains (
					parentHelper.objectTypeId ()))
				return;

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			List<PrivTypeRec> privTypes =
				privTypeDao.findByParentObjectType (
					parentType);

			for (PrivTypeRec privType
					: privTypes) {

				privHelper.insert (
					new PrivRec ()
						.setPrivType (privType)
						.setCode (privType.getCode ())
						.setParentObjectType (parentType)
						.setParentObjectId (parent.getId ()));

			}

		}

	}

	@Override
	public
	int compareTo (
			Record<PrivRec> otherRecord) {

		PrivRec other =
			(PrivRec) otherRecord;

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


}
