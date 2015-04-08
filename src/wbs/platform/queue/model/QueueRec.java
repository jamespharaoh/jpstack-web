package wbs.platform.queue.model;

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
class QueueRec
	implements MinorRecord<QueueRec> {

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

	// details

	@TypeField
	QueueTypeRec queueType;

	// children

	@CollectionField
	Set<QueueSubjectRec> subjects;

	// compare to

	@Override
	public
	int compareTo (
			Record<QueueRec> otherRecord) {

		QueueRec other =
			(QueueRec) otherRecord;

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
	class QueueHooks
		extends AbstractObjectHooks<QueueRec> {

		// dependencies

		@Inject
		Database database;

		@Inject
		ObjectTypeDao objectTypeDao;

		@Inject
		QueueTypeDao queueTypeDao;

		// state

		Set<Integer> parentObjectTypeIds =
			new HashSet<Integer> ();

		// init

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

				List<QueueTypeRec> queueTypes =
					queueTypeDao.findByParentObjectType (
						objectType);

				if (queueTypes.isEmpty ())
					continue;

				parentObjectTypeIds.add (
					objectType.getId ());

			}

		}

		@Override
		public
		void createSingletons (
				ObjectHelper<QueueRec> queueHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! parentObjectTypeIds.contains (
					parentHelper.objectTypeId ()))
				return;

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			List<QueueTypeRec> queueTypes =
				queueTypeDao.findByParentObjectType (
					parentType);

			for (
				QueueTypeRec queueType
					: queueTypes
			) {

				queueHelper.insert (
					new QueueRec ()

					.setQueueType (
						queueType)

					.setCode (
						queueType.getCode ())

					.setParentObjectType (
						parentType)

					.setParentObjectId (
						parent.getId ())

				);


			}

		}

	}

}
