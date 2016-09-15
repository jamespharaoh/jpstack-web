package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.Cleanup;

import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.model.ImChatProfileState;

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
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ImChatProfileListAction.handle ()",
				this);

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
