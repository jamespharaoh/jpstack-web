package wbs.platform.rpc.php;

import static wbs.utils.string.StringUtils.stringFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.php.PhpEntity;
import wbs.platform.php.PhpFormatter;
import wbs.platform.php.PhpSerializer;
import wbs.platform.php.PhpUnserializer;

import wbs.utils.io.BorrowedOutputStream;

import wbs.web.context.RequestContext;
import wbs.web.misc.HttpStatus;
import wbs.web.responder.WebResponder;

@Log4j
@Accessors (fluent = true)
@PrototypeComponent ("phpMapResponder")
public
class PhpMapResponder
	implements WebResponder {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	Map <?,?> map;

	@Getter @Setter
	long status =
		HttpStatus.httpOk;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.status (
			status);

		requestContext.contentType (
			"application/vnd.php.serialized",
			"utf-8");

		try (

			BorrowedOutputStream outputStream =
				requestContext.outputStream ();

		) {

			PhpSerializer.serialize (
				outputStream,
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

}
