package wbs.applications.imchat.api;

import java.io.IOException;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.AbstractResponder;
import wbs.framework.web.RequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("imChatMediaResponder")
public
class ImChatMediaResponder
	extends AbstractResponder {

	// dependencies

	@Inject
	RequestContext requestContext;

	// properties

	@Getter @Setter
	byte[] data;

	@Getter @Setter
	String contentType;

	// implementation

	@Override
	protected
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			contentType);

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (
				data.length));

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		requestContext.outputStream ().write (
			data);

	}

}
