package wbs.integrations.g8wave.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;

@PrototypeComponent ("g8waveInFile")
public
class G8waveInFile
	implements WebFile {

	// singleton dependencies

	@SingletonDependency
	Database database;

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
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

		doPost (
			taskLogger);

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"G8waveInFile.doPost ()",
				this);

		// get request stuff

		Long routeId =
			requestContext.requestIntegerRequired (
				"route_id");

		// get params in local variables

		String numFromParam =
			requestContext.parameterOrNull ("telno");

		String numToParam =
			requestContext.parameterOrNull ("shortcode");

		String networkParam =
			requestContext.parameterOrNull ("network");

		String messageParam =
			requestContext.parameterOrNull ("message");

		if (
			numFromParam == null
			|| numToParam == null
			|| messageParam == null
		) {
			throw new ServletException ("Parameter not supplied");
		}

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

			else if (networkParam.equals("THREE"))
				networkId = 6l;

			else
				throw new ServletException (
					"Unknown network: " + networkParam);
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
			optionalAbsent (),
			textHelper.findOrCreate (
				messageParam),
			smsNumberHelper.findOrCreate (
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

		PrintWriter out =
			requestContext.writer ();

		out.println ("OK");

	}

	@Override
	public
	void doOptions (
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

	}

}
