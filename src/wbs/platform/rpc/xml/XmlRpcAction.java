package wbs.platform.rpc.xml;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.fromHex;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.utils.cal.CalDate;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.api.WebApiAction;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcException;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.web.ReusableRpcHandler;

@Log4j
@Accessors (fluent = true)
@PrototypeComponent ("xmlRpcAction")
public
class XmlRpcAction
	implements WebApiAction {

	@Inject
	ApplicationContext applicationContext;

	@Inject
	RequestContext requestContext;

	@Inject
	Provider<XmlResponder> xmlResponder;

	@Getter @Setter
	ReusableRpcHandler rpcHandler;

	public
	XmlRpcAction rpcHandlerName (
			final String name) {

		ReusableRpcHandler rpcHandler =
			new ReusableRpcHandler () {

			@Override
			public
			RpcResult handle (
					RpcSource source) {

				RpcHandler delegate =
					applicationContext.getBean (
						name,
						RpcHandler.class);

				return delegate.handle (
					source);

			}

		};

		return rpcHandler (
			rpcHandler);

	}

	public
	Responder xmlInternalErrorResponder () {

		return makeRpcResponder (
			Rpc.rpcError (
				"FIXME",
				Rpc.stInternalError,
				"internal-error",
				"An internal error has occurred"));

	}

	@Override
	public
	Provider<Responder> makeFallbackResponder () {

		return new Provider<Responder> () {

			@Override
			public
			Responder get () {

				return xmlInternalErrorResponder ();

			}

		};

	}

	private
	Responder makeRpcResponder (
			RpcResult result) {

		return xmlResponder.get ()

			.data (
				result.getStruct ())

			.status (
				result.getHttpStatus ());

	}

	@Override
	public
	Responder go ()
		throws IOException {

		RpcResult ret =
			realGo ();

		return ret != null
			? makeRpcResponder (ret)
			: null;

	}

	public
	XmlRpcSource parse (
			InputStream in)
		throws IOException {

		try {

			Builder parser =
				new Builder ();

			Document document =
				parser.build (in);

			XmlRpcSource source =
				new XmlRpcSource (
					document.getRootElement ());

			if (log.isDebugEnabled ()) {

				Serializer serializer =
					new Serializer (System.out, "utf-8");

				serializer.setIndent (4);

				serializer.write (document);

			}

			return source;

		} catch (ValidityException exception) {

			throw new RpcException (
				"error-response",
				Rpc.stRequestInvalid,
				"request-invalid",
				exception.getMessage ());

		} catch (ParsingException exception) {

			throw new RpcException (
				"error-response",
				Rpc.stRequestInvalid,
				"request-invalid",
				exception.getMessage ());

		}

	}

	public RpcResult realGo ()
		throws IOException {

		try {

			XmlRpcSource source =
				parse (requestContext.inputStream ());

			return rpcHandler.handle (source);

		} catch (RpcException exception) {

			return exception.getRpcResult ();

		} catch (Exception exception) {

			throw new RuntimeException (exception);

		}

	}

	private
	class XmlRpcSource
		implements RpcSource {

		private
		Element element;

		private
		XmlRpcSource (
				Element newElem) {

			element =
				newElem;

		}

		@Override
		public
		Object obtain (
				RpcDefinition def,
				List<String> errors,
				boolean checkRequires) {

			return xmlToObject (
				def,
				element,
				errors,
				checkRequires);

		}

	}

	private
	Object xmlToObject (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors,
			boolean checkRequires) {

		Object object =
			xmlToObjectInternal (
				rpcDefinition,
				element,
				errors,
				checkRequires);

		if (rpcDefinition.checker () != null) {

			object =
				rpcDefinition.checker ().check (
					rpcDefinition,
					object,
					errors);

		}

		return object;

	}

	private
	Object xmlToObjectInternal (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors,
			boolean checkRequires) {

		// check the element type is correct

		if (! equal (
				rpcDefinition.name (),
				element.getLocalName ())) {

			errors.add (
				stringFormat (
					"Unexpected element: %s, expected: %s",
					element.getLocalName (),
					rpcDefinition.name ()));

			return null;

		}

		// delegate depending on type

		switch (rpcDefinition.type ()) {

		case rBoolean:

			return xmlToBoolean (
				rpcDefinition,
				element,
				errors);

		case rInteger:

			return xmlToInteger (
				rpcDefinition,
				element,
				errors);

		case rString:

			return xmlToString (
				rpcDefinition,
				element,
				errors);

		case rFloat:

			return xmlToFloat (
				rpcDefinition,
				element,
				errors);

		case rStructure:

			return xmlToStructure (
				rpcDefinition,
				element,
				errors,
				checkRequires);

		case rList:

			return xmlToList (
				rpcDefinition,
				element,
				errors,
				checkRequires);

		case rSource:

			return xmlToSource (
				rpcDefinition,
				element,
				errors);

		case rBinary:

			return xmlToBinary (
				rpcDefinition,
				element,
				errors);

		case rDate:

			return xmlToDate (
				rpcDefinition,
				element,
				errors);

		}

		throw new RuntimeException (
			stringFormat (
				"Can't decode %s",
				rpcDefinition.type ()));

	}

	private
	Object xmlToBoolean (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors) {

		if (element.getChildElements ().size () != 0) {

			errors.add (
				stringFormat (
					"Node should not contain elements: %s",
					rpcDefinition.name ()));

			return null;

		}

		String text =
			element.getValue ();

		if (equal (text, "true"))
			return true;

		if (equal (text, "false"))
			return false;

		errors.add (
			stringFormat (
				"Node should contain true or false: %s",
				rpcDefinition.name ()));

		return null;

	}

	private final static
	Pattern integerPattern =
		Pattern.compile (
			"0|-?[123456789][0123456789]*");

	private
	Object xmlToInteger (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors) {

		if (element.getChildElements ().size () > 0) {

			errors.add (
				stringFormat (
					"Node should not contain children: %s",
					rpcDefinition.name ()));

			return null;

		}

		String text =
			element.getValue ();

		if (! integerPattern.matcher (text).matches ()) {

			errors.add (
				stringFormat (
					"Node should be an integer value: %s",
					rpcDefinition.name ()));

			return null;

		}

		return Integer.parseInt (
			text);

	}

	private
	Object xmlToString (
			RpcDefinition def,
			Element elem,
			List<String> errors) {

		if (elem.getChildElements ().size () > 0) {

			errors.add (
				stringFormat (
					"Node should not contain children: %s",
					def.name ()));

			return null;

		}

		return elem.getValue ();

	}

	private
	Object xmlToDate (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors) {

		if (element.getChildElements ().size () > 0) {

			errors.add (
				stringFormat (
					"Node should not contain children: %s",
					rpcDefinition.name ()));

			return null;

		}

		String string =
			element.getValue ();

		try {

			return CalDate.parseYmd (
				string);

		} catch (Exception exception) {

			errors.add (
				stringFormat (
					"Node contains invalid date: %s",
					rpcDefinition.name ()));

			return null;

		}

	}

	private
	Object xmlToFloat (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors) {

		if (element.getChildElements ().size () > 0) {

			errors.add (
				stringFormat (
					"Node should not contain children: %s",
					rpcDefinition.name ()));

			return null;

		}

		String text =
			element.getValue ();

		return Double.parseDouble (
			text);

	}

	private
	Object xmlToStructure (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors,
			boolean checkRequires) {

		Map<String,Object> outMap =
			new HashMap<String,Object> ();

		Map<String,RpcDefinition> definitionMap =
			rpcDefinition.membersByName ();

		Elements children =
			element.getChildElements ();

		// iterate provided members

		for (
			int index = 0;
			index < children.size ();
			index ++
		) {

			Element child =
				children.get (
					index);

			// check it is defined

			if (! definitionMap.containsKey (
					child.getLocalName ())) {

				if (checkRequires) {

					errors.add (
						stringFormat (
							"Structure member not recognised: %s",
							child.getLocalName ()));

				}

				continue;

			}

			// check it isn't a dupe

			if (outMap.containsKey (
					child.getLocalName ())) {

				errors.add (
					stringFormat (
						"Duplicated structure member: %s",
						child.getLocalName ()));

				continue;

			}

			// store it

			Object outValue =
				xmlToObject (
					definitionMap.get (
						child.getLocalName ()),
					child,
					errors,
					checkRequires);

			outMap.put (
				child.getLocalName (),
				outValue);

		}

		// check all required parameters are present

		for (RpcDefinition member
				: definitionMap.values ()) {

			if (outMap.containsKey (
					member.name ()))
				continue;

			if (member.required ()) {

				errors.add (
					stringFormat (
						"Structure missing required member: %s",
						member.name ()));

			} else {

				outMap.put (
					member.name (),
					member.defaultValue ());

			}

		}

		// return

		return outMap;

	}

	private
	Object xmlToList (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors,
			boolean checkRequires) {

		// iterate members

		List<Object> ret =
			new ArrayList<Object> ();

		RpcDefinition memberDefinition =
			rpcDefinition.members () [0];

		Elements children =
			element.getChildElements ();

		for (
			int index = 0;
			index < children.size ();
			index ++
		) {

			Element child =
				children.get (
					index);

			Object out =
				xmlToObject (
					memberDefinition,
					child,
					errors,
					checkRequires);

			ret.add (
				out);

		}

		// return

		return ret;

	}

	private
	Object xmlToSource (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors) {

		return new XmlRpcSource (
			element);

	}

	private final static
	Pattern hexPattern =
		Pattern.compile (
			"[0123456789abcdefABCDEF]*");

	private
	Object xmlToBinary (
			RpcDefinition rpcDefinition,
			Element element,
			List<String> errors) {

		if (element.getChildElements ().size () > 0) {

			errors.add (
				stringFormat (
					"Elementshould not contain children: %s",
					rpcDefinition.name ()));

			return null;

		}

		String text =
			element.getValue ().trim ();

		if (! hexPattern.matcher (text).matches ()) {

			errors.add (
				stringFormat (
					"Element should contain hex: %s",
					rpcDefinition.name ()));

			return null;

		}

		return fromHex (
			text);

	}

}
