package wbs.platform.rpc.xml;

import static wbs.framework.utils.etc.Misc.toHex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.rpc.core.RpcElem;
import wbs.platform.rpc.core.RpcList;
import wbs.platform.rpc.core.RpcPrimitive;
import wbs.platform.rpc.core.RpcStructure;
import wbs.platform.rpc.core.RpcType;

@Log4j
@Accessors (fluent = true)
@PrototypeComponent ("xmlResponder")
public
class XmlResponder
	implements Responder {

	@Inject
	RequestContext requestContext;

	@Getter @Setter
	RpcElem data;

	@Getter @Setter
	int status =
		HttpServletResponse.SC_OK;

	@Override
	public
	void execute ()
		throws IOException {

		requestContext.status (
			status);

		requestContext.setHeader (
			"Content-Type",
			"application/xml");

		OutputStream out =
			requestContext.outputStream ();

		Element node =
			toNode (data);

		Document doc =
			new Document (node);

		Serializer serializer =
			new Serializer (out, "utf-8");

		serializer.setIndent (4);
		serializer.write (doc);

		if (log.isDebugEnabled ()) {

			serializer =
				new Serializer (System.out, "utf-8");

			serializer.setIndent (4);
			serializer.write (doc);

		}

	}

	public
	static Element toNode (
			RpcStructure data) {

		Element elem =
			new Element (data.getName ());

		for (Map.Entry<String,RpcElem> ent
				: data.getValue ().entrySet ()) {

			elem.appendChild (
				toNode (ent.getValue ()));

		}

		return elem;

	}

	public
	static Element toNode (
			RpcList data) {

		Element elem =
			new Element (data.getName ());

		for (RpcElem ent
				: data.getValue ()) {

			elem.appendChild (
				toNode (ent));

		}

		return elem;

	}

	public
	static Element toNode (
			RpcPrimitive data) {

		Element elem =
			new Element (data.getName ());

		if (data.getType () == RpcType.rBinary) {

			byte[] bytes =
				(byte[]) data.getValue ();

			elem.appendChild (
				toHex (bytes));

		} else {

			elem.appendChild (
				data.getValue ().toString ());

		}

		return elem;

	}

	public
	static Element toNode (
			RpcElem data) {

		switch (data.getType ()) {

		case rStructure:

			return toNode ((RpcStructure) data);

		case rList:

			return toNode ((RpcList) data);

		case rString:
		case rBoolean:
		case rInteger:
		case rDate:
		case rFloat:
		case rBinary:

			return toNode (
				(RpcPrimitive) data);

		default:

			throw new RuntimeException (
				"Unable to process " + data.getType ());

		}

	}

}
