package shn.shopify.apiclient;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.apiclient.GenericHttpSender.Method;
import wbs.framework.apiclient.GenericHttpSenderHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.data.tools.DataToJson;

@Accessors (fluent = true)
@PrototypeComponent ("shopifyProductCreateHttpSenderHelper")
public
class ShopifyProductCreateHttpSenderHelper
	implements GenericHttpSenderHelper <
		ShopifyProductCreateRequest,
		ShopifyProductCreateResponse
	> {

	// properties

	@Getter @Setter
	ShopifyProductCreateRequest request;

	@Getter @Setter
	ShopifyProductCreateResponse response;

	@Getter @Setter
	Long responseStatusCode;

	@Getter @Setter
	String responseStatusReason;

	@Getter @Setter
	Map <String, List <String>> responseHeaders;

	@Getter @Setter
	String requestBody;

	@Getter @Setter
	String responseBody;

	// details

	@Override
	public
	Method method () {
		return Method.post;
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

		return emptyMap ();

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

			.put (
				"Content-Type",
				"application/json; charset=utf-8")

			.build ();

	}

	@Override
	public
	void verify () {

		doNothing ();

	}

	@Override
	public
	void encode () {

		DataToJson dataToJson =
			new DataToJson ();

		JsonElement jsonValue =
			dataToJson.toJson (
				request);

		requestBody =
			jsonValue.toString ();

	}

	@Override
	public
	void decode () {

		DataFromJson dataFromJson =
			new DataFromJson ();

		response =
			dataFromJson.fromJson (
				ShopifyProductCreateResponse.class,
				responseBody);

	}

}
