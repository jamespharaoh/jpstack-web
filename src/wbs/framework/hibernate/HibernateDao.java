package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isEmptyString;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotInstanceOf;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.shouldNeverHappen;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.record.IdObject;

@Log4j
public abstract
class HibernateDao {

	@Inject
	ActivityManager activityManager;

	@Inject
	HibernateDatabase database;

	protected
	Session session () {

		return database.currentSession ();

	}

	protected <Record>
	Record get (
			Class<Record> theClass,
			int id) {

		Session session =
			database.currentSession ();

		return theClass.cast (
			session.get (
				theClass,
				id));

	}

	protected <Record>
	Record get (
			Class<Record> theClass,
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

	protected <Record>
	Record load (
			Class<Record> theClass,
			int id) {

		Session session =
			database.currentSession ();

		return theClass.cast (
			session.load (
				theClass,
				id));

	}

	protected <Record>
	Record save (
			Record object) {

		Session session =
			database.currentSession ();

		session.save (object);

		return object;

	}

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

		Session sess =
			database.currentSession ();

		sess.flush ();

	}

	@Deprecated
	protected
	Query createQuery (
			String query) {

		Session session =
			database.currentSession ();

		return session.createQuery (query);

	}

	protected
	Criteria createCriteria (
			Class<?> theClass) {

		Session session =
			database.currentSession ();

		return session.createCriteria (theClass);

	}

	protected
	Criteria createCriteria (
			Class<?> theClass,
			String name) {

		Session session =
			database.currentSession ();

		return session.createCriteria (
			theClass,
			name);

	}

	protected
	<Record>
	Record refresh (
			Record object) {

		Session session =
			database.currentSession ();

		session.flush ();

		session.refresh (
			object);

		return object;

	}

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
	Record findOne (
			@NonNull String methodName,
			@NonNull Class<Record> theClass,
			@NonNull Criteria criteria) {

		@Cleanup
		ActiveTask activeTask =
			activityManager.start (
				"hibernate",
				stringFormat (
					"%s.%s",
					getClass ().getSimpleName (),
					methodName),
				this);

		// perform the operation

		List<?> objectList =
			criteria.list ();

		// handle empty list

		if (
			isEmpty (
				objectList)
		) {
			return null;
		}

		// handle multiple results error

		if (
			notEqual (
				objectList.size (),
				1)
		) {
			shouldNeverHappen ();
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

		return theClass.cast (
			objectList.get (0));

	}

	@Deprecated
	protected <Record>
	List<Record> findMany (
			@NonNull Class<Record> theClass,
			@NonNull List<?> list) {

		@SuppressWarnings ("unchecked")
		List<Record> ret =
			(List<Record>) list;

		if (

			isNotEmpty (
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
	List<Record> findMany (
			@NonNull String methodName,
			@NonNull Class<Record> theClass,
			@NonNull Criteria criteria) {

		@Cleanup
		ActiveTask activeTask =
			activityManager.start (
				"hibernate",
				stringFormat (
					"%s.%s",
					getClass ().getSimpleName (),
					methodName),
				this);

		// perform the operation

		List<?> objectList =
			criteria.list ();

		// check the first object at least is of the right type

		if (

			isNotEmpty (
				objectList)

			&& isNotInstanceOf (
				theClass,
				objectList.get (0))

		) {

			throw new ClassCastException ();

		}

		// forcibly cast the whole list

		@SuppressWarnings ("unchecked")
		List<Record> recordList =
			(List<Record>) objectList;

		// and return

		return recordList;

	}

	protected
	List<Integer> findIdsOnly (
			List<?> list) {

		List<Integer> idList =
			new ArrayList<Integer> ();

		for (
			Object rowObject
				: list
		) {

			Object[] rowArray =
				(Object[])
				rowObject;

			idList.add (
				(Integer)
				rowArray [0]);

		}

		return idList;

	}

	protected <RowType extends IdObject>
	List<RowType> findOrdered (
			Class<RowType> rowTypeClass,
			List<Integer> objectIds,
			List<?> unorderedList) {

		HashMap<Integer,RowType> indexedList =
			new HashMap<Integer,RowType> ();

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

			Integer objectId =
				object.getId ();

			indexedList.put (
				objectId,
				object);

		}

		List<RowType> orderedList =
			new ArrayList<RowType> ();

		for (
			Integer objectId
				: objectIds
		) {

			RowType object =
				indexedList.get (
					objectId);

			if (
				isNull (
					object)
			) {

				log.warn (
					stringFormat (
						"%s with id %s not found",
						rowTypeClass.getSimpleName (),
						ifNull (
							objectId,
							"null")));

			}

			orderedList.add (
				object);

		}

		return orderedList;

	}

}
