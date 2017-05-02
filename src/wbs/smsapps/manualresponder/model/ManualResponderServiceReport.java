package wbs.smsapps.manualresponder.model;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.entity.record.IdObject;

@Accessors (fluent = true)
@Data
public
class ManualResponderServiceReport
	implements IdObject {

	ManualResponderRec manualResponder;

	Long numBilled;
	Long numFree;

	public
	Long numTotal () {
		return numBilled () + numFree ();
	}

	@Override
	public
	Long getId () {
		return manualResponder.getId ();
	}

}
