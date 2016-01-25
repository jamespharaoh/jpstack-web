package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.getMethodRequired;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.methodInvoke;
import static wbs.framework.utils.etc.Misc.requiredValue;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.IdObject;
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
	String resultsDaoMethodName;

	@Getter @Setter
	String sessionKey;

	// state

	FormatWriter formatWriter;

	Object searchObject;
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

		// set search object

		searchObject =
			requiredValue (
				requestContext.session (
					sessionKey + "Fields"));

		// get object ids

		@SuppressWarnings ("unchecked")
		List<Integer> objectIdsTemp =
			(List<Integer>)
			requiredValue (
				requestContext.session (
					sessionKey + "Results"));

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

		int batchesSinceFlush = 0;

		for (
			List<Integer> batch
				: Lists.partition (
					objectIds,
					64)
		) {

			List<IdObject> objects;

			if (
				isNotNull (
					resultsDaoMethodName)
			) {

				Method method =
					getMethodRequired (
						consoleHelper.getClass (),
						resultsDaoMethodName,
						ImmutableList.<Class<?>>of (
							searchObject.getClass (),
							List.class));

				@SuppressWarnings ("unchecked")
				List<IdObject> objectsTemp =
					(List<IdObject>)
					methodInvoke (
						method,
						consoleHelper,
						ImmutableList.<Object>of (
							searchObject,
							batch));

				objects =
					objectsTemp;

			} else {

				objects =
					new ArrayList<IdObject> ();

				for (
					Integer objectId
						: batch
				) {

					objects.add (
						consoleHelper.find (
							objectId));

				}

			}

			for (
				Object object
					: objects
			) {

				// write object

				formFieldLogic.outputCsvRow (
					formatWriter,
					formFieldSets,
					object);

			}

			// flush regularly

			if (++ batchesSinceFlush == 64) {

				transaction.flush ();

				batchesSinceFlush = 0;

			}

		}

	}

}
