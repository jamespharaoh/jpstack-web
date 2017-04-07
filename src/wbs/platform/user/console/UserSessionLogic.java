package wbs.platform.user.console;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.io.Serializable;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.SerializationUtils;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

import wbs.utils.string.StringUtils;

public
interface UserSessionLogic {

	UserSessionRec userLogon (
			TaskLogger parentTaskLogger,
			UserRec user,
			Optional <String> userAgent,
			Optional <String> consoleDeploymentCode);

	Optional <UserSessionRec> userLogonTry (
			TaskLogger parentTaskLogger,
			String sliceCode,
			String username,
			String password,
			Optional <String> userAgent,
			Optional <String> consoleDeploymentCode);

	void userLogoff (
			TaskLogger parentTaskLogger,
			UserRec user);

	boolean userSessionVerify (
			TaskLogger parentTaskLogger);

	Optional <byte[]> userData (
			UserRec user,
			String code);

	default
	byte[] userDataRequired (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalGetRequired (
			userData (
				user,
				code));

	}

	void userDataStore (
			TaskLogger taskLogger,
			UserRec user,
			String code,
			byte[] value);

	void userDataRemove (
			UserRec user,
			String code);

	default
	Optional <Serializable> userDataObject (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalMapRequired (
			userData (
				user,
				code),
			SerializationUtils::deserialize);

	}

	default
	Serializable userDataObjectRequired (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalGetRequired (
			userDataObject (
				user,
				code));

	}

	default
	void userDataObjectStore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull String code,
			@NonNull Serializable value) {

		userDataStore (
			parentTaskLogger,
			user,
			code,
			SerializationUtils.serialize (
				value));

	}

	default
	Optional <String> userDataString (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalMapRequired (
			userData (
				user,
				code),
			StringUtils::utf8ToString);

	}

	default
	String userDataStringRequired (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalGetRequired (
			userDataString (
				user,
				code));

	}

	default
	void userDataStringStore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull String code,
			@NonNull String value) {

		userDataStore (
			parentTaskLogger,
			user,
			code,
			stringToUtf8 (
				value));

	}

}
