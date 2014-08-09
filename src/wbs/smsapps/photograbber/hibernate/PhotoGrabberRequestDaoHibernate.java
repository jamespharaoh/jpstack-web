package wbs.smsapps.photograbber.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.core.model.MessageRec;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestDao;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestRec;

public
class PhotoGrabberRequestDaoHibernate
	extends HibernateDao
	implements PhotoGrabberRequestDao {

	@Override
	public
	PhotoGrabberRequestRec findByBilledMessage (
			MessageRec billedMessage) {

		return findOne (
			PhotoGrabberRequestRec.class,

			createQuery (
				"FROM PhotoGrabberRequestRec pgr " +
				"WHERE pgr.billedMessage = :billedMessage")

			.setEntity (
				"billedMessage",
				billedMessage)

			.list ());

	}

}
