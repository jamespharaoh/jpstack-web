package wbs.integrations.g8wave.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.io.IOException;

import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;

@PrototypeComponent ("g8waveInFile")
public
class G8waveInFile
	implements WebFile {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void doGet (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doGet");

		doPost (
			taskLogger);

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doPost");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"G8waveInFile.doPost ()",
					this);

		) {

			// get request stuff

			Long routeId =
				requestContext.requestIntegerRequired (
					"route_id");

			// get params in local variables

			String numFromParam =
				requestContext.parameterRequired (
					"telno");

			String numToParam =
				requestContext.parameterRequired (
					"shortcode");

			String networkParam =
				optionalOrNull (
					requestContext.parameter (
						"network"));

			String messageParam =
				requestContext.parameterRequired (
					"message");

			Long networkId = null;

			if (networkParam != null) {

				if (networkParam.equals("ORANGE"))
					networkId = 1l;

				else if (networkParam.equals("VODA"))
					networkId = 2l;

				else if (networkParam.equals("TMOB"))
					networkId = 3l;

				else if (networkParam.equals("O2"))
					networkId = 4l;

				else if (networkParam.equals("THREE")) {

					networkId = 6l;

				} else {

					throw new RuntimeException (
						"Unknown network: " + networkParam);

				}

			}

			// load the stuff

			RouteRec route =
				routeHelper.findRequired (
					routeId);

			NetworkRec network =
				networkId == null
					? null
					: networkHelper.findRequired (
						networkId);

			// insert the message

			smsInboxLogic.inboxInsert (
				taskLogger,
				optionalAbsent (),
				textHelper.findOrCreate (
					taskLogger,
					messageParam),
				smsNumberHelper.findOrCreate (
					taskLogger,
					numFromParam),
				numToParam,
				route,
				optionalFromNullable (
					network),
				optionalAbsent (),
				emptyList (),
				optionalAbsent (),
				optionalAbsent ());

			transaction.commit ();

			FormatWriter formatWriter =
				requestContext.formatWriter ();

			formatWriter.writeLineFormat (
				"OK");

		}

	}

	@Override
	public
	void doOptions (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

	}

}
