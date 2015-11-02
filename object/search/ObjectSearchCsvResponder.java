package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchCsvResponder")
public
class ObjectSearchCsvResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	List<FormFieldSet> formFieldSets;

	@Getter @Setter
	String sessionKey;

	// state

	FormatWriter formatWriter;

	List<Integer> objectIds;

	// implementation

	@Override
	protected
	void setup () {

		formatWriter =
			new FormatWriterWriter (
				requestContext.writer ());

	}

	// implementation

	@Override
	public
	void prepare () {

		// get search results from session

		@SuppressWarnings ("unchecked")
		List<Integer> objectIdsTemp =
			(List<Integer>)
			requestContext.session (
				sessionKey + "Results");

		objectIds =
			objectIdsTemp;

	}

	@Override
	protected
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/csv");

		requestContext.setHeader (
			"Content-Disposition",
			stringFormat (
				"attachment;filename=%s-search.csv",
				camelToHyphen (
					consoleHelper.objectName ())));

	}

	@Override
	protected
	void render () {

		Transaction transaction =
			database.currentTransaction ();

		// write headers

		formFieldLogic.outputCsvHeadings (
			formatWriter,
			formFieldSets);

		// iterate through objects

		int recordCountSinceFlush = 0;

		for (
			Integer objectId
				: objectIds
		) {

			// load object

			// TODO this should be done in batches for efficiency, how?

			Record<?> object =
				consoleHelper.find (
					objectId);

			// write object

			formFieldLogic.outputCsvRow (
				formatWriter,
				formFieldSets,
				object);

			// flush regularly

			if (recordCountSinceFlush == 1000) {

				transaction.flush ();

				recordCountSinceFlush = 0;

			}

		}

	}

}
