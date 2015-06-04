package wbs.integrations.smsarena.model;

import lombok.Getter;

public
enum SmsArenaDescMessageStatus {

	unknownError (2, "Unknown error"),
	unknownSubscriber (41, "Unknown subscriber MSISDN");

	@Getter
	int ordinal;

	@Getter
	String description;

	SmsArenaDescMessageStatus (
			int newOrdinal,
			String newDescription) {

		ordinal = newOrdinal;
		description = newDescription;

	}

	public static
	SmsArenaDescMessageStatus fromInt (
			int ordinal) {

		switch (ordinal) {

		case 2:
			return unknownError;

		case 41:
			return unknownSubscriber;

		}

		throw new IllegalArgumentException ();

	}
}