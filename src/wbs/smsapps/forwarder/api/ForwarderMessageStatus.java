package wbs.smsapps.forwarder.api;

public
enum ForwarderMessageStatus {

	pending (
		0x000,
		"pending"),

	sent (
		0x001,
		"sent"),

	sentUpstream (
		0x002,
		"sent-upstream"),

	cancelled (
		0x100,
		"cancelled"),

	failed (
		0x101,
		"failed"),

	undelivered (
		0x102,
		"undelivered"),

	reportTimedOut (
		0x103,
		"report-timed-out"),

	delivered (
		0x200,
		"delivered");

	private final
	int status;

	private final
	String statusCode;

	private
	ForwarderMessageStatus (
			int newStatus,
			String newStatusCode) {

		status = newStatus;
		statusCode = newStatusCode;

	}

	public
	int status () {
		return status;
	}

	public
	String statusCode () {
		return statusCode;
	}

}
