package wbs.platform.queue.model;

import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.record.IdObject;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@Data
public
class UserQueueReport
	implements IdObject {

	UserRec user;

	Long messageCount;

	Date firstMessage;
	Date lastMessage;

	Long timeToProcess;

	@Override
	public
	Integer getId () {
		return user.getId ();
	}

}
