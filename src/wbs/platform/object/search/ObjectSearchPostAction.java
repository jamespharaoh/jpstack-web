package wbs.platform.object.search;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;
import static wbs.utils.etc.PropertyUtils.propertySetAuto;
import static wbs.utils.etc.ReflectionUtils.methodGetRequired;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.joinWithComma;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.RedirectResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;
import wbs.platform.user.model.UserRec;

import wbs.utils.etc.NumberUtils;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchAction")
public
class ObjectSearchPostAction <
	ObjectType extends Record <ObjectType>,
	SearchType extends Serializable,
	ResultType
>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic fieldsLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <RedirectResponder> redirectResponderProvider;

	@PrototypeDependency
	Provider <ObjectSearchCsvResponder <ResultType>>
		objectSearchCsvResponderProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	Class <SearchType> searchClass;

	@Getter @Setter
	String searchDaoMethodName;

	@Getter @Setter
	String resultsDaoMethodName;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	String parentIdKey;

	@Getter @Setter
	String parentIdName;

	@Getter @Setter
	FormFieldSet <SearchType> searchFormFieldSet;

	@Getter @Setter
	Map <String, ObjectSearchResultsMode <ResultType>> resultsModes;

	@Getter @Setter
	String searchResponderName;

	@Getter @Setter
	String fileName;

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goReal",
					true);

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ObjectSearchPostAction.goReal ()",
					this);

		) {

			UserRec user =
				userConsoleLogic.userRequired ();

			// handle new/repeat search buttons

			if (
				optionalIsPresent (
					requestContext.parameter (
						"new-search"))
			) {

				taskLogger.debugFormat (
					"New search");

				userSessionLogic.userDataRemove (
					taskLogger,
					user,
					stringFormat (
						"object_search_%s_results",
						sessionKey));

				transaction.commit ();

				return redirectResponderProvider.get ()

					.targetUrl (
						requestContext.resolveLocalUrl (
							"/" + fileName));

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"repeat-search"))
			) {

				taskLogger.debugFormat (
					"Repeat search");

				userSessionLogic.userDataRemove (
					taskLogger,
					user,
					stringFormat (
						"object_search_%s_results",
						sessionKey));

				transaction.flush ();

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"download-csv"))
			) {

				taskLogger.debugFormat (
					"Download CSV");

				transaction.commit ();

				return objectSearchCsvResponderProvider.get ()

					.consoleHelper (
						consoleHelper)

					.resultsModes (
						resultsModes)

					.resultsDaoMethodName (
						resultsDaoMethodName)

					.sessionKey (
						sessionKey);

			}

			// perform search

			taskLogger.debugFormat (
				"Process search form");

			SearchType search =
				genericCastUnchecked (
					optionalOrElse (
						userSessionLogic.userDataObject (
							taskLogger,
							user,
							stringFormat (
								"object_search_%s_fields",
								sessionKey)),
						() -> classInstantiate (
							searchClass)));

			fieldsLogic.implicit (
				taskLogger,
				searchFormFieldSet,
				search);

			consoleHelper.consoleHooks ().applySearchFilter (
				taskLogger,
				search);

			if (
				parentIdKey != null
				|| parentIdName != null
			) {

				if (
					parentIdKey == null
					|| parentIdName == null
				) {
					throw new RuntimeException ();
				}

				Object parentId =
					requestContext.stuff (
						parentIdKey);

				if (parentId == null) {
					throw new RuntimeException ();
				}

				propertySetAuto (
					search,
					parentIdName,
					parentId);

			}

			// update search details

			UpdateResultSet updateResultSet =
				fieldsLogic.update (
					taskLogger,
					requestContext,
					searchFormFieldSet,
					search,
					emptyMap (),
					"search");

			userSessionLogic.userDataObjectStore (
				taskLogger,
				user,
				stringFormat (
					"object_search_%s_fields",
					sessionKey),
				search);

			if (updateResultSet.errorCount () > 0) {

				taskLogger.debugFormat (
					"Found %s errors processing search form",
					integerToDecimalString (
						updateResultSet.errorCount ()));

				fieldsLogic.reportErrors (
					requestContext,
					updateResultSet,
					"search");

				requestContext.request (
					"objectSearchUpdateResultSet",
					updateResultSet);

				transaction.commit ();

				return responder (
					searchResponderName);

			}

			// perform search

			taskLogger.debugFormat (
				"Perform search");

			List <Long> objectIds;

			if (
				isNotNull (
					searchDaoMethodName)
			) {

				Method method =
					methodGetRequired (
						consoleHelper.getClass (),
						searchDaoMethodName,
						ImmutableList.<Class <?>> of (
							TaskLogger.class,
							searchClass));

				objectIds =
					genericCastUnchecked (
						methodInvoke (
							method,
							consoleHelper,
							taskLogger,
							search));

			} else {

				objectIds =
					consoleHelper.searchIds (
						taskLogger,
						search);

			}

			if (objectIds.isEmpty ()) {

				// no results

				taskLogger.debugFormat (
					"Search returned no results");

				requestContext.addError (
					"No results returned from search");

				transaction.commit ();

				return responder (
					searchResponderName);

			} else if (

				objectIds.size () == 1

				&& isNull (
					resultsDaoMethodName)

			) {

				// single result

				taskLogger.debugFormat (
					"Search returned single result");

				ConsoleContextType targetContextType =
					consoleManager.contextType (
						consoleHelper.objectName () + ":combo",
						true);

				Optional <ConsoleContext> targetContext =
					consoleManager.relatedContext (
						taskLogger,
						requestContext.consoleContextRequired (),
						targetContextType);

				if (
					optionalIsPresent (
						targetContext)
				) {

					transaction.commit ();

					return redirectResponderProvider.get ()

						.targetUrl (
							requestContext.resolveContextUrlFormat (
								"%s",
								targetContext.get ().pathPrefix (),
								"/%s",
								consoleHelper.getPathId (
									taskLogger,
									objectIds.get (
										0))));

				} else {

					Record <?> object =
						consoleHelper.findRequired (
							objectIds.get (
								0));

					transaction.commit ();

					return redirectResponderProvider.get ()

						.targetUrl (
							requestContext.resolveLocalUrl (
								consoleHelper.getDefaultLocalPathGeneric (
									taskLogger,
									object)));


				}

			} else {

				// multiple results

				taskLogger.debugFormat (
					"Search returned %s results",
					integerToDecimalString (
						collectionSize (
							objectIds)));

				userSessionLogic.userDataStringStore (
					taskLogger,
					user,
					stringFormat (
						"object_search_%s_results",
						sessionKey),
					joinWithComma (
						iterableMap (
							NumberUtils::integerToDecimalString,
							objectIds)));

				transaction.commit ();

				return redirectResponderProvider.get ()

					.targetUrl (
						requestContext.resolveLocalUrl (
							"/" + fileName));

			}

		}

	}

}
