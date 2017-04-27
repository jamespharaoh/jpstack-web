package wbs.platform.object.search;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.OptionalUtils.presentInstancesList;
import static wbs.utils.etc.ReflectionUtils.methodGetRequired;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitComma;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;

import wbs.utils.etc.NumberUtils;
import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchCsvResponder")
public
class ObjectSearchCsvResponder <ResultType>
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	// properties

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	@Getter @Setter
	Map <String, ObjectSearchResultsMode <ResultType>> resultsModes;

	@Getter @Setter
	String resultsDaoMethodName;

	@Getter @Setter
	String sessionKey;

	// state

	FormatWriter formatWriter;

	List <FormFieldSet <ResultType>> formFieldSets =
		new ArrayList<> ();

	Object searchObject;
	List <Long> objectIds;

	// implementation

	@Override
	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter =
			requestContext.formatWriter ();

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			// set search object

			searchObject =
				userSessionLogic.userDataObjectRequired (
					taskLogger,
					userConsoleLogic.userRequired (),
					stringFormat (
						"object_search_%s_fields",
						sessionKey));

			// get object ids

			List <Long> objectIdsTemp =
				iterableMapToList (
					NumberUtils::parseIntegerRequired,
					stringSplitComma (
						userSessionLogic.userDataStringRequired (
							userConsoleLogic.userRequired (),
							stringFormat (
								"object_search_%s_results",
								sessionKey))));

			objectIds =
				objectIdsTemp;

		}

	}


	@Override
	protected
	void setHtmlHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setHtmlHeaders");

		) {

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

	}

	@Override
	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"render ()");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			// form fields

			String resultsModeName =
				requestContext.parameterOrDefault (
					"mode",
					resultsModes.keySet ().iterator ().next ());

			ObjectSearchResultsMode <ResultType> resultsMode =
				mapItemForKeyRequired (
					resultsModes,
					resultsModeName);

			formFieldSets =
				presentInstancesList (
					resultsMode.columns,
					resultsMode.rows);

			// write headers

			formFieldLogic.outputCsvHeadings (
				formatWriter,
				formFieldSets);

			// iterate through objects

			int batchesSinceFlush = 0;

			for (
				List <Long> batch
					: Lists.partition (
						objectIds,
						64)
			) {

				List <Optional <ResultType>> objects;

				if (
					isNotNull (
						resultsDaoMethodName)
				) {

					Method method =
						methodGetRequired (
							consoleHelper.getClass (),
							resultsDaoMethodName,
							ImmutableList.<Class<?>> of (
								searchObject.getClass (),
								List.class));

					objects =
						genericCastUnchecked (
							methodInvoke (
								method,
								consoleHelper,
								searchObject,
								batch));

				} else {

					objects =
						genericCastUnchecked (
							consoleHelper.findMany (
								batch));

				}

				for (
					ResultType object
						: presentInstances (
							objects)
				) {

					// write object

					formFieldLogic.outputCsvRow (
						taskLogger,
						formatWriter,
						formFieldSets,
						object,
						emptyMap ());

				}

				// flush regularly

				if (++ batchesSinceFlush == 64) {

					transaction.flush ();

					batchesSinceFlush = 0;

				}

			}

		}

	}

}
