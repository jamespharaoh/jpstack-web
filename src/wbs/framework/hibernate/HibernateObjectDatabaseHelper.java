package wbs.framework.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.LogicUtils.notEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectDatabaseHelper;
import wbs.framework.object.ObjectModel;
import wbs.framework.object.ObjectTypeRegistry;

@Accessors (fluent = true)
@PrototypeComponent ("hibernateObjectDatabaseHelper")
public
class HibernateObjectDatabaseHelper <RecordType extends Record <RecordType>>
	implements ObjectDatabaseHelper <RecordType> {

	// singleton dependencies

	@SingletonDependency
	ActivityManager activityManager;

	@SingletonDependency
	HibernateDatabase hibernateDatabase;

	@SingletonDependency
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> model;

	// public implementation

	@Override
	public
	RecordType find (
			@NonNull Long id) {

		Session session =
			hibernateDatabase.currentSession ();

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			session.get (
				model.objectClass (),
				id);

		return object;

	}

	@Override
	public
	List <RecordType> findMany (
			@NonNull List <Long> ids) {

		if (
			collectionIsEmpty (
				ids)
		) {
			return ImmutableList.of ();
		}

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findMany",
				"list of %s ids");

		Session session =
			hibernateDatabase.currentSession ();

		Criteria criteria =
			session.createCriteria (
				model.objectClass ());

		criteria.add (
			Restrictions.in (
				"id",
				ids));

		List <?> recordsUncast =
			criteria.list ();

		@SuppressWarnings ("unchecked")
		List <RecordType> records =
			(List <RecordType>)
			recordsUncast;

		Map <Long, RecordType> recordsById =
			records.stream ()

			.collect (
				Collectors.toMap (
					record ->
						record.getId (),
					record ->
						record));

		return ids.stream ()

			.map (
				recordsById::get)

			.collect (
				Collectors.toList ());

	}

	@Override
	public
	RecordType findByParentAndCode (
			@NonNull GlobalId parentGlobalId,
			@NonNull String code) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findByParentAndCode",
				parentGlobalId.toString (),
				code);

		Session session =
			hibernateDatabase.currentSession ();

		if (
			isNotNull (
				model.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s must be looked up by type code",
					getClass ().getSimpleName ()));

		}

		if (model.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s for rooted object in %s.%s",
						parentGlobalId,
						getClass ().getSimpleName (),
						"findChildByCode"));

			}

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s = :code",
					model.objectName (),
					model.codeField ().name ()))

				.setString (
					"code",
					code)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		} else if (model.parentTypeIsFixed ()) {

			/*
			if (parentGlobalId.getTypeId ()
					!= parentObjectHelperProvider ().objectTypeId ()) {

				throw new IllegalArgumentException (sf (
					"Invalid parent type id %s for %s (should be %s)",
					parentGlobalId.getTypeId (),
					objectClass ().getSimpleName (),
					parentObjectHelperProvider ().objectTypeId ()));

			}
			*/

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentId ",
					model.objectName (),
					model.parentField ().name (),

					"AND _%s.%s = :code",
					model.objectName (),
					model.codeField ().name ()))

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setString (
					"code",
					code)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		} else {

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentTypeId ",
					model.objectName (),
					model.parentTypeField ().name (),

					"AND _%s.%s = :parentId ",
					model.objectName (),
					model.parentIdField ().name (),

					"AND _%s.%s = :code",
					model.objectName (),
					model.codeField ().name ()))

				.setLong (
					"parentTypeId",
					parentGlobalId.typeId ())

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setString (
					"code",
					code)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		}

	}

	@Override
	public
	RecordType findByParentAndIndex (
			@NonNull GlobalId parentGlobalId,
			@NonNull Long index) {

		Session session =
			hibernateDatabase.currentSession ();

		if (
			isNotNull (
				model.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s must be looked up by type code",
					getClass ().getSimpleName ()));

		}

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findByParentAndIndex",
				parentGlobalId.toString (),
				index.toString ());

		if (model.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s for rooted object in %s.%s",
						parentGlobalId,
						getClass ().getSimpleName (),
						"findChildByCode"));

			}

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s = :index",
					model.objectName (),
					model.indexField ().name ()))

				.setLong (
					"index",
					index)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		} else if (model.parentTypeIsFixed ()) {

			/*
			if (parentGlobalId.getTypeId ()
					!= parentObjectHelperProvider ().objectTypeId ()) {

				throw new IllegalArgumentException (sf (
					"Invalid parent type id %s for %s (should be %s)",
					parentGlobalId.getTypeId (),
					objectClass ().getSimpleName (),
					parentObjectHelperProvider ().objectTypeId ()));

			}
			*/

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentId ",
					model.objectName (),
					model.parentField ().name (),

					"AND _%s.%s = :index",
					model.objectName (),
					model.indexField ().name ()))

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setLong (
					"index",
					index)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		} else {

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentTypeId ",
					model.objectName (),
					model.parentTypeField ().name (),

					"AND _%s.%s = :parentId ",
					model.objectName (),
					model.parentIdField ().name (),

					"AND _%s.%s = :index",
					model.objectName (),
					model.indexField ().name ()))

				.setLong (
					"parentTypeId",
					parentGlobalId.typeId ())

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setLong (
					"index",
					index)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		}

	}

	@Override
	public
	RecordType findByParentAndTypeAndCode (
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode,
			@NonNull String code) {

		if (
			isNull (
				model.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Cannot call findAllByParentAndType for '%s', ",
					model.objectName (),
					"because it has no type code field"));

		}

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findByParentAndTypeAndCode",
				parentGlobalId.toString (),
				typeCode,
				code);

		Session session =
			hibernateDatabase.currentSession ();

		if (model.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s for rooted object in %s.%s",
						parentGlobalId,
						getClass ().getSimpleName (),
						"findByParentAndCode"));

			}

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s = :%s ",
					model.objectName (),
					model.typeCodeField ().name (),
					model.typeCodeField ().name (),

					"AND _%s.%s = :%s",
					model.objectName (),
					model.codeField ().name (),
					model.codeField ().name ()))

				.setString (
					model.typeCodeField ().name (),
					typeCode)

				.setString (
					model.codeField ().name (),
					code)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		} else if (model.parentTypeIsFixed ()) {

			/*
			if (parentGlobalId.getTypeId ()
					!= parentObjectHelperProvider ().objectTypeId ()) {

				throw new IllegalArgumentException (sf (
					"Invalid parent type id %s for %s (should be %s)",
					parentGlobalId.getTypeId (),
					objectClass ().getSimpleName (),
					parentObjectHelperProvider ().objectTypeId ()));

			}
			*/

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentId ",
					model.objectName (),
					model.parentField ().name (),

					"AND _%s.%s = :%s ",
					model.objectName (),
					model.typeCodeField ().name (),
					model.typeCodeField ().name (),

					"AND _%s.%s = :%s",
					model.objectName (),
					model.codeField ().name (),
					model.codeField ().name ()))

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setString (
					model.typeCodeField ().name (),
					typeCode)

				.setString (
					model.codeField ().name (),
					code)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		} else {

			List<?> list =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentTypeId ",
					model.objectName (),
					model.parentTypeField ().name (),

					"AND _%s.%s = :parentId ",
					model.objectName (),
					model.parentIdField ().name (),

					"AND _%s.%s = :%s ",
					model.objectName (),
					model.typeCodeField ().name (),
					model.typeCodeField ().name (),

					"AND _%s.%s = :code",
					model.objectName (),
					model.codeField ().name ()))

				.setLong (
					"parentTypeId",
					parentGlobalId.typeId ())

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setString (
					model.typeCodeField ().name (),
					typeCode)

				.setString (
					model.codeField ().name (),
					code)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			if (list.isEmpty ())
				return null;

			@SuppressWarnings ("unchecked")
			RecordType object =
				(RecordType)
				list.get (0);

			return object;

		}

	}

	@Override
	public
	List<RecordType> findAll () {

		Session session =
			hibernateDatabase.currentSession ();

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findAll");

		List<?> objectsUncast =
			session.createQuery (

			stringFormat (
				"FROM %s",
				model.objectClass ().getSimpleName ()))

			.setFlushMode (
				FlushMode.MANUAL)

			.list ();

		@SuppressWarnings ("unchecked")
		List<RecordType> objects =
			(List<RecordType>)
			objectsUncast;

		return objects;

	}

	@Override
	public
	List<RecordType> findAllByParent (
			@NonNull GlobalId parentGlobalId) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findAllByParent",
				parentGlobalId.toString ());

		Session session =
			hibernateDatabase.currentSession ();

		if (model.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s for rooted object in %s.%s",
						parentGlobalId,
						getClass ().getSimpleName (),
						"findChildren"));

			}

			List<?> objectsUncast =
				session.createQuery (

				stringFormat (
					"FROM %s",
					model.objectClass ().getSimpleName ()))

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			@SuppressWarnings ("unchecked")
			List<RecordType> objects =
				(List<RecordType>)
				objectsUncast;

			return objects;

		} else if (model.canGetParent ()) {

			/*

			TODO enable this again

			if (parentGlobalId.getTypeId ()
					!= parentObjectHelperProvider ().objectTypeId ()) {

				throw new IllegalArgumentException (sf (
					"Invalid parent type id %s for %s (should be %s)",
					parentGlobalId.getTypeId (),
					objectClass ().getSimpleName (),
					parentObjectHelperProvider ().objectTypeId ()));

			}

			*/

			List<?> objectsUncast =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentId",
					model.objectName (),
					model.parentField ().name ()))

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			@SuppressWarnings ("unchecked")
			List <RecordType> objects =
				(List <RecordType>)
				objectsUncast;

			return objects;

		} else {

			List <?> objectsUncast =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentTypeId ",
					model.objectName (),
					model.parentTypeField ().name (),

					"AND _%s.%s = :parentId",
					model.objectName (),
					model.parentIdField ().name ()))

				.setLong (
					"parentTypeId",
					parentGlobalId.typeId ())

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			@SuppressWarnings ("unchecked")
			List <RecordType> objects =
				(List <RecordType>)
				objectsUncast;

			return objects;

		}

	}

	private
	void applyParentRestrictions (
			@NonNull Criteria criteria,
			@NonNull GlobalId parentGlobalId) {

		if (model.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s for rooted object in %s.%s",
						parentGlobalId,
						getClass ().getSimpleName (),
						"findChildren"));

			}

		} else if (model.canGetParent ()) {

			/*

			TODO enable this again

			if (parentGlobalId.getTypeId ()
					!= parentObjectHelperProvider ().objectTypeId ()) {

				throw new IllegalArgumentException (sf (
					"Invalid parent type id %s for %s (should be %s)",
					parentGlobalId.getTypeId (),
					objectClass ().getSimpleName (),
					parentObjectHelperProvider ().objectTypeId ()));

			}

			*/

			criteria.add (
				Restrictions.eq (
					stringFormat (
						"_%s.%s.id",
						model.objectName (),
						model.parentField ().name ()),
					parentGlobalId.objectId ()));

		} else {

			criteria.add (
				Restrictions.eq (
					stringFormat (
						"_%s.%s.id",
						model.objectName (),
						model.parentTypeField ().name ()),
					parentGlobalId.typeId ()));

			criteria.add (
				Restrictions.eq (
					stringFormat (
						"_%s.%s",
						model.objectName (),
						model.parentIdField ().name ()),
					parentGlobalId.objectId ()));

		}

	}

	@Override
	public
	List<RecordType> findByParentAndIndexRange (
			@NonNull GlobalId parentGlobalId,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findByParentAndIndexRange",
				parentGlobalId.toString (),
				indexStart.toString (),
				indexEnd.toString ());

		Session session =
			hibernateDatabase.currentSession ();

		Criteria criteria =
			session.createCriteria (
				model.objectClass (),
				"_" + model.objectName ());

		// apply parent restriction

		applyParentRestrictions (
			criteria,
			parentGlobalId);

		// apply index range restriction

		criteria.add (
			Restrictions.ge (
				stringFormat (
					"_%s.%s",
					model.objectName (),
					model.indexField ().name ()),
				indexStart));

		criteria.add (
			Restrictions.lt (
				stringFormat (
					"_%s.%s",
					model.objectName (),
					model.indexField ().name ()),
				indexEnd));

		// order by index ascending

		criteria.addOrder (
			Order.asc (
				stringFormat (
					"_%s.%s",
					model.objectName (),
					model.indexField ().name ())));

		// manual flush mode

		criteria.setFlushMode (
			FlushMode.MANUAL);

		// execute and return

		List<?> objectsUncast =
			criteria.list ();

		@SuppressWarnings ("unchecked")
		List<RecordType> objects =
			(List<RecordType>)
			objectsUncast;

		return objects;

	}

	@Override
	public
	RecordType insert (
			@NonNull RecordType object) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"insert",
				"...");

		model.hooks ().beforeInsert (
			object);

		Session session =
			hibernateDatabase.currentSession ();

		session.save (
			object);

		model.hooks ().afterInsert (
			object);

		return object;

	}

	@Override
	public
	RecordType insertSpecial (
			@NonNull RecordType object) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"insertSpecial",
				stringFormat (
					"id = %s",
					object.getId ()));

		model.hooks ().beforeInsert (
			object);

		Session session =
			hibernateDatabase.currentSession ();

		session.replicate (
			object,
			ReplicationMode.EXCEPTION);

		model.hooks ().afterInsert (
			object);

		return object;

	}

	@Override
	public
	RecordType update (
			@NonNull RecordType object) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"update",
				stringFormat (
					"id = %s",
					object.getId()));

		model.hooks ().beforeUpdate (
			object);

		return object;

	}

	@Override
	public <ObjectType extends EphemeralRecord<RecordType>>
	ObjectType remove (
			@NonNull ObjectType object) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"remove",
				stringFormat (
					"id = %s",
					object.getId ()));

		Session session =
			hibernateDatabase.currentSession ();

		session.delete (
			object);

		return object;

	}

	@Override
	public
	List<RecordType> findAllByParentAndType (
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode) {

		if (
			isNull (
				model.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Cannot call findAllByParentAndType for '%s', ",
					model.objectName (),
					"because it has no type code field"));

		}

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"findAllByParentAndType",
				parentGlobalId.toString (),
				typeCode);

		Session session =
			hibernateDatabase.currentSession ();

		if (model.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s for rooted object in %s.%s",
						parentGlobalId,
						getClass ().getSimpleName (),
						"findChildren"));

			}

			List<?> objectsUncast =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s = :%s",
					model.objectName (),
					model.typeCodeField ().name (),
					model.typeCodeField ().name ()))

				.setString (
					model.typeCodeField ().name (),
					typeCode)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			@SuppressWarnings ("unchecked")
			List<RecordType> objects =
				(List<RecordType>)
				objectsUncast;

			return objects;

		}

		if (model.canGetParent ()) {

			if (
				integerNotEqualSafe (
					parentGlobalId.typeId (),
					model.parentTypeId ())
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent type id %s for %s (should be %s)",
						parentGlobalId.typeId (),
						model.objectName (),
						model.parentTypeId ()));

			}

			List<?> objectsUncast =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentId ",
					model.objectName (),
					model.parentField ().name (),

					"AND _%s.%s = :%s",
					model.objectName (),
					model.typeCodeField ().name (),
					model.typeCodeField ().name ()))

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setString (
					model.typeCodeField ().name (),
					typeCode)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			@SuppressWarnings ("unchecked")
			List<RecordType> objects =
				(List<RecordType>)
				objectsUncast;

			return objects;

		} else {

			List<?> objectsUncast =
				session.createQuery (

				stringFormat (

					"FROM %s _%s ",
					model.objectClass ().getSimpleName (),
					model.objectName (),

					"WHERE _%s.%s.id = :parentTypeId ",
					model.objectName (),
					model.parentTypeField ().name (),

					"AND _%s.%s = :parentId",
					model.objectName (),
					model.parentIdField ().name (),

					"AND _%s.%s = :%s",
					model.objectName (),
					model.typeCodeField ().name (),
					model.typeCodeField ().name ()))

				.setLong (
					"parentTypeId",
					parentGlobalId.typeId ())

				.setLong (
					"parentId",
					parentGlobalId.objectId ())

				.setString (
					model.typeCodeField ().name (),
					typeCode)

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			@SuppressWarnings ("unchecked")
			List<RecordType> objects =
				(List<RecordType>)
				objectsUncast;

			return objects;

		}

	}

	@Override
	public
	RecordType lock (
			@NonNull RecordType object) {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"lock",
				stringFormat (
					"id = %s",
					object.getId ()));

		Session session =
			hibernateDatabase.currentSession ();

		session.flush ();

		session.refresh (
			object,
			LockOptions.UPGRADE);

		return object;

	}

	private
	ActiveTask startTask (
			@NonNull String methodName,
			@NonNull String... arguments) {

		return activityManager.start (
			"hibernate",
			stringFormat (
				"%sHelperProvider.%s (%s)",
				model.objectName (),
				methodName,
				joinWithCommaAndSpace (
					arguments)),
			this);

	}

}
