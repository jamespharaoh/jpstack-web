package wbs.framework.fixtures;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;

@SingletonComponent ("testAccounts")
public
class TestAccounts {

	// properties

	@Getter @Setter
	String path =
		"conf/test-accounts.xml";

	// state

	@Getter
	TestAccountsSpec testAccounts;

	// life cycle

	@PostConstruct
	public
	void setup () {

		File testAccountsFile =
			new File (
				"conf/test-accounts.xml");

		if (! testAccountsFile.exists ()) {

			testAccounts =
				new TestAccountsSpec ();

			return;

		}

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClasses (
				TestAccountsSpec.class,
				TestAccountSpec.class);

		testAccounts =
			(TestAccountsSpec)
			dataFromXml.readFilename (
				"conf/test-accounts.xml");

	}

	// implementation

	public
	void forEach (
			@NonNull String type,
			@NonNull Consumer<Map<String,String>> closure) {

		forType (
			type)

			.stream ()

			.map (
				account ->
					account.params ())

			.forEach (
				closure);

	}

	public
	List<TestAccountSpec> forType (
			@NonNull String type) {

		return testAccounts.accounts ().stream ()

			.filter (
				account ->
					equal (
						account.type (),
						type))

			.collect (
				Collectors.toList ());

	}

}
