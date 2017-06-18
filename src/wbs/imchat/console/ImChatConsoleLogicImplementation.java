package wbs.imchat.console;

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

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatRec;

@SingletonComponent ("imChatConsoleLogic")
public
class ImChatConsoleLogicImplementation
	implements ImChatConsoleLogic {

	// singleton dependencies

	@SingletonDependency
	ImChatConsoleHelper imChatHelper;

	@ClassSingletonDependency
	LogContext logContext;

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
					"getSupervisorSearchUserIds");

		) {

			List <Predicate <ImChatRec>> predicates =
				new ArrayList<> ();

			// handle slice code condition

			Optional <Set <String>> sliceCodesOptional =
				mapItemForKey (
					conditions,
					"slice-code-optional");

			if (
				optionalIsPresent (
					sliceCodesOptional)
			) {

				Set <String> sliceCodes =
					optionalGetRequired (
						sliceCodesOptional);

				predicates.add (
					imChat ->
						contains (
							sliceCodes,
							imChat.getSlice ().getCode ()));

			}

			// handle im chat slice code condition

			Optional <Set <String>> imChatSliceCodesOptional =
				mapItemForKey (
					conditions,
					"im-chat-slice-code");

			if (
				optionalIsPresent (
					imChatSliceCodesOptional)
			) {

				Set <String> imChatSliceCodes =
					optionalGetRequired (
						imChatSliceCodesOptional);

				predicates.add (
					imChat ->
						contains (
							imChatSliceCodes,
							imChat.getSlice ().getCode ()));

			}

			// handle im chat code condition

			Optional <Set <String>> imChatCodesOptional =
				mapItemForKey (
					conditions,
					"im-chat-code");

			if (
				optionalIsPresent (
					imChatCodesOptional)
			) {

				Set <String> imChatCodes =
					optionalGetRequired (
						imChatCodesOptional);

				predicates.add (
					user ->
						contains (
							imChatCodes,
							joinWithFullStop (
								user.getSlice ().getCode (),
								user.getCode ())));

			}

			// return

			return iterableFilterMapToSet (
				imChatHelper.findAll (
					transaction),
				predicatesCombineAll (
					predicates),
				ImChatRec::getId);

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
				imChatHelper.findAll (
					transaction),
				imChat ->
					privChecker.canRecursive (
						transaction,
						imChat,
						"supervisor"),
				ImChatRec::getId);

		}

	}

}
