package wbs.platform.rpc.php;

import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.api.mvc.WebApiAction;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.php.PhpEntity;
import wbs.platform.php.PhpFormatter;
import wbs.platform.php.PhpUnserializeException;
import wbs.platform.php.PhpUnserializer;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcException;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.web.ReusableRpcHandler;

@Log4j
@Accessors (fluent = true)
@PrototypeComponent ("phpRpcAction")
public
class PhpRpcAction
	implements WebApiAction {

	@Inject
	ComponentManager applicationContext;

	@Inject
	RequestContext requestContext;

	@Inject
	Provider<PhpMapResponder> phpMapResponder;

	@Getter @Setter
	ReusableRpcHandler rpcHandler;

	public
	PhpRpcAction rpcHandlerName (
			final String name) {

		ReusableRpcHandler rpcHandler =
			new ReusableRpcHandler () {

			@Override
			public
			RpcResult handle (
					RpcSource source) {

				RpcHandler delegate =
					applicationContext.getComponentRequired (
						name,
						RpcHandler.class);

				return delegate.handle (
					source);

			}

		};

		return rpcHandler (
			rpcHandler);

	}

	@Override
	public
	Provider<Responder> makeFallbackResponder () {

		return phpInternalErrorResponder ();

	}

	@Override
	public
	Responder go ()
		throws IOException {

		RpcResult ret =
			realGo ();

		return ret != null
			? phpMapResponder.get ()
				.map (ret.getStruct ().getNative ())
				.status (ret.getHttpStatus ())
			: null;

	}

	public
	RpcResult realGo ()
		throws IOException {

		// process input

		InputStream in =
			requestContext.inputStream ();

		PhpEntity input;
		try {

			input =
				PhpUnserializer.unserialize (in);

		} catch (PhpUnserializeException e) {

			e.printStackTrace (System.out);

			return Rpc.rpcError (
				"FIXME",
				Rpc.stRequestInvalid,
				"request-invalid",
				"PHP serialized data not recognised");

		}

		if (in.read () >= 0) {

			return Rpc.rpcError (
				"FIXME",
				Rpc.stRequestInvalid,
				"request-invalid",
				"PHP serialized data too long");

		}

		if (! input.isArray ()) {

			return Rpc.rpcError (
				"FIXME",
				Rpc.stRequestInvalid,
				"request-invalid",
				"PHP serialized data should be an array");

		}

		log.debug (
			stringFormat (
				"PHP RPC request to %s\n%s",
				requestContext.requestUri (),
				PhpFormatter.DEFAULT.format (input)));

		// get and validate encoding

		PhpEntity entity =
			input.getAt ("encoding");

		String encoding =
			entity.isNull ()
				? "iso-8859-1"
				: entity.asString ("iso-8859-1");

		if (! Charset.isSupported (encoding)) {

			return Rpc.rpcError (
				"FIXME",
				Rpc.stEncodingUnsupported,
				"encoding-unsupported",
				"Unsupported character encoding: " + encoding);

		}

		// hand off to the handler

		try {
			return rpcHandler.handle(new PhpRpcSource(encoding, input));
		} catch (RpcException e) {
			return e.getRpcResult();
		}

	}

	private
	Provider<Responder> makeRpcResponder (
			final RpcResult result) {

		return new Provider<Responder> () {

			@Override
			public
			Responder get () {

				return phpMapResponder.get ()
					.map (result.getStruct ().getNative ())
					.status (result.getHttpStatus ());

			}

		};

	}

	public
	Provider<Responder> phpInternalErrorResponder () {

		return makeRpcResponder (
			Rpc.rpcError (
				"FIXME",
				Rpc.stInternalError,
				"internal-error",
				"An internal error has occurred"));

	}

	private
	class PhpRpcSource
		implements RpcSource {

		private
		String encoding;

		private
		PhpEntity entity;

		private
		PhpRpcSource (
				String newEncoding,
				PhpEntity newEntity) {

			encoding =
				newEncoding;

			entity =
				newEntity;

		}

		@Override
		public
		Object obtain (
				RpcDefinition def,
				List <String> errors,
				boolean checkRequires) {

			return phpToObject (
				def,
				entity,
				errors,
				encoding,
				checkRequires);

		}

	}

	private
	Object phpToObject (
			RpcDefinition rpcDef,
			PhpEntity entity,
			List<String> errors,
			String encoding,
			boolean checkRequires) {

		Object object =
			phpToObjectInternal (
				rpcDef,
				entity,
				errors,
				encoding,
				checkRequires);

		if (rpcDef.checker () != null) {

			object =
				rpcDef.checker ().check (
					rpcDef,
					object,
					errors);

		}

		return object;

	}

	private
	Object phpToObjectInternal (
			RpcDefinition rpcDefition,
			PhpEntity entity,
			List<String> errors,
			String encoding,
			boolean checkRequires) {

		switch (rpcDefition.type ()) {

		case rBoolean:

			return phpToBoolean (
				rpcDefition,
				entity,
				errors,
				encoding);

		case rInteger:

			return phpToInteger (
				rpcDefition,
				entity,
				errors,
				encoding);

		case rString:

			return phpToString (
				rpcDefition,
				entity,
				errors,
				encoding);

		case rFloat:

			return phpToFloat (
				rpcDefition,
				entity,
				errors,
				encoding);

		case rDate:

			return phpToDate (
				rpcDefition,
				entity,
				errors,
				encoding);

		case rStructure:

			return phpToStructure (
				rpcDefition,
				entity,
				errors,
				encoding,
				checkRequires);

		case rList:

			return phpToList (
				rpcDefition,
				entity,
				errors,
				encoding,
				checkRequires);

		case rSource:

			return phpToSource (
				rpcDefition,
				entity,
				errors,
				encoding);

		case rBinary:

			return phpToByteArray (
				rpcDefition,
				entity,
				errors,
				encoding);

		}

		throw new RuntimeException (
			stringFormat (
				"Unrecognised RPC type %s",
				rpcDefition.type ()));

	}

	private
	Object phpToBoolean (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isBoolean ()) {

			errors.add (
				stringFormat (
					"Parameter should be boolean: %s",
					rpcDefinition.name ()));

			return null;

		}

		return entity.asBoolean ();

	}

	private
	Object phpToInteger (
			RpcDefinition def,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isInteger ()) {

			errors.add (
				stringFormat (
					"Parameter should be integer: %s",
					def.name ()));

			return null;

		}

		return entity.asInteger ();

	}

	private
	Object phpToString (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isString ()) {

			errors.add (
				stringFormat (
					"Parameter should be string: %s",
					rpcDefinition.name ()));

			return null;

		}

		try {

			return entity.asString (
				encoding);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	private
	byte[] phpToByteArray (
			RpcDefinition def,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isString ()) {

			errors.add (
				stringFormat (
					"Parameter should be string: %s",
					def.name ()));

			return null;

		}

		return entity.asByteArray ();

	}

	private
	Object phpToFloat (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isFloat ()) {

			errors.add (
				stringFormat (
					"Parameter should be float: %s",
					rpcDefinition.name ()));

			return null;

		}

		return entity.asDouble ();

	}

	private
	Object phpToDate (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isString ()) {

			errors.add (
				stringFormat (
					"Parameter should be date string: %s",
					rpcDefinition.name ()));

			return null;

		}

		return LocalDate.parse (
			entity.asString ());

	}

	private
	Object phpToStructure (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding,
			boolean checkRequires) {

		// check the entity is an array

		if (! entity.isArray ()) {

			errors.add (
				stringFormat (
					"Parameter should be array: %s",
					rpcDefinition.name ()));

			return null;

		}

		Map<String,Object> outMap =
			new HashMap<String,Object> ();

		Map<String,RpcDefinition> defMap =
			rpcDefinition.membersByName ();

		Map<Object,PhpEntity> inMap =
			entity.asMap ();

		// iterate provided members

		for (Map.Entry<Object,PhpEntity> entry
				: inMap.entrySet ()) {

			String key =
				entry.getKey ().toString ();

			PhpEntity inValue = entry.getValue();

			// ignore encoding, this is handled separately

			if (
				stringEqualSafe (
					key,
					"encoding")
			) {
				continue;
			}

			// check it is defined

			if (! defMap.containsKey (key)) {

				if (checkRequires) {

					errors.add (
						stringFormat (
							"Parameter not recognised: %s",
							key));

				}

				continue;

			}

			// store it

			Object outValue =
				phpToObject (
					defMap.get (key),
					inValue,
					errors,
					encoding,
					checkRequires);

			outMap.put (
				key,
				outValue);

		}

		// check all required parameters are present

		for (
			RpcDefinition member
				: defMap.values ()
		) {

			if (inMap.containsKey (
					member.name ()))
				continue;

			if (member.required ()) {

				errors.add (
					stringFormat (
						"Missing required parameter: %s",
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
	Object phpToList (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding,
			boolean checkRequires) {

		// check the entity is a normal array

		if (! entity.isNormalArray ()) {

			errors.add (
				stringFormat (
					"Parameter should be a zero-indexed array: %s",
					rpcDefinition.name ()));

			return null;

		}

		// iterate members

		List<Object> ret =
			new ArrayList<Object> ();

		RpcDefinition memberDefinition =
			rpcDefinition.members () [0];

		for (
			PhpEntity in
				: entity.asList ()
		) {

			Object out =
				phpToObject (
					memberDefinition,
					in,
					errors,
					encoding,
					checkRequires);

			ret.add (
				out);

		}

		// return

		return ret;

	}

	private
	Object phpToSource (
			RpcDefinition rpcDefinition,
			PhpEntity entity,
			List<String> errors,
			String encoding) {

		if (! entity.isArray ()) {

			errors.add (
				stringFormat (
					"Parameter should be an array: %s",
					rpcDefinition.name ()));

			return null;

		}

		return new PhpRpcSource (
			encoding,
			entity);

	}

}
