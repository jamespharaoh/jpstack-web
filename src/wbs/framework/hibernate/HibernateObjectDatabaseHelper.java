package wbs.framework.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.notEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

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
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	// public implementation

	@Override
	public
	RecordType find (
			@NonNull Long id) {

		Session session =
			hibernateDatabase.currentSession ();

		RecordType object =
			session.get (
				objectModel.objectClass (),
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

		try (

			ActiveTask activeTask =
				startTask (
					"findMany",
					"list of %s ids");

		) {

			Session session =
				hibernateDatabase.currentSession ();

			Criteria criteria =
				session.createCriteria (
					objectModel.objectClass ());

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

	}

	@Override
	public
	RecordType findByParentAndCode (
			@NonNull GlobalId parentGlobalId,
			@NonNull String code) {

		try (

			ActiveTask activeTask =
				startTask (
					"findByParentAndCode",
					parentGlobalId.toString (),
					code);

		) {

			Session session =
				hibernateDatabase.currentSession ();

			if (
				isNotNull (
					objectModel.typeCodeField ())
			) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s must be looked up by type code",
						getClass ().getSimpleName ()));

			}

			if (objectModel.isRooted ()) {

				if (
					notEqualSafe (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s ",
							parentGlobalId.toString (),
							"for rooted object in %s.%s",
							getClass ().getSimpleName (),
							"findChildByCode"));

				}

				List<?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s = :code",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

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

			} else if (objectModel.parentTypeIsFixed ()) {

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

				if (
					isNull (
						objectModel.parentField ())
				) {

					throw new UnsupportedOperationException (
						stringFormat (
							"%sObjectHelper.findByParentAndCode (...)",
							objectModel.objectName ()));

				}

				List <?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentId ",
						objectModel.objectName (),
						objectModel.parentField ().name (),

						"AND _%s.%s = :code",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectModel.objectName (),
						objectModel.parentTypeField ().name (),

						"AND _%s.%s = :parentId ",
						objectModel.objectName (),
						objectModel.parentIdField ().name (),

						"AND _%s.%s = :code",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

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

	}

	@Override
	public
	List <RecordType> findManyByParentAndCode (
			@NonNull GlobalId parentGlobalId,
			@NonNull List <String> codes) {

		try (

			ActiveTask activeTask =
				startTask (
					"findManyByParentAndCode",
					parentGlobalId.toString (),
					stringFormat (
						"%s codes",
						integerToDecimalString (
							collectionSize (codes))));

		) {

			Session session =
				hibernateDatabase.currentSession ();

			if (
				isNotNull (
					objectModel.typeCodeField ())
			) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s must be looked up by type code",
						getClass ().getSimpleName ()));

			}

			if (objectModel.isRooted ()) {

				if (
					notEqualSafe (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s ",
							parentGlobalId.toString (),
							"for rooted object in %s.%s",
							getClass ().getSimpleName (),
							"findChildByCode"));

				}

				List <?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s IN :codes",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

					.setParameterList (
						"codes",
						codes)

					.setFlushMode (
						FlushMode.MANUAL)

					.list ();

				return genericCastUnchecked (
					list);

			} else if (objectModel.parentTypeIsFixed ()) {

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

				if (
					isNull (
						objectModel.parentField ())
				) {

					throw new UnsupportedOperationException (
						stringFormat (
							"%sObjectHelper.findByParentAndCode (...)",
							objectModel.objectName ()));

				}

				List <?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentId ",
						objectModel.objectName (),
						objectModel.parentField ().name (),

						"AND _%s.%s IN :codes",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

					.setLong (
						"parentId",
						parentGlobalId.objectId ())

					.setParameterList (
						"codes",
						codes)

					.setFlushMode (
						FlushMode.MANUAL)

					.list ();

				return genericCastUnchecked (
					list);

			} else {

				List <?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectModel.objectName (),
						objectModel.parentTypeField ().name (),

						"AND _%s.%s = :parentId ",
						objectModel.objectName (),
						objectModel.parentIdField ().name (),

						"AND _%s.%s IN :codes",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

					.setLong (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setLong (
						"parentId",
						parentGlobalId.objectId ())

					.setParameterList (
						"codes",
						codes)

					.setFlushMode (
						FlushMode.MANUAL)

					.list ();

				return genericCastUnchecked (
					list);

			}

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
				objectModel.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s must be looked up by type code",
					getClass ().getSimpleName ()));

		}

		try (

			ActiveTask activeTask =
				startTask (
					"findByParentAndIndex",
					parentGlobalId.toString (),
					index.toString ());

		) {

			if (objectModel.isRooted ()) {

				if (
					notEqualSafe (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s ",
							parentGlobalId.toString (),
							"for rooted object in %s.%s",
							getClass ().getSimpleName (),
							"findChildByCode"));

				}

				List<?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s = :index",
						objectModel.objectName (),
						objectModel.indexField ().name ()))

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

			} else if (objectModel.parentTypeIsFixed ()) {

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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentId ",
						objectModel.objectName (),
						objectModel.parentField ().name (),

						"AND _%s.%s = :index",
						objectModel.objectName (),
						objectModel.indexField ().name ()))

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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectModel.objectName (),
						objectModel.parentTypeField ().name (),

						"AND _%s.%s = :parentId ",
						objectModel.objectName (),
						objectModel.parentIdField ().name (),

						"AND _%s.%s = :index",
						objectModel.objectName (),
						objectModel.indexField ().name ()))

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

	}

	@Override
	public
	RecordType findByParentAndTypeAndCode (
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode,
			@NonNull String code) {

		if (
			isNull (
				objectModel.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Cannot call findAllByParentAndType for '%s', ",
					objectModel.objectName (),
					"because it has no type code field"));

		}

		try (

			ActiveTask activeTask =
				startTask (
					"findByParentAndTypeAndCode",
					parentGlobalId.toString (),
					typeCode,
					code);

		) {

			Session session =
				hibernateDatabase.currentSession ();

			if (objectModel.isRooted ()) {

				if (
					notEqualSafe (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s ",
							parentGlobalId.toString (),
							"for rooted object in %s.%s",
							getClass ().getSimpleName (),
							"findByParentAndCode"));

				}

				List<?> list =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s = :%s ",
						objectModel.objectName (),
						objectModel.typeCodeField ().name (),
						objectModel.typeCodeField ().name (),

						"AND _%s.%s = :%s",
						objectModel.objectName (),
						objectModel.codeField ().name (),
						objectModel.codeField ().name ()))

					.setString (
						objectModel.typeCodeField ().name (),
						typeCode)

					.setString (
						objectModel.codeField ().name (),
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

			} else if (objectModel.parentTypeIsFixed ()) {

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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentId ",
						objectModel.objectName (),
						objectModel.parentField ().name (),

						"AND _%s.%s = :%s ",
						objectModel.objectName (),
						objectModel.typeCodeField ().name (),
						objectModel.typeCodeField ().name (),

						"AND _%s.%s = :%s",
						objectModel.objectName (),
						objectModel.codeField ().name (),
						objectModel.codeField ().name ()))

					.setLong (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						objectModel.typeCodeField ().name (),
						typeCode)

					.setString (
						objectModel.codeField ().name (),
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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectModel.objectName (),
						objectModel.parentTypeField ().name (),

						"AND _%s.%s = :parentId ",
						objectModel.objectName (),
						objectModel.parentIdField ().name (),

						"AND _%s.%s = :%s ",
						objectModel.objectName (),
						objectModel.typeCodeField ().name (),
						objectModel.typeCodeField ().name (),

						"AND _%s.%s = :code",
						objectModel.objectName (),
						objectModel.codeField ().name ()))

					.setLong (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setLong (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						objectModel.typeCodeField ().name (),
						typeCode)

					.setString (
						objectModel.codeField ().name (),
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

	}

	@Override
	public
	List <RecordType> findAll () {

		Session session =
			hibernateDatabase.currentSession ();

		try (

			ActiveTask activeTask =
				startTask (
					"findAll");

		) {

			List <?> objectsUncast =
				session.createQuery (

				stringFormat (
					"FROM %s",
					objectModel.objectClass ().getSimpleName ()))

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
	List <RecordType> findNotDeleted () {

		Session session =
			hibernateDatabase.currentSession ();

		try (

			ActiveTask activeTask =
				startTask (
					"findAll");

		) {

			List <?> objectsUncast =
				session.createCriteria (
					objectModel.objectClass (),
					"_subject")

				.add (
					ifNotNullThenElse (
						objectModel.deletedField (),
						() -> Restrictions.eq (
							stringFormat (
								"_subject.%s",
								objectModel.deletedField ().columnName ()),
							false),
						() -> Restrictions.sqlRestriction (
							"true")))


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
	List<RecordType> findAllByParent (
			@NonNull GlobalId parentGlobalId) {

		try (

			ActiveTask activeTask =
				startTask (
					"findAllByParent",
					parentGlobalId.toString ());

		) {

			Session session =
				hibernateDatabase.currentSession ();

			if (objectModel.isRooted ()) {

				if (
					notEqualSafe (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s ",
							parentGlobalId.toString (),
							"for rooted object in %s.%s",
							getClass ().getSimpleName (),
							"findChildren"));

				}

				List<?> objectsUncast =
					session.createQuery (

					stringFormat (
						"FROM %s",
						objectModel.objectClass ().getSimpleName ()))

					.setFlushMode (
						FlushMode.MANUAL)

					.list ();

				@SuppressWarnings ("unchecked")
				List<RecordType> objects =
					(List<RecordType>)
					objectsUncast;

				return objects;

			} else if (objectModel.canGetParent ()) {

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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentId",
						objectModel.objectName (),
						objectModel.parentField ().name ()))

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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectModel.objectName (),
						objectModel.parentTypeField ().name (),

						"AND _%s.%s = :parentId",
						objectModel.objectName (),
						objectModel.parentIdField ().name ()))

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

	}

	private
	void applyParentRestrictions (
			@NonNull Criteria criteria,
			@NonNull GlobalId parentGlobalId) {

		if (objectModel.isRooted ()) {

			if (
				notEqualSafe (
					parentGlobalId,
					GlobalId.root)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid parent global id %s ",
						parentGlobalId.toString (),
						"for rooted object in %s.%s",
						getClass ().getSimpleName (),
						"findChildren"));

			}

		} else if (objectModel.canGetParent ()) {

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
						objectModel.objectName (),
						objectModel.parentField ().name ()),
					parentGlobalId.objectId ()));

		} else {

			criteria.add (
				Restrictions.eq (
					stringFormat (
						"_%s.%s.id",
						objectModel.objectName (),
						objectModel.parentTypeField ().name ()),
					parentGlobalId.typeId ()));

			criteria.add (
				Restrictions.eq (
					stringFormat (
						"_%s.%s",
						objectModel.objectName (),
						objectModel.parentIdField ().name ()),
					parentGlobalId.objectId ()));

		}

	}

	@Override
	public
	List<RecordType> findByParentAndIndexRange (
			@NonNull GlobalId parentGlobalId,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		try (

			ActiveTask activeTask =
				startTask (
					"findByParentAndIndexRange",
					parentGlobalId.toString (),
					indexStart.toString (),
					indexEnd.toString ());

		) {

			Session session =
				hibernateDatabase.currentSession ();

			Criteria criteria =
				session.createCriteria (
					objectModel.objectClass (),
					"_" + objectModel.objectName ());

			// apply parent restriction

			applyParentRestrictions (
				criteria,
				parentGlobalId);

			// apply index range restriction

			criteria.add (
				Restrictions.ge (
					stringFormat (
						"_%s.%s",
						objectModel.objectName (),
						objectModel.indexField ().name ()),
					indexStart));

			criteria.add (
				Restrictions.lt (
					stringFormat (
						"_%s.%s",
						objectModel.objectName (),
						objectModel.indexField ().name ()),
					indexEnd));

			// order by index ascending

			criteria.addOrder (
				Order.asc (
					stringFormat (
						"_%s.%s",
						objectModel.objectName (),
						objectModel.indexField ().name ())));

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

	}

	@Override
	public
	RecordType insert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"insert");

		try (

			ActiveTask activeTask =
				startTask (
					"insert",
					"...");

		) {

			objectModel.hooks ().beforeInsert (
				taskLogger,
				object);

			Session session =
				hibernateDatabase.currentSession ();

			session.save (
				object);

			objectModel.hooks ().afterInsert (
				taskLogger,
				object);

			return object;

		}

	}

	@Override
	public
	RecordType insertSpecial (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"insertSpecial");

		try (

			ActiveTask activeTask =
				startTask (
					"insertSpecial",
					stringFormat (
						"id = %s",
						integerToDecimalString (
							object.getId ())));

		) {

			objectModel.hooks ().beforeInsert (
				taskLogger,
				object);

			Session session =
				hibernateDatabase.currentSession ();

			session.replicate (
				object,
				ReplicationMode.EXCEPTION);

			objectModel.hooks ().afterInsert (
				taskLogger,
				object);

			return object;

		}

	}

	@Override
	public
	RecordType update (
			@NonNull RecordType object) {

		try (

			ActiveTask activeTask =
				startTask (
					"update",
					stringFormat (
						"id = %s",
						integerToDecimalString (
							object.getId ())));

		) {

			objectModel.hooks ().beforeUpdate (
				object);

			return object;

		}

	}

	@Override
	public <ObjectType extends EphemeralRecord<RecordType>>
	ObjectType remove (
			@NonNull ObjectType object) {

		try (

			ActiveTask activeTask =
				startTask (
					"remove",
					stringFormat (
						"id = %s",
						integerToDecimalString (
							object.getId ())));

		) {

			Session session =
				hibernateDatabase.currentSession ();

			session.delete (
				object);

			return object;

		}

	}

	@Override
	public
	List<RecordType> findAllByParentAndType (
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode) {

		if (
			isNull (
				objectModel.typeCodeField ())
		) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Cannot call findAllByParentAndType for '%s', ",
					objectModel.objectName (),
					"because it has no type code field"));

		}

		try (

			ActiveTask activeTask =
				startTask (
					"findAllByParentAndType",
					parentGlobalId.toString (),
					typeCode);

		) {

			Session session =
				hibernateDatabase.currentSession ();

			if (objectModel.isRooted ()) {

				if (
					notEqualSafe (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s ",
							parentGlobalId.toString (),
							"for rooted object in %s.%s",
							getClass ().getSimpleName (),
							"findChildren"));

				}

				List<?> objectsUncast =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s = :%s",
						objectModel.objectName (),
						objectModel.typeCodeField ().name (),
						objectModel.typeCodeField ().name ()))

					.setString (
						objectModel.typeCodeField ().name (),
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

			if (objectModel.canGetParent ()) {

				if (
					integerNotEqualSafe (
						parentGlobalId.typeId (),
						objectModel.parentTypeId ())
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent type id %s for %s (should be %s)",
							integerToDecimalString (
								parentGlobalId.typeId ()),
							objectModel.objectName (),
							integerToDecimalString (
								objectModel.parentTypeId ())));

				}

				List <?> objectsUncast =
					session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentId ",
						objectModel.objectName (),
						objectModel.parentField ().name (),

						"AND _%s.%s = :%s",
						objectModel.objectName (),
						objectModel.typeCodeField ().name (),
						objectModel.typeCodeField ().name ()))

					.setLong (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						objectModel.typeCodeField ().name (),
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
						objectModel.objectClass ().getSimpleName (),
						objectModel.objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectModel.objectName (),
						objectModel.parentTypeField ().name (),

						"AND _%s.%s = :parentId",
						objectModel.objectName (),
						objectModel.parentIdField ().name (),

						"AND _%s.%s = :%s",
						objectModel.objectName (),
						objectModel.typeCodeField ().name (),
						objectModel.typeCodeField ().name ()))

					.setLong (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setLong (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						objectModel.typeCodeField ().name (),
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

	}

	@Override
	public
	RecordType lock (
			@NonNull RecordType object) {

		try (

			ActiveTask activeTask =
				startTask (
					"lock",
					stringFormat (
						"id = %s",
						integerToDecimalString (
							object.getId ())));

		) {

			Session session =
				hibernateDatabase.currentSession ();

			session.flush ();

			session.refresh (
				object,
				LockOptions.UPGRADE);

			return object;

		}

	}

	private
	ActiveTask startTask (
			@NonNull String methodName,
			@NonNull String... arguments) {

		return activityManager.start (
			"hibernate",
			stringFormat (
				"%sHelperProvider.%s (%s)",
				objectModel.objectName (),
				methodName,
				joinWithCommaAndSpace (
					arguments)),
			this);

	}

}
