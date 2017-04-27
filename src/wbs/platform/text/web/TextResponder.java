package wbs.platform.text.web;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

// TODO this belongs elsewhere

@Accessors (fluent = true)
@PrototypeComponent ("textResponder")
public
class TextResponder
	implements
		Provider <Responder>,
		Responder {

	@SingletonDependency
	RequestContext requestContext;

	@Getter @Setter
	String text;

	@Getter @Setter
	String contentType =
		"text/plain";

	@Getter @Setter
	String filename;

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.setHeader (
			"Content-Type",
			stringFormat (
				"%s; charset=utf-8",
				contentType));

		if (
			isNotNull (
				filename)
		) {

			requestContext.setHeader (
				"Content-Disposition",
				stringFormat (
					"attachment; filename=%s",
					filename));

		}

		try (

			FormatWriter formatWriter =
				requestContext.formatWriter ();

		) {

			formatWriter.writeString (
				text);

		}

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
