package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotInstanceOf;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;

import wbs.framework.record.IdObject;

@Log4j
public abstract
class HibernateDao {

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

	protected <Record>
	Record findOne (
			Class<Record> theClass,
			List<?> list) {

		if (list.isEmpty ())
			return null;

		return theClass.cast (
			list.get (0));

	}

	protected <Record>
	List<Record> findMany (
			Class<Record> theClass,
			List<?> list) {

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
