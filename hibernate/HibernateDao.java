package wbs.framework.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public abstract
class HibernateDao {

	// singleton dependencies

	@SingletonDependency
	HibernateDatabase database;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@SuppressWarnings ("resource")
	protected <Record>
	Record get (
			@NonNull Transaction transaction,
			@NonNull Class <Record> theClass,
			@NonNull Long id) {

		Session session =
			transaction.hibernateSession ();

		return theClass.cast (
			session.get (
				theClass,
				id));

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record get (
			@NonNull Transaction transaction,
			@NonNull Class <Record> theClass,
			@NonNull Long id,
			@NonNull LockOptions lockOptions) {

		Session session =
			transaction.hibernateSession ();

		return theClass.cast (
			session.get (
				theClass,
				id,
				lockOptions));

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record load (
			@NonNull Transaction transaction,
			@NonNull Class <Record> theClass,
			@NonNull Long id) {

		Session session =
			transaction.hibernateSession ();

		return theClass.cast (
			session.load (
				theClass,
				id));

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record save (
			@NonNull Transaction transaction,
			@NonNull Record object) {

		Session session =
			transaction.hibernateSession ();

		session.save (
			object);

		return object;

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record delete (
			@NonNull Transaction transaction,
			@NonNull Record object) {

		Session session =
			transaction.hibernateSession ();

		session.delete (
			object);

		return object;

	}

	@Deprecated
	@SuppressWarnings ("resource")
	protected
	Query createQuery (
			@NonNull Transaction transaction,
			@NonNull String query) {

		Session session =
			transaction.hibernateSession ();

		return session.createQuery (
			query);

	}

	@SuppressWarnings ("resource")
	protected
	Criteria createCriteria (
			@NonNull Transaction transaction,
			@NonNull Class <?> theClass) {

		Session session =
			transaction.hibernateSession ();

		return session.createCriteria (
			theClass);

	}

	@SuppressWarnings ("resource")
	protected
	Criteria createCriteria (
			@NonNull Transaction transaction,
			@NonNull Class <?> theClass,
			@NonNull String name) {

		Session session =
			transaction.hibernateSession ();

		return session.createCriteria (
			theClass,
			name);

	}

	@SuppressWarnings ("resource")
	protected
	<Record>
	Record refresh (
			@NonNull Transaction transaction,
			@NonNull Record object) {

		Session session =
			transaction.hibernateSession ();

		session.flush ();

		session.refresh (
			object);

		return object;

	}

	@SuppressWarnings ("resource")
	protected
	<Record>
	Record refresh (
			@NonNull Transaction transaction,
			@NonNull Record object,
			@NonNull LockOptions lockOptions) {

		Session session =
			transaction.hibernateSession ();

		session.flush ();

		session.refresh (
			object,
			lockOptions);

		return object;

	}

	@Deprecated
	protected <Record>
	Record findOne (
			@NonNull Class <Record> theClass,
			@NonNull List<?> list) {

		if (list.isEmpty ())
			return null;

		return theClass.cast (
			list.get (0));

	}

	protected <Record>
	Optional <Record> findOne (
			@NonNull Transaction parentTransaction,
			@NonNull Class <Record> theClass,
			@NonNull Criteria criteria) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOne");

		) {

			// perform the operation

			List <?> objectList =
				criteria.list ();

			// handle empty list

			if (
				collectionIsEmpty (
					objectList)
			) {
				return optionalAbsent ();
			}

			// handle multiple results error

			if (
				collectionDoesNotHaveOneElement (
					objectList)
			) {

				throw new RuntimeException (
					stringFormat (
						"Expected exactly zero or one results but found %s",
						integerToDecimalString (
							objectList.size ())));

			}

			// check the object type

			if (
				isNotInstanceOf (
					theClass,
					objectList.get (0))
			) {
				throw new ClassCastException ();
			}

			// cast and return

			return optionalOf (
				theClass.cast (
					objectList.get (0)));

		}

	}

	protected <Record>
	Record findOneOrNull (
			@NonNull Transaction transaction,
			@NonNull Class <Record> theClass,
			@NonNull Criteria criteria) {

		return optionalOrNull (
			findOne (
				transaction,
				theClass,
				criteria));

	}

	@Deprecated
	protected <Record>
	List <Record> findMany (
			@NonNull Class <Record> theClass,
			@NonNull List <?> list) {

		@SuppressWarnings ("unchecked")
		List <Record> ret =
			(List <Record>) list;

		if (

			collectionIsNotEmpty (
				list)

			&& isNotInstanceOf (
				theClass,
				list.get (0))

		) {

			throw new ClassCastException ();

		}

		return ret;

	}

	protected <Record>
	List <Record> findMany (
			@NonNull Transaction parentTransaction,
			@NonNull Class <Record> theClass,
			@NonNull Criteria criteria) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMany");

		) {

			// perform the operation

			List <?> objectList =
				criteria.list ();

			// check the first object at least is of the right type

			if (

				collectionIsNotEmpty (
					objectList)

				&& isNotInstanceOf (
					theClass,
					objectList.get (0))

			) {

				throw new ClassCastException ();

			}

			// forcibly cast the whole list

			List <Record> recordList =
				genericCastUnchecked (
					objectList);

			// and return

			return recordList;

		}

	}

	protected
	List <Long> findIdsOnly (
			@NonNull List <?> list) {

		List <Long> idList =
			new ArrayList<> ();

		for (
			Object rowObject
				: list
		) {

			Object[] rowArray =
				(Object[])
				rowObject;

			idList.add (
				(Long)
				rowArray [0]);

		}

		return idList;

	}

	protected <RowType extends IdObject>
	List <Optional <RowType>> findOrdered (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <RowType> rowTypeClass,
			@NonNull List <Long> objectIds,
			@NonNull List <?> unorderedList) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findOrdered");

		) {

			HashMap <Long, RowType> indexedList =
				new HashMap<> ();

			for (
				int index = 0;
				index < unorderedList.size ();
				index ++
			) {

				RowType object =
					rowTypeClass.cast (
						unorderedList.get (
							index));

				if (
					isNull (
						object)
				) {

					throw new RuntimeException ();

				}

				Long objectId =
					object.getId ();

				indexedList.put (
					objectId,
					object);

			}

			List <Optional <RowType>> orderedList =
				new ArrayList<> ();

			for (
				Long objectId
					: objectIds
			) {

				RowType object =
					indexedList.get (
						objectId);

				if (
					isNull (
						object)
				) {

					taskLogger.warningFormat (
						"%s with id %s not found",
						rowTypeClass.getSimpleName (),
						integerToDecimalString (
							objectId));

				}

				orderedList.add (
					optionalFromNullable (
						object));

			}

			return orderedList;

		}

	}

}
