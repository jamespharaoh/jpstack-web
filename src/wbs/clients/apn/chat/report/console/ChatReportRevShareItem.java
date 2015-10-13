package wbs.clients.apn.chat.report.console;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.currency.model.CurrencyRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
public
class ChatReportRevShareItem
	implements CommonRecord<ChatReportRevShareItem> {

	Integer id;
	AffiliateRec affiliate;
	CurrencyRec currency;

	String path = "";
	String description = "";

	Long outRev = 0L;
	Long inRev = 0L;
	Long smsCost = 0L;
	Long mmsCost = 0L;
	Long outRevNum = 0L;
	Long inRevNum = 0L;
	Long smsCostNum = 0L;
	Long mmsCostNum = 0L;
	Long creditRev = 0L;
	Long joiners = 0L;
	Long staffCost = 0L;

	public Long getTotal(){
		return outRev + inRev + creditRev - smsCost - mmsCost;
	}

	public Long getTotal_p(){
		return (getTotal()/100);
	}

	public Long getCreditRev_p() {
		return creditRev/100;
	}

	public Long getOutRev_p() {
		return outRev/100;
	}


	public Long getInRev_p() {
		return inRev/100;
	}

	public Long getSmsCost_p() {
		return smsCost/100;
	}


	public Long getMmsCost_p() {
		return mmsCost/100;
	}

	public Long staffCost_p() {
		return staffCost/100;
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatReportRevShareItem> otherRecord) {

		ChatReportRevShareItem other =
			(ChatReportRevShareItem) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPath (),
				other.getPath ())

			.toComparison ();

	}

}
