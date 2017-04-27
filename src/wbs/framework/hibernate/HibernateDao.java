package wbs.framework.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
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

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.IdObject;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public abstract
class HibernateDao {

	// singleton dependencies

	@SingletonDependency
	ActivityManager activityManager;

	@SingletonDependency
	HibernateDatabase database;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	protected
	Session session () {

		return database.currentSession ();

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record get (
			@NonNull Class<Record> theClass,
			@NonNull Long id) {

		Session session =
			database.currentSession ();

		return theClass.cast (
			session.get (
				theClass,
				id));

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record get (
			Class <Record> theClass,
			int id,
			LockOptions lockOptions) {

		Session session =
			database.currentSession ();

		return theClass.cast (
			session.get (
				theClass,
				id,
				lockOptions));

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record load (
			Class <Record> theClass,
			int id) {

		Session session =
			database.currentSession ();

		return theClass.cast (
			session.load (
				theClass,
				id));

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record save (
			Record object) {

		Session session =
			database.currentSession ();

		session.save (
			object);

		return object;

	}

	@SuppressWarnings ("resource")
	protected <Record>
	Record delete (
			Record object) {

		Session sess =
			database.currentSession ();

		sess.delete (object);

		return object;

	}

	protected
	void flush () {

		@SuppressWarnings ("resource")
		Session sess =
			database.currentSession ();

		sess.flush ();

	}

	@Deprecated
	@SuppressWarnings ("resource")
	protected
	Query createQuery (
			@NonNull String query) {

		Session session =
			database.currentSession ();

		return session.createQuery (
			query);

	}

	@SuppressWarnings ("resource")
	protected
	Criteria createCriteria (
			@NonNull Class <?> theClass) {

		Session session =
			database.currentSession ();

		return session.createCriteria (
			theClass);

	}

	@SuppressWarnings ("resource")
	protected
	Criteria createCriteria (
			@NonNull Class <?> theClass,
			@NonNull String name) {

		Session session =
			database.currentSession ();

		return session.createCriteria (
			theClass,
			name);

	}

	@SuppressWarnings ("resource")
	protected
	<Record>
	Record refresh (
			@NonNull Record object) {

		Session session =
			database.currentSession ();

		session.flush ();

		session.refresh (
			object);

		return object;

	}

	@SuppressWarnings ("resource")
	protected
	<Record>
	Record refresh (
			Record object,
			LockOptions lockOptions) {

		Session session =
			database.currentSession ();

		session.flush ();

		session.refresh (
			object,
			lockOptions);

		return object;

	}

	@Deprecated
	protected <Record>
	Record findOne (
			@NonNull Class<Record> theClass,
			@NonNull List<?> list) {

		if (list.isEmpty ())
			return null;

		return theClass.cast (
			list.get (0));

	}

	protected <Record>
	Optional <Record> findOne (
			@NonNull String methodName,
			@NonNull Class <Record> theClass,
			@NonNull Criteria criteria) {

		try (

			ActiveTask activeTask =
				activityManager.start (
					"hibernate",
					stringFormat (
						"%s.%s",
						getClass ().getSimpleName (),
						methodName),
					this);

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
						"%s.%s (...) ",
						classNameSimple (
							getClass ()),
						methodName,
						"should only find zero or one results but found %s",
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
			@NonNull String methodName,
			@NonNull Class <Record> theClass,
			@NonNull Criteria criteria) {

		return optionalOrNull (
			findOne (
				methodName,
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
			@NonNull String methodName,
			@NonNull Class <Record> theClass,
			@NonNull Criteria criteria) {

		try (

			ActiveTask activeTask =
				activityManager.start (
					"hibernate",
					stringFormat (
						"%s.%s",
						getClass ().getSimpleName (),
						methodName),
					this);

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

			TaskLogger taskLogger =
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
