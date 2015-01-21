package wbs.imchat.core.model;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class ImChatCustomerRec
	implements CommonRecord<ImChatCustomerRec>{

	// identity

	@GeneratedIdField
	Integer id;

	@ParentField
	ImChatRec imChat;

	@CodeField
	String code;

	// statistics

	@SimpleField
	Integer numConversations = 0;

	// object helper methods

	public
	interface ImChatCustomerObjectHelperMethods {

		String generateCode ();

	}

	// object helper implementation

	public static
	class ImChatCustomerObjectHelperImplementation
		implements ImChatCustomerObjectHelperMethods {

		// dependencies

		@Inject
		Random random;

		// implementation

		@Override
		public
		String generateCode () {

			int intCode =
				+ random.nextInt (90000000)
				+ 10000000;

			return Integer.toString (
				intCode);

		}

	}

	// dao methods

	public static
	interface ImChatCustomerDaoMethods {

		List<Integer> searchIds (
				ImChatCustomerSearch imChatCustomerSearch);

	}

	// object hooks

	public static
	class ImChatCustomerHooks
		extends AbstractObjectHooks<ImChatCustomerRec> {

		// dependencies

		@Inject
		ImChatCustomerDao imChatCustomerDao;

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			ImChatCustomerSearch imChatCustomerSearch =
				(ImChatCustomerSearch) search;

			return imChatCustomerDao.searchIds (
					imChatCustomerSearch);

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatCustomerRec> otherRecord) {

		ImChatCustomerRec other =
			(ImChatCustomerRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getImChat (),
				other.getImChat ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
