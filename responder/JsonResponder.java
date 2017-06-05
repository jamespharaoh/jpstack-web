package wbs.web.responder;

import java.io.IOException;
import java.io.StringWriter;

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

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("jsonResponder")
public
class JsonResponder
	implements
		Provider <Responder>,
		Responder {

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

			requestContext.setHeader (
				"Content-Type",
				"application/json");

			try (

				FormatWriter formatWriter =
					requestContext.formatWriter ();

			) {

				DataToJson dataToJson =
					new DataToJson ();

				Object jsonValue =
					dataToJson.toJson (
						value);

				try (

					StringWriter stringWriter =
						new StringWriter ();

				) {

					JSONValue.writeJSONString (
						jsonValue,
						stringWriter);

					formatWriter.writeLineFormat (
						"%s",
						stringWriter.toString ());

				} catch (IOException ioException) {

					throw new RuntimeIoException (
						ioException);

				}

			}

		}

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
