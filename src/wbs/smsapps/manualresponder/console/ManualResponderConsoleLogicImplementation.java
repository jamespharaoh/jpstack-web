package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.IterableUtils.iterableFilterMapToSet;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.LogicUtils.predicatesCombineAll;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithFullStop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.smsapps.manualresponder.model.ManualResponderRec;

@SingletonComponent ("manualResponderConsoleLogic")
public
class ManualResponderConsoleLogicImplementation
	implements ManualResponderConsoleLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderConsoleHelper manualResponderHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// public implementation

	@Override
	public
	Set <Long> getSupervisorSearchIds (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Set <String>> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSupervisorSearchManualRessponderIds");

		) {

			List <Predicate <ManualResponderRec>> predicates =
				new ArrayList<> ();

			// handle slice code condition

			Optional <Set <String>> sliceCodes =
				mapItemForKey (
					conditions,
					"slice-code");

			if (
				optionalIsPresent (
					sliceCodes)
			) {

				predicates.add (
					manualResponder ->
						contains (
							optionalGetRequired (
								sliceCodes),
							manualResponder.getSlice ().getCode ()));

			}

			// handle manual responder slice code condition

			Optional <Set <String>> manualResponderSliceCodes =
				mapItemForKey (
					conditions,
					"manual-responder-slice-code");

			if (
				optionalIsPresent (
					manualResponderSliceCodes)
			) {

				predicates.add (
					manualResponder ->
						contains (
							optionalGetRequired (
								manualResponderSliceCodes),
							manualResponder.getSlice ().getCode ()));

			}

			// handle chat code condition

			Optional <Set <String>> manualResponderCodes =
				mapItemForKey (
					conditions,
					"manual-responder-code");

			if (
				optionalIsPresent (
					manualResponderCodes)
			) {

				predicates.add (
					manualResponder ->
						contains (
							optionalGetRequired (
								manualResponderCodes),
							joinWithFullStop (
								manualResponder.getSlice ().getCode (),
								manualResponder.getCode ())));

			}

			// return

			return iterableFilterMapToSet (
				manualResponderHelper.findAll (
					transaction),
				predicatesCombineAll (
					predicates),
				ManualResponderRec::getId);

		}

	}

	@Override
	public
	Set <Long> getSupervisorFilterIds (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSupervisorFilterUserIds");

		) {

			return iterableFilterMapToSet (
				manualResponderHelper.findAll (
					transaction),
				user ->
					privChecker.canRecursive (
						transaction,
						user,
						"supervisor"),
				ManualResponderRec::getId);

		}

	}

}
