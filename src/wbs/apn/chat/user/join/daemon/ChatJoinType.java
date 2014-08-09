package wbs.apn.chat.user.join.daemon;

import wbs.apn.chat.keyword.model.ChatKeywordJoinType;

public
enum ChatJoinType {

	chatSimple,
	chatSetInfo,
	chatNext,
	chatLocation,
	chatPrefs,
	chatGender,
	chatGenderOther,
	chatDob,
	chatCharges,
	chatPics,
	chatVideos,
	chatAge,

	dateSimple,
	dateSetInfo,
	dateLocation,
	dateGender,
	dateGenderOther,
	dateDob,
	dateCharges,
	dateSetPhoto;

	public static
	ChatJoinType convertJoinType (
			ChatKeywordJoinType in) {

		if (in == null)
			throw new NullPointerException ();

		switch (in) {

			case chatSimple: return chatSimple;
			case chatSetInfo: return chatSetInfo;
			case chatNext: return chatNext;
			case chatLocation: return chatLocation;
			case chatDob: return chatDob;
			case chatPics: return chatPics;
			case chatVideos: return chatVideos;
			case dateSimple: return dateSimple;
			case dateSetInfo: return dateSetInfo;
			case dateDob: return dateDob;
			case dateLocation: return dateLocation;

			default: throw new RuntimeException (
				"Uknown keyword join type: " + in);

		}

	}

}