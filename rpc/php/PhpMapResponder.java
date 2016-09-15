package wbs.platform.rpc.php;

import static wbs.utils.string.StringUtils.stringFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.php.PhpEntity;
import wbs.platform.php.PhpFormatter;
import wbs.platform.php.PhpSerializer;
import wbs.platform.php.PhpUnserializer;

@Log4j
@Accessors (fluent = true)
@PrototypeComponent ("phpMapResponder")
public
class PhpMapResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	Map <?,?> map;

	@Getter @Setter
	int status =
		HttpServletResponse.SC_OK;

	// implementation

	@Override
	public
	void execute ()
		throws IOException {

		requestContext.status (
			status);

		requestContext.setHeader (
			"Content-Type",
			"application/vnd.php.serialized; charset=utf-8");

		OutputStream out =
			requestContext.outputStream ();

		PhpSerializer.serialize (
			out,
			map,
			"utf-8");

		if (log.isDebugEnabled ()) {

			ByteArrayOutputStream baos =
				new ByteArrayOutputStream ();

			PhpSerializer.serialize (
				baos,
				map,
				"utf-8");

			ByteArrayInputStream byteArrayInputStream =
				new ByteArrayInputStream (
					baos.toByteArray ());

			PhpEntity entity =
				PhpUnserializer.unserialize (
					byteArrayInputStream);

			log.debug (
				stringFormat (
					"PHP response:\n",
					"%s",
					PhpFormatter.DEFAULT.format (entity)));

		}

	}

}
