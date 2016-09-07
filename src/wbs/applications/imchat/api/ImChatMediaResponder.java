package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.CollectionUtils.arrayLength;
import static wbs.framework.utils.etc.NumberUtils.integerToDecimalString;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.AbstractResponder;
import wbs.framework.web.RequestContext;

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
	void goHeaders () {

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
	void goContent ()
		throws IOException {

		requestContext.outputStream ().write (
			data);

	}

}
