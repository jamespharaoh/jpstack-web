package wbs.smsapps.photograbber.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull MessageRec billedMessage) {

		return findOne (
			"findByBilledMessage (billedMessage)",
			PhotoGrabberRequestRec.class,

			createCriteria (
				PhotoGrabberRequestRec.class,
				"_photoGrabberRequest")

			.add (
				Restrictions.eq (
					"_photoGrabberRequest.billedMessage",
					billedMessage))

		);

	}

}
