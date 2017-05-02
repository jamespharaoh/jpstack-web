package wbs.console.object;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.lookup.StringLookup;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.request.Cryptor;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.exceptions.HttpNotFoundException;

public abstract
class AbstractObjectContext
	extends ConsoleContext {

	// singleton dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// abstract getters

	public abstract
	Cryptor cryptor ();

	public abstract
	String requestIdKey ();

	public abstract
	String title ();

	public abstract
	StringLookup titleLookup ();

	public abstract
	ObjectLookup<?> objectLookup ();

	public abstract
	String postProcessorName ();

	public abstract
	Map<String,Object> stuff ();

	// implementation

	@Override
	public
	String localPathForStuff (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff stuff) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"localPathForStuff");

		) {

			return stringFormat (
				"/%s",
				encodeId (
					transaction,
					genericCastUnchecked (
						stuff.get (
							requestIdKey ()))));

		}

	}

	@Override
	public
	String titleForStuff (
			ConsoleContextStuff stuff) {

		if (title () != null) {

			return stuff.substitutePlaceholders (
				title ());

		}

		if (titleLookup () != null) {

			return titleLookup ().lookup (
				stuff);

		}

		throw new RuntimeException ();

	}

	protected
	Long decodeId (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String encodedId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"decodeId");

		) {

			if (cryptor () != null) {

				return cryptor ().decryptInteger (
					taskLogger,
					encodedId);

			} else {

				return Long.parseLong (
					encodedId);

			}

		}

	}

	protected
	String encodeId (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long numericId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"encodeId");

		) {

			if (cryptor () != null) {

				return cryptor ().encryptInteger (
					taskLogger,
					numericId);

			} else {

				return Long.toString (
					numericId);

			}

		}

	}

	@Override
	public
	void initContext (
			@NonNull Transaction parentTransaction,
			@NonNull PathSupply pathParts,
			@NonNull ConsoleContextStuff contextStuff) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"initContext");

		) {

			Long localId =
				decodeId (
					transaction,
					pathParts.next ());

			contextStuff.set (
				requestIdKey (),
				localId);

			Object object =
				objectLookup ().lookupObject (
					transaction,
					contextStuff);

			if (object == null) {

				transaction.warningFormat (
					"Can't find object with id %s",
					integerToDecimalString (
						localId));

				throw new HttpNotFoundException (
					optionalAbsent (),
					emptyList ());

			}

			if (stuff () != null) {

				for (
					Map.Entry <String, ? extends Object> entry
						: stuff ().entrySet ()
				) {

					contextStuff.set (
						entry.getKey (),
						entry.getValue ());

				}

			}

			if (postProcessorName () != null) {

				consoleManager.runPostProcessors (
					transaction,
					postProcessorName (),
					contextStuff);

			}

		}

	}

	public static
	interface ObjectPostProcessor {

		void process (
				ConsoleRequestContext requestContext,
				ConsoleContextStuff contextStuff,
				Record<?> object);

	}

}
