package wbs.imchat.api;

import static wbs.utils.collection.CollectionUtils.arrayLength;
import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.responder.AbstractResponder;

@Accessors (fluent = true)
@PrototypeComponent ("imChatMediaResponder")
public
class ImChatMediaResponder
	extends AbstractResponder {

	// dependencies

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
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.setHeader (
			"Content-Type",
			contentType);

		requestContext.setHeader (
			"Content-Length",
			integerToDecimalString (
				arrayLength (
					data)));

	}

	@Override
	protected
	void goContent (
			@NonNull TaskLogger parentTaskLogger) {

		writeBytes (
			requestContext.outputStream (),
			data);

	}

}
