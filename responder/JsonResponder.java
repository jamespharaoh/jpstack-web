package wbs.web.responder;

import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.web.utils.JsonUtils.jsonEncode;

import java.io.OutputStream;

import com.google.gson.JsonObject;

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

@Accessors (fluent = true)
@PrototypeComponent ("jsonResponder")
public
class JsonResponder
	extends BufferedResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	JsonObject value;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			doNothing ();

		}

	}

	@Override
	public
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull OutputStream outputStream) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			String stringValue =
				jsonEncode (
					value);

			byte[] bytesValue =
				stringToUtf8 (
					stringValue);

			writeBytes (
				outputStream,
				bytesValue);

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
					"headers");

		) {

			requestContext.contentType (
				"application/json",
				"utf-8");

		}

	}

}
