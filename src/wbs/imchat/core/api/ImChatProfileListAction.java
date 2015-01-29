package wbs.imchat.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatProfileObjectHelper;
import wbs.imchat.core.model.ImChatProfileRec;
import wbs.imchat.core.model.ImChatRec;

@PrototypeComponent ("imChatProfileListAction")
public
class ImChatProfileListAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatProfileObjectHelper imChatProfileHelper;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		// retrieve profiles

		List<ImChatProfileRec> profiles =
			imChatProfileHelper.findByParent (
				imChat);

		Collections.sort (
			profiles);

		// create response

		List<ImChatProfileData> profileDatas =
			new ArrayList<ImChatProfileData> ();

		for (
			ImChatProfileRec profile
				: profiles
		) {

			if (profile.getDeleted ())
				continue;

			profileDatas.add (
				new ImChatProfileData ()

				.id (
					profile.getId ())

				.name (
					profile.getPublicName ())

				.description (
					profile.getPublicDescription ())

				.imageLink (
					"TODO")

			);

		}

		return jsonResponderProvider.get ()
			.value (profileDatas);

	}

}
