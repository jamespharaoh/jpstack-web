package shn.shopify.apiclient.product;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
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
@PrototypeComponent ("shopifyProductRemoveHttpSenderHelper")
public
class ShopifyProductRemoveHttpSenderHelper
	implements GenericHttpSenderHelper <
		ShopifyProductRemoveRequest,
		ShopifyProductRemoveResponse
	> {

	// properties

	@Getter @Setter
	ShopifyProductRemoveRequest request;

	@Getter @Setter
	ShopifyProductRemoveResponse response;

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
		return Method.delete;
	}

	@Override
	public
	String url () {

		return stringFormat (
			"https://%s.myshopify.com/admin/products/%s.json",
			request.credentials ().storeName (),
			integerToDecimalString (
				request.id ()));

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
				ShopifyProductRemoveResponse.class,
				responseBody);

	}

}
