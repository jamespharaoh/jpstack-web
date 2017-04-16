package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatProfileState;
import wbs.imchat.model.ImChatRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatProfileListAction")
public
class ImChatProfileListAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatProfileObjectHelper imChatProfileHelper;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ImChatProfileListAction.handle ()",
					this);

		) {

			ImChatRec imChat =
				imChatHelper.findRequired (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// retrieve profiles

			List <ImChatProfileRec> profiles =
				imChatProfileHelper.findByParent (
					imChat);

			Collections.sort (
				profiles);

			// create response

			List <ImChatProfileData> profileDatas =
				new ArrayList<> ();

			for (
				ImChatProfileRec profile
					: profiles
			) {

				if (profile.getDeleted ())
					continue;

				if (profile.getState () == ImChatProfileState.disabled)
					continue;

				profileDatas.add (
					imChatApiLogic.profileData (
						profile));

			}

			return jsonResponderProvider.get ()
				.value (profileDatas);

		}

	}

}
