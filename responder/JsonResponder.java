package wbs.web.responder;

import static wbs.utils.collection.CollectionUtils.arrayLength;
import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.string.StringUtils.stringToUtf8;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataToJson;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("jsonResponder")
public
class JsonResponder
	implements
		Provider <WebResponder>,
		WebResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	Object value;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"execute");

		) {

			DataToJson dataToJson =
				new DataToJson ();

			Object jsonValue =
				dataToJson.toJson (
					value);

			String stringValue =
				JSONValue.toJSONString (
					jsonValue);

			byte[] bytesValue =
				stringToUtf8 (
					stringValue);

			requestContext.contentType (
				"application/json",
				"utf-8");

			requestContext.contentLength (
				arrayLength (
					bytesValue));

			writeBytes (
				requestContext.outputStream (),
				bytesValue);

		}

	}

	@Override
	public
	WebResponder get () {

		return this;

	}

}
