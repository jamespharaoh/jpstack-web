package wbs.imchat.api;

import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.Misc.doNothing;

import java.io.OutputStream;

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
import wbs.web.responder.BufferedResponder;

@Accessors (fluent = true)
@PrototypeComponent ("imChatMediaResponder")
public
class ImChatMediaResponder
	extends BufferedResponder {

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull OutputStream outputStream) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

		) {

			writeBytes (
				outputStream,
				data);

		}

	}

	@Override
	protected
	void headers (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeaders");

		) {

			requestContext.contentType (
				contentType);

		}

	}

}
