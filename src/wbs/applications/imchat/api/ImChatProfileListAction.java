package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatProfileObjectHelper;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaRec;

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

	@Inject
	WbsConfig wbsConfig;

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

			MediaRec image =
				profile.getProfileImage ();

			String imageUrl;

			if (image == null) {

				imageUrl = "no-image";

			} else {

				ContentRec content =
					image.getContent ();

				Integer hash =
					Math.abs (
						content.getHash ());

				imageUrl =
					stringFormat (
						"%s",
						wbsConfig.apiUrl (),
						"/im-chat-media/%u",
						image.getId (),
						"/%u",
						hash,
						"/original.jpg");

			}

			profileDatas.add (
				new ImChatProfileData ()

				.code (
					profile.getCode ())

				.name (
					profile.getPublicName ())

				.description (
					profile.getPublicDescription ())

				.imageLink (
					imageUrl)

			);

		}

		return jsonResponderProvider.get ()
			.value (profileDatas);

	}

}
