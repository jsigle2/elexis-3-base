package ch.elexis.base.ch.arzttarife.xml.exporter;

import java.util.HashMap;
import java.util.Map;

import ch.fd.invoice450.request.ServiceExType;
import ch.fd.invoice450.request.ServiceType;
import ch.fd.invoice450.request.ServicesType;
import ch.rgw.tools.Money;

public class ServicesFinancialInfo {
	
	private double obligationSum;
	
	private Map<String, Double> tariffSum;
	
	private VatRateSum vatRateSum;
	
	public static ServicesFinancialInfo of(ServicesType services){
		ServicesFinancialInfo ret = new ServicesFinancialInfo();
		
		for (Object obj : services.getServiceExOrService()) {
			if (obj instanceof ServiceExType) {
				ret.addTariffAmount(((ServiceExType) obj)
					.getTariffType(),
					((ServiceExType) obj).getAmount());
				if (((ServiceExType) obj).isObligation()) {
					ret.addObligationAmount(((ServiceExType) obj).getAmount());
				}
				ret.addVatAmount(((ServiceExType) obj).getVatRate(),
					((ServiceExType) obj).getAmount());
			} else if (obj instanceof ServiceType) {
				ret.addTariffAmount(((ServiceType) obj).getTariffType(),
					((ServiceType) obj).getAmount());
				ret.addVatAmount(((ServiceType) obj).getVatRate(), ((ServiceType) obj).getAmount());
			}
		}
		
		return ret;
	}
	
	public ServicesFinancialInfo(){
		tariffSum = new HashMap<>();
		vatRateSum = new VatRateSum();
		obligationSum = 0.0;
	}
	
	private void addVatAmount(double vatRate, double amount){
		vatRateSum.add(vatRate, amount);
	}
	
	private void addTariffAmount(String tariffType, double amount){
		Double sum = tariffSum.get(tariffType);
		if (sum == null) {
			sum = new Double(0.0);
		}
		sum += amount;
		tariffSum.put(tariffType, sum);
	}
	
	private void addObligationAmount(double amount){
		obligationSum += amount;
	}
	
	public Money getObligationSum(){
		return new Money(obligationSum);
	}
	
	public VatRateSum getVatRateSum(){
		return vatRateSum;
	}
}
