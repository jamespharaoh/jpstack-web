package wbs.apn.chat.core.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.logic.AbstractNumberLookupHelper;
import wbs.sms.number.lookup.model.NumberLookupRec;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatNumberLookupHelper")
public
class ChatNumberLookupHelper
	extends AbstractNumberLookupHelper {

	// singleton dependencies

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	public
	String parentObjectTypeCode () {
		return "chat";
	}

	// implementation

	@Override
	public
	boolean lookupNumber (
			@NonNull Transaction parentTransaction,
			@NonNull NumberLookupRec numberLookup,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupNumber");

		) {

			ChatRec chat =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						numberLookup));

			if (
				stringEqualSafe (
					numberLookup.getCode (),
					"block_all")
			) {

				Optional <ChatUserRec> chatUserOptional =
					chatUserHelper.find (
						transaction,
						chat,
						number);

				if (
					optionalIsNotPresent (
						chatUserOptional)
				) {
					return false;
				}

				return chatUserOptional.get ().getBlockAll ();

			} else {

				throw new RuntimeException ();

			}

		}

	}

}
