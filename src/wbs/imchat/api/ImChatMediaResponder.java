package wbs.imchat.api;

import static wbs.utils.collection.CollectionUtils.arrayLength;
import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.web.context.RequestContext;
import wbs.web.responder.AbstractResponder;

@Accessors (fluent = true)
@PrototypeComponent ("imChatMediaResponder")
public
class ImChatMediaResponder
	extends AbstractResponder {

	// dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	byte[] data;

	@Getter @Setter
	String contentType;

	// implementation

	@Override
	protected
	void goHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeaders");

		) {

			requestContext.contentType (
				contentType);

			requestContext.setHeader (
				"Content-Length",
				integerToDecimalString (
					arrayLength (
						data)));

		}

	}

	@Override
	protected
	void goContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

		) {

			writeBytes (
				requestContext.outputStream (),
				data);

		}

	}

}
