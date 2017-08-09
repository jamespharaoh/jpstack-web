package wbs.console.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

public
class TabContext {

	List<Layer> layers =
		new ArrayList<Layer> ();

	public
	TabContext (
			String title,
			TabList tabList) {

		Layer firstLayer =
			new Layer ();

		firstLayer.title =
			title;

		firstLayer.tabList =
			tabList;

		layers.add (
			firstLayer);

	}

	public
	void add (
			@NonNull Tab parentTab,
			@NonNull String title,
			@NonNull TabList tabList) {

		Layer lastLayer =
			layers.get (
				layers.size () - 1);

		lastLayer.tab =
			parentTab;

		Layer nextLayer =
			new Layer ();

		nextLayer.title =
			title;

		nextLayer.tabList =
			tabList;

		layers.add (
			nextLayer);

	}

	public
	Collection<Layer> getLayers () {

		return layers;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Layer {

		String title;
		TabList tabList;
		Tab tab;

	}

}
