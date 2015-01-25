package wbs.imchat.core.api;

import java.io.IOException;

import javax.inject.Inject;

import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("imChatTestAction")
public
class ImChatTestAction
	implements Action {

	// dependencies

	@Inject
	RequestContext requestContext;

	// implementation

	@Override
	@SneakyThrows (IOException.class)
	public
	Responder handle () {

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		DataFromJson dataFromJson =
			new DataFromJson ();

		ImChatTestRequest request =
			dataFromJson.fromJson (
				ImChatTestRequest.class,
				jsonValue);

		System.out.println (
			"hello: " + request.hello ());

		System.out.println (
			"world: " + request.world ());

		return null;

	}

}
