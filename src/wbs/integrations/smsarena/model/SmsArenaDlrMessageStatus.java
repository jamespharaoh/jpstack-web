package wbs.integrations.smsarena.model;

import lombok.Getter;

public
enum SmsArenaDlrMessageStatus {

	delivered (1, "Delivered to phone"),
	undelivered (2, "Undelivered to phone"),
	buffered (4, "Buffered to gateway");

	@Getter
	int ordinal;

	@Getter
	String description;

	SmsArenaDlrMessageStatus (
			int newOrdinal,
			String newDescription) {

		ordinal = newOrdinal;
		description = newDescription;

	}

	public static
	SmsArenaDlrMessageStatus fromInt (
			int ordinal) {

		switch (ordinal) {

		case 1:
			return delivered;

		case 2:
			return undelivered;

		case 4:
			return buffered;
		}

		throw new IllegalArgumentException ();

	}
}
