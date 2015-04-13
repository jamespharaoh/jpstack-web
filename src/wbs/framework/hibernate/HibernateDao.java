package wbs.framework.hibernate;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;

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

		return ret;

	}

}
