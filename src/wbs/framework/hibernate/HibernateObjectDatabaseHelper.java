package wbs.framework.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.notEqualSafe;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
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
	HibernateDatabase hibernateDatabase;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	// public implementation

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType find (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			Session session =
				transaction.hibernateSession ();

			RecordType object =
				session.get (
					objectModel.objectClass (),
					id);

			return object;

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public
	List <RecordType> findMany (
			@NonNull Transaction parentTransaction,
			@NonNull List <Long> ids) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMany");

		) {

			if (
				collectionIsEmpty (
					ids)
			) {
				return emptyList ();
			}

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType findByParentAndCode (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull String code) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByParentAndCode");

		) {

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	List <RecordType> findManyByParentAndCode (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull List <String> codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findManyByParentAndCode");

		) {

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType findByParentAndIndex (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull Long index) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByParentAndIndex");

		) {

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType findByParentAndTypeAndCode (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode,
			@NonNull String code) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByParentAndTypeAndCode");

		) {

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

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	List <RecordType> findAll (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAll");

		) {

			Session session =
				transaction.hibernateSession ();

			List <?> objectsUncast =
				session.createQuery (

				stringFormat (
					"FROM %s",
					objectModel.objectClass ().getSimpleName ()))

				.setFlushMode (
					FlushMode.MANUAL)

				.list ();

			return genericCastUnchecked (
				objectsUncast);

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public
	List <RecordType> findNotDeleted (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findNotDeleted");

		) {

			Session session =
				transaction.hibernateSession ();

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

			return genericCastUnchecked (
				objectsUncast);

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public
	List <RecordType> findAllByParent (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAllByParent");

		) {

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	List<RecordType> findByParentAndIndexRange (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByParentAndIndexRange");

		) {

			Session session =
				transaction.hibernateSession ();

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

			return genericCastUnchecked (
				criteria.list ());

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType insert (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"insert");

		) {

			objectModel.hooks ().beforeInsert (
				transaction,
				object);

			Session session =
				transaction.hibernateSession ();

			session.save (
				object);

			objectModel.hooks ().afterInsert (
				transaction,
				object);

			return object;

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType insertSpecial (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"insertSpecial",
					keyEqualsDecimalInteger (
						"id",
						object.getId ()));

		) {

			objectModel.hooks ().beforeInsert (
				transaction,
				object);

			Session session =
				transaction.hibernateSession ();

			session.replicate (
				object,
				ReplicationMode.EXCEPTION);

			objectModel.hooks ().afterInsert (
				transaction,
				object);

			return object;

		}

	}

	@Override
	public
	RecordType update (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update",
					keyEqualsDecimalInteger (
						"id",
						object.getId ()));

		) {

			objectModel.hooks ().beforeUpdate (
				transaction,
				object);

			return object;

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public <ObjectType extends EphemeralRecord<RecordType>>
	ObjectType remove (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"remove",
					keyEqualsDecimalInteger (
						"id",
						object.getId ()));

		) {

			Session session =
				transaction.hibernateSession ();

			session.delete (
				object);

			return object;

		}

	}

	@SuppressWarnings ("resource")
	@Override
	public
	List <RecordType> findAllByParentAndType (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAllByParentAndType");

		) {

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

			Session session =
				transaction.hibernateSession ();

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

	@SuppressWarnings ("resource")
	@Override
	public
	RecordType lock (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update",
					keyEqualsDecimalInteger (
						"id",
						object.getId ()));

		) {

			Session session =
				transaction.hibernateSession ();

			session.flush ();

			session.refresh (
				object,
				LockOptions.UPGRADE);

			return object;

		}

	}

}
