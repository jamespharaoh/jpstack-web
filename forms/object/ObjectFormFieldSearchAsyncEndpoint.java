package wbs.console.forms.object;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listSliceFromStart;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.LogicUtils.predicatesCombineAll;
import static wbs.utils.etc.LogicUtils.predicatesCombineAny;
import static wbs.utils.etc.NullUtils.allAreNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalIfPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstancesList;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringContains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.async.ConsoleAsyncConnectionHandle;
import wbs.console.async.ConsoleAsyncEndpoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.priv.UserPrivCheckerBuilder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectTypeRegistry;

@SingletonComponent ("objectFormFieldSearchAsyncEndpoint")
public
class ObjectFormFieldSearchAsyncEndpoint
	implements ConsoleAsyncEndpoint <ObjectFormFieldSearchRequest> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ObjectTypeRegistry objectTypeRegistry;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <UserPrivCheckerBuilder> privCheckerBuilderProvider;

	// details

	@Override
	public
	String endpointPath () {
		return "/forms/object-search";
	}

	@Override
	public
	Class <ObjectFormFieldSearchRequest> requestClass () {
		return ObjectFormFieldSearchRequest.class;
	}

	// public implementation

	@Override
	public
	Optional <ObjectFormFieldSearchResponse> message (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleAsyncConnectionHandle connectionHandle,
			@NonNull Long userId,
			@NonNull ObjectFormFieldSearchRequest request) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"message");

		) {

			debugFormat (
				"REQUEST: %s",
				request.toString ());

			UserPrivChecker privChecker =
				privCheckerBuilderProvider.provide (
					transaction)

				.userId (
					userId)

				.build (
					transaction)

			;

			ConsoleHelper <?> consoleHelper =
				objectManager.consoleHelperForNameRequired (
					objectTypeRegistry.nameForTypeIdRequired (
						request.objectTypeId ()));

			if (! consoleHelper.consoleHelperProvider ().canSearch ()) {

				transaction.warningFormat (
					"Ignoring attempt to search type: %s",
					consoleHelper.objectName ());

				return optionalAbsent ();

			}

			Optional <Record <?>> rootOptional;

			if (
				allAreNotNull (
					request.rootObjectTypeId (),
					request.rootObjectId ())
			) {

				ConsoleHelper <?> rootConsoleHelper =
					objectManager.consoleHelperForNameRequired (
						objectTypeRegistry.nameForTypeIdRequired (
							request.rootObjectTypeId ()));

				rootOptional =
					genericCastUnchecked (
						rootConsoleHelper.find (
							transaction,
							request.rootObjectId ()));

				if (
					optionalIsNotPresent (
						rootOptional)
				) {
					return optionalAbsent ();
				}

			} else {

				rootOptional =
					optionalAbsent ();

			}

			String searchText =
				lowercase (
					request.searchText ());

			List <Predicate <Record <?>>> filterPredicates =
				presentInstancesList (

				optionalOf (
					object ->
						consoleHelper.canView (
							transaction,
							privChecker,
							genericCastUnchecked (
								object))),

				optionalIfPresent (
					rootOptional,
					() -> object ->
						objectManager.isAncestor (
							transaction,
							object,
							optionalGetRequired (
								rootOptional)))

			);

			List <Predicate <Record <?>>> searchPredicates =
				presentInstancesList (

				optionalIf (
					consoleHelper.codeExists (),
					() -> object ->
						stringContains (
							searchText,
							lowercase (
								consoleHelper.getCode (
									genericCastUnchecked (
										object))))),

				optionalIf (
					consoleHelper.nameExists (),
					() -> object ->
						stringContains (
							searchText,
							lowercase (
								consoleHelper.getName (
									genericCastUnchecked (
										object))))),

				optionalIf (
					consoleHelper.descriptionExists (),
					() -> object ->
						stringContains (
							searchText,
							lowercase (
								consoleHelper.getDescription (
									genericCastUnchecked (
										object)))))

			);

			List <Record <?>> objects =
				iterableFilterToList (
					genericCastUnchecked (
						consoleHelper.findNotDeleted (
							transaction)),
					predicatesCombineAll (
						predicatesCombineAll (
							filterPredicates),
						predicatesCombineAny (
							searchPredicates)));

			debugFormat (
				"Found %s objects",
				integerToDecimalString (
					collectionSize (
						objects)));

			List <ObjectFormFieldSearchResponseItem> responseItems =
				iterableMapToList (
					objects,
					object ->
						new ObjectFormFieldSearchResponseItem ()

				.objectId (
					object.getId ())

				.path (
					objectManager.objectPathMini (
						transaction,
						object,
						rootOptional))

				.code (
					ifThenElse (
						consoleHelper.codeExists (),
						() -> consoleHelper.getCode (
							genericCastUnchecked (
								object)),
						() -> null))

				.name (
					ifThenElse (
						consoleHelper.nameExists (),
						() -> consoleHelper.getName (
							genericCastUnchecked (
								object)),
						() -> null))

				.description (
					ifThenElse (
						consoleHelper.descriptionExists (),
						() -> consoleHelper.getDescription (
							genericCastUnchecked (
								object)),
						() -> null))

			);

			responseItems =
				new ArrayList<> (
					responseItems);

			Collections.sort (
				responseItems);

			ObjectFormFieldSearchResponse response =
				new ObjectFormFieldSearchResponse ()

				.fieldId (
					request.fieldId ())

				.items (
					listSliceFromStart (
						responseItems,
						100l))

			;

			return optionalOf (
				response);

		}

	}

}
