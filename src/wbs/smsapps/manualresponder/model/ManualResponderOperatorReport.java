package wbs.smsapps.manualresponder.model;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.record.IdObject;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@Data
public
class ManualResponderOperatorReport
	implements IdObject {

	UserRec user;

	Long numBilled;
	Long numFree;

	public
	Long numTotal () {
		return numBilled () + numFree ();
	}

	@Override
	public
	Long getId () {
		return user.getId ();
	}

}
