package wbs.framework.processapi;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("status-response")
public
class ProcessApiStatusResponse {

	@DataAttribute (
		name = "status")
	ProcessApiIcingaStatus status;

	@DataAttribute (
		name = "status-message")
	String statusMessage;

	@DataAttribute (
		name = "additional-messages")
	List <String> additionalMessages;

	public static
	class Builder {

		// state

		List <String> okStatusMessages =
			new ArrayList<> ();

		List <String> warningStatusMessages =
			new ArrayList<> ();

		List <String> criticalStatusMessages =
			new ArrayList<> ();

		List <String> unknownStatusMessages =
			new ArrayList<> ();

		List <String> additionalMessages =
			new ArrayList<> ();

		// utility methods

		public
		Builder okFormat (
				@NonNull String ... arguments) {

			okStatusMessages.add (
				stringFormatArray (
					arguments));

			return this;

		}

		public
		Builder warningFormat (
				@NonNull String ... arguments) {

			warningStatusMessages.add (
				stringFormatArray (
					arguments));

			return this;

		}

		public
		Builder criticalFormat (
				@NonNull String ... arguments) {

			criticalStatusMessages.add (
				stringFormatArray (
					arguments));

			return this;

		}

		public
		Builder unknownFormat (
				@NonNull String ... arguments) {

			unknownStatusMessages.add (
				stringFormatArray (
					arguments));

			return this;

		}

		public
		Builder additionalMessageFormat (
				@NonNull String ... arguments) {

			additionalMessages.add (
				stringFormatArray (
					arguments));

			return this;

		}

		// implementation

		ProcessApiStatusResponse build () {

			ProcessApiIcingaStatus icingaStatus =
				ProcessApiIcingaStatus.ok;

			List <String> statusMessages =
				new ArrayList<> ();

			// critical messages

			if (
				collectionIsNotEmpty (
					criticalStatusMessages)
			) {

				icingaStatus =
					ProcessApiIcingaStatus.combine (
						icingaStatus,
						ProcessApiIcingaStatus.critical);

				statusMessages.addAll (
					criticalStatusMessages);

			}

			// warning messages

			if (
				collectionIsNotEmpty (
					warningStatusMessages)
			) {

				icingaStatus =
					ProcessApiIcingaStatus.combine (
						icingaStatus,
						ProcessApiIcingaStatus.warning);

				statusMessages.addAll (
					warningStatusMessages);

			}

			// unknown messages

			if (
				collectionIsNotEmpty (
					unknownStatusMessages)
			) {

				icingaStatus =
					ProcessApiIcingaStatus.combine (
						icingaStatus,
						ProcessApiIcingaStatus.unknown);

				statusMessages.addAll (
					unknownStatusMessages);

			}

			// ok messages

			if (
				collectionIsNotEmpty (
					okStatusMessages)
			) {

				icingaStatus =
					ProcessApiIcingaStatus.combine (
						icingaStatus,
						ProcessApiIcingaStatus.ok);

				statusMessages.addAll (
					okStatusMessages);

			}

			// return

			return new ProcessApiStatusResponse ()

				.status (
					icingaStatus)

				.statusMessage (
					joinWithCommaAndSpace (
						statusMessages))

				.additionalMessages (
					additionalMessages);

		}

	}

}
