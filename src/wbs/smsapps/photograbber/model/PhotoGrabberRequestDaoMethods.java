package wbs.smsapps.photograbber.model;

import wbs.sms.message.core.model.MessageRec;

public
interface PhotoGrabberRequestDaoMethods {

	PhotoGrabberRequestRec findByBilledMessage (
			MessageRec billedMessage);

}