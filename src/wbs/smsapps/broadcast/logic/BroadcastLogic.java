package wbs.smsapps.broadcast.logic;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.smsapps.broadcast.model.BroadcastRec;

public
interface BroadcastLogic {

	AddResult addNumbers (
			Transaction parentTransaction,
			BroadcastRec broadcast,
			List <String> numbers,
			UserRec user);

	@Accessors (fluent = true)
	@Data
	public static
	class AddResult {

		int numAlreadyAdded = 0;
		int numAlreadyRejected = 0;
		int numAdded = 0;
		int numRejected = 0;

	}

}
