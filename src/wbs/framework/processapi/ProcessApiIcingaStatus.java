package wbs.framework.processapi;

import static wbs.utils.etc.EnumUtils.enumInSafe;

import lombok.NonNull;

public
enum ProcessApiIcingaStatus {

	ok,
	warning,
	critical,
	unknown;

	public static
	ProcessApiIcingaStatus combine (
			@NonNull ProcessApiIcingaStatus ... statuses) {

		// check critical

		if (
			enumInSafe (
				ProcessApiIcingaStatus.critical,
				statuses)
		) {
			return ProcessApiIcingaStatus.critical;
		}

		// check warning

		if (
			enumInSafe (
				ProcessApiIcingaStatus.warning,
				statuses)
		) {
			return ProcessApiIcingaStatus.warning;
		}

		// check unknown

		if (
			enumInSafe (
				ProcessApiIcingaStatus.unknown,
				statuses)
		) {
			return ProcessApiIcingaStatus.unknown;
		}

		// all ok

		return ProcessApiIcingaStatus.ok;

	}

}
