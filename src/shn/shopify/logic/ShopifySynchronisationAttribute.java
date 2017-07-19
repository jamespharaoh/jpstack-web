package shn.shopify.logic;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.database.Transaction;

import shn.shopify.model.ShnShopifyConnectionRec;

@Accessors (fluent = true)
@Data
public
class ShopifySynchronisationAttribute <
	Local,
	RemoteRequest,
	RemoteResponse
> {

	Class <?> valueClass;
	Class <Local> localClass;
	Class <RemoteRequest> remoteRequestClass;
	Class <RemoteResponse> remoteResponseClass;

	String friendlyName;

	Boolean send;
	Boolean receive;
	Boolean compare;

	Boolean localId;
	Boolean remoteId;

	Function <
		Context <Local, RemoteRequest, RemoteResponse>,
		Optional <Object>
	> localGetOperation;

	BiConsumer <
		Context <Local, RemoteRequest, RemoteResponse>,
		Optional <Object>
	> localSetOperation;

	Function <
		Context <Local, RemoteRequest, RemoteResponse>,
		Optional <Object>
	> remoteGetOperation;

	BiConsumer <
		Context <Local, RemoteRequest, RemoteResponse>,
		Optional <Object>
	> remoteSetOperation;

	BiPredicate <Optional <Object>, Optional <Object>> compareOperation;

	@Accessors (fluent = true)
	@Data
	static
	class Context <Local, RemoteRequest, RemoteResponse> {
		Transaction transaction;
		ShnShopifyConnectionRec connection;
		Local local;
		RemoteRequest remoteRequest;
		RemoteResponse remoteResponse;
	}

	@Accessors (fluent = true)
	@Data
	static
	class Factory <Local, RemoteRequest, RemoteResponse> {

		Class <Local> localClass;
		Class <RemoteRequest> remoteRequestClass;
		Class <RemoteResponse> remoteResponseClass;

		public
		ShopifySynchronisationAttribute <
			Local,
			RemoteRequest,
			RemoteResponse
		> send (
				@NonNull Class <?> valueClass,
				@NonNull String friendlyName,
				@NonNull Function <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> localGetOperation,
				@NonNull BiConsumer <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> remoteSetOperation,
				@NonNull Function <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> remoteGetOperation) {

			return new ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			> ()

				.localClass (
					localClass)

				.remoteRequestClass (
					remoteRequestClass)

				.remoteResponseClass (
					remoteResponseClass)

				.valueClass (
					valueClass)

				.friendlyName (
					friendlyName)

				.send (
					true)

				.receive (
					false)

				.compare (
					true)

				.localId (
					false)

				.remoteId (
					false)

				.localGetOperation (
					localGetOperation)

				.remoteGetOperation (
					remoteGetOperation)

				.remoteSetOperation (
					remoteSetOperation)

				.compareOperation (
					(left, right) ->
						optionalEqualOrNotPresentWithClass (
							valueClass,
							genericCastUnchecked (
								left),
							genericCastUnchecked (
								right)))

			;

		}

		public <Value>
		ShopifySynchronisationAttribute <
			Local,
			RemoteRequest,
			RemoteResponse
		> sendSimple (
				@NonNull Class <Value> valueClass,
				@NonNull String friendlyName,
				@NonNull Function <Local, Value> localGetter,
				@NonNull BiConsumer <RemoteRequest, Value> remoteSetter,
				@NonNull Function <RemoteResponse, Value> remoteGetter) {

			return send (
				valueClass,
				friendlyName,

				context ->
					optionalFromNullable (
						localGetter.apply (
							context.local ())),

				(context, value) ->
					remoteSetter.accept (
						context.remoteRequest (),
						optionalOrNull (
							optionalCast (
								valueClass,
								value))),

				context ->
					optionalFromNullable (
						remoteGetter.apply (
							context.remoteResponse ()))

			);

		}

		public
		ShopifySynchronisationAttribute <
			Local,
			RemoteRequest,
			RemoteResponse
		> receive (
				@NonNull Class <?> valueClass,
				@NonNull String friendlyName,
				@NonNull Function <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> remoteGetOperation,
				@NonNull BiConsumer <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> localSetOperation,
				@NonNull Function <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> localGetOperation) {

			return new ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			> ()

				.localClass (
					localClass)

				.remoteRequestClass (
					remoteRequestClass)

				.remoteResponseClass (
					remoteResponseClass)

				.valueClass (
					valueClass)

				.friendlyName (
					friendlyName)

				.send (
					false)

				.receive (
					true)

				.compare (
					true)

				.localId (
					false)

				.remoteId (
					false)

				.localGetOperation (
					localGetOperation)

				.localSetOperation (
					localSetOperation)

				.remoteGetOperation (
					remoteGetOperation)

				.compareOperation (
					(left, right) ->
						optionalEqualOrNotPresentWithClass (
							valueClass,
							genericCastUnchecked (
								left),
							genericCastUnchecked (
								right)))

			;

		}

		public <Value>
		ShopifySynchronisationAttribute <
			Local,
			RemoteRequest,
			RemoteResponse
		> receiveSimple (
				@NonNull Class <Value> valueClass,
				@NonNull String friendlyName,
				@NonNull Function <RemoteResponse, Value> remoteGetter,
				@NonNull BiConsumer <Local, Value> localSetter,
				@NonNull Function <Local, Value> localGetter) {

			return receive (
				valueClass,
				friendlyName,

				context ->
					optionalFromNullable (
						remoteGetter.apply (
							context.remoteResponse ())),

				(context, value) ->
					localSetter.accept (
						context.local (),
						optionalOrNull (
							optionalCast (
								valueClass,
								value))),

				context ->
					optionalFromNullable (
						localGetter.apply (
							context.local ()))

			);

		}

		public
		ShopifySynchronisationAttribute <
			Local,
			RemoteRequest,
			RemoteResponse
		> remoteId (
				@NonNull Class <?> valueClass,
				@NonNull String friendlyName,
				@NonNull Function <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> remoteGetOperation,
				@NonNull BiConsumer <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> localSetOperation,
				@NonNull Function <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> localGetOperation,
				@NonNull BiConsumer <
					Context <Local, RemoteRequest, RemoteResponse>,
					Optional <Object>
				> remoteSetOperation) {

			return new ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			> ()

				.localClass (
					localClass)

				.remoteRequestClass (
					remoteRequestClass)

				.remoteResponseClass (
					remoteResponseClass)

				.valueClass (
					valueClass)

				.friendlyName (
					friendlyName)

				.send (
					false)

				.receive (
					true)

				.compare (
					true)

				.localId (
					false)

				.remoteId (
					true)

				.localGetOperation (
					localGetOperation)

				.localSetOperation (
					localSetOperation)

				.remoteGetOperation (
					remoteGetOperation)

				.remoteSetOperation (
					remoteSetOperation)

				.compareOperation (
					(left, right) ->
						optionalEqualOrNotPresentWithClass (
							valueClass,
							genericCastUnchecked (
								left),
							genericCastUnchecked (
								right)))

			;

		}

		public <Value>
		ShopifySynchronisationAttribute <
			Local,
			RemoteRequest,
			RemoteResponse
		> remoteIdSimple (
				@NonNull Class <Value> valueClass,
				@NonNull String friendlyName,
				@NonNull Function <RemoteResponse, Value> remoteGetter,
				@NonNull BiConsumer <Local, Value> localSetter,
				@NonNull Function <Local, Value> localGetter,
				@NonNull BiConsumer <RemoteRequest, Value> remoteSetter) {

			return remoteId (
				valueClass,
				friendlyName,

				context ->
					optionalFromNullable (
						remoteGetter.apply (
							context.remoteResponse ())),

				(context, value) ->
					localSetter.accept (
						context.local (),
						optionalOrNull (
							optionalCast (
								valueClass,
								value))),

				context ->
					optionalFromNullable (
						localGetter.apply (
							context.local ())),

				(context, value) ->
					remoteSetter.accept (
						context.remoteRequest (),
						optionalOrNull (
							optionalCast (
								valueClass,
								value)))

			);

		}

	}

}
