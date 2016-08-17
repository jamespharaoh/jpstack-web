package wbs.framework.utils.etc;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import lombok.NonNull;

public
class CollectionUtils {

	public static
	Properties toProperties (
			@NonNull Map <String, String> propertiesMap) {

		Properties properties =
			new Properties ();

		properties.putAll (
			propertiesMap);

		return properties;

	}

	public static <Type>
	Type collectionAdd (
			@NonNull Collection <? super Type> list,
			@NonNull Type item) {

		list.add (
			item);

		return item;

	}

	public static
	long collectionSize (
			@NonNull Collection <?> collection) {

		return collection.size ();

	}

	public static
	long iterableCount (
			@NonNull Iterable <?> iterable) {

		long size = 0;

		for (
			@SuppressWarnings ("unused")
			Object _item
				: iterable
		) {
			size ++;
		}

		return size;

	}

}
