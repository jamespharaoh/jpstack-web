package wbs.psychic.affiliate.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.GrandParentField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.psychic.affiliategroup.model.PsychicAffiliateGroupRec;
import wbs.psychic.core.model.PsychicRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class PsychicAffiliateRec
	implements MajorRecord<PsychicAffiliateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PsychicAffiliateGroupRec psychicAffiliateGroup;

	@CodeField
	String code;

	// details

	@GrandParentField
	PsychicRec psychic;

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField (nullable = true)
	String welcomeMessage;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicAffiliateRec> otherRecord) {

		PsychicAffiliateRec other =
			(PsychicAffiliateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPsychicAffiliateGroup (),
				other.getPsychicAffiliateGroup ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// dao

	public static
	interface PsychicAffiliateDaoMethods {

		List<PsychicAffiliateRec> findByPsychic (
			int psychicId);

	}

}
