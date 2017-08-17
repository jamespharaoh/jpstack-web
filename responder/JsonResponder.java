package wbs.web.responder;

import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.web.utils.JsonUtils.jsonEncode;
import static wbs.web.utils.JsonUtils.objectToJson;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.web.context.RequestContext;
import wbs.web.utils.DataObject;

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

	JsonElement value;

	// property getters

	public
	JsonElement value () {
		return value;
	}

	public
	JsonResponder value (
			@NonNull JsonObject value) {

		this.value =
			value;

		return this;

	}

	public
	JsonResponder value (
			@NonNull DataObject value) {

		this.value =
			objectToJson (
				value);

		return this;

	}

	public
	JsonResponder value (
			@NonNull Map <String, ?> value) {

		this.value =
			objectToJson (
				value);

		return this;

	}

	@Deprecated
	public
	JsonResponder value (
			@NonNull JsonElement value) {

		this.value =
			value;

		return this;

	}

	@Deprecated
	public
	JsonResponder value (
			@NonNull List <?> value) {

		this.value =
			(JsonArray)
			objectToJson (
				value);

		return this;

	}

	// property setters

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
