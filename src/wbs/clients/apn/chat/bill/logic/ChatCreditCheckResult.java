package wbs.clients.apn.chat.bill.logic;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
enum ChatCreditCheckResult {

	passedStrict,
	passedPrepay,
	passedFree,

	monitor,
	failedBarred,
	failedBlocked,

	failedNoNumber,
	failedNoNetwork,
	failedInvalidNetwork,
	failedNoChatScheme,
	failedPrepayDisabledForNetwork,
	failedReverseBillDisabledForNetwork,

	failedStrict,
	failedPrepay;

	public
	boolean passed () {

		switch (this) {

			case passedStrict:
			case passedPrepay:
			case passedFree:
				return true;

			case monitor:
				return true;

			case failedBarred:
			case failedBlocked:
				return false;

			case failedNoNumber:
			case failedNoNetwork:
			case failedInvalidNetwork:
			case failedNoChatScheme:
			case failedPrepayDisabledForNetwork:
			case failedReverseBillDisabledForNetwork:
				return false;

			case failedStrict:
			case failedPrepay:
				return false;

			default:
				throw new RuntimeException ();

		}

	}

	public
	boolean passedOrBlocked () {

		switch (this) {

			case failedBlocked:
				return true;

			default:
				return passed ();

		}

	}

	public
	boolean failed () {
		return ! passed ();
	}

	public
	String details () {

		return camelToSpaces (
			this.name ());

	}

}
