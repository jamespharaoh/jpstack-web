package shn.shopify.apiclient;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.presentInstancesMap;
import static wbs.utils.string.StringUtils.joinWithComma;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.apiclient.GenericHttpSender.Method;
import wbs.framework.apiclient.GenericHttpSenderHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;

@Accessors (fluent = true)
@PrototypeComponent ("shopifyProductListHttpSenderHelper")
public
class ShopifyProductListHttpSenderHelper
	implements GenericHttpSenderHelper <
		ShopifyProductListRequest,
		ShopifyProductListResponse
	> {

	// properties

	@Getter @Setter
	ShopifyProductListRequest request;

	@Getter @Setter
	ShopifyProductListResponse response;

	@Getter @Setter
	Long responseStatusCode;

	@Getter @Setter
	String responseStatusReason;

	@Getter @Setter
	Map <String, List <String>> responseHeaders;

	@Getter @Setter
	String responseBody;

	// details

	@Override
	public
	Method method () {
		return Method.get;
	}

	@Override
	public
	String url () {

		return stringFormat (
			"https://%s.myshopify.com/admin/products.json",
			request.credentials ().storeName ());

	}

	@Override
	public
	Map <String, List <String>> requestParameters () {

		return presentInstancesMap (

			Pair.of (
				"ids",
				optionalMapRequired (
					optionalFromNullable (
						request.ids ()),
					ids ->
						singletonList (
							joinWithComma (
								integerToDecimalString (
									ids))))),

			Pair.of (
				"limit",
				optionalMapRequired (
					optionalFromNullable (
						request.limit ()),
					limit ->
						singletonList (
							integerToDecimalString (
								limit)))),

			Pair.of (
				"page",
				optionalMapRequired (
					optionalFromNullable (
						request.page ()),
					page ->
						singletonList (
							integerToDecimalString (
								page + 1))))

		);

	}

	@Override
	public
	Map <String, String> requestHeaders () {

		return ImmutableMap.<String, String> builder ()

			.put (
				"Authorization",
				stringFormat (
					"Basic %s",
					bytesToBase64 (
						stringToUtf8 (
							stringFormat (
								"%s:%s",
								request.credentials ().username (),
								request.credentials ().password ())))))

			.build ();

	}

	@Override
	public
	String requestBody () {

		return "";

	}

	@Override
	public
	void verify () {

		doNothing ();

	}

	@Override
	public
	void encode () {

		doNothing ();

	}

	@Override
	public
	void decode () {

		JSONObject jsonObject =
			(JSONObject)
			JSONValue.parse (
				responseBody);

		DataFromJson dataFromJson =
			new DataFromJson ();

		response =
			dataFromJson.fromJson (
				ShopifyProductListResponse.class,
				jsonObject);

	}

}
