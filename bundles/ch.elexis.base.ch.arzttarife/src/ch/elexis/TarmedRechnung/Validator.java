/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.TarmedRechnung;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class Validator {
	
	public Result<IInvoice> checkBill(final XMLExporter xp, final Result<IInvoice> res){
		IInvoice invoice = xp.invoice;
		IMandator m = invoice.getMandator();
		if (invoice.getState().numericValue() > InvoiceState.OPEN.numericValue()) {
			return res; // Wenn sie eh schon gedruckt war machen wir kein Büro mehr auf
		}
		
		if ((m == null)) {
			invoice.reject(InvoiceState.REJECTCODE.NO_MANDATOR, Messages.Validator_NoMandator);
			res.add(Result.SEVERITY.ERROR, 2, Messages.Validator_NoMandator, invoice, true);
		}
		ICoverage coverage = invoice.getCoverage();
		
		if (coverage == null || !CoverageServiceHolder.get().isValid(coverage)) {
			invoice.reject(InvoiceState.REJECTCODE.NO_CASE, Messages.Validator_NoCase);
			res.add(Result.SEVERITY.ERROR, 4, Messages.Validator_NoCase, invoice, true);
		}

		String ean = TarmedRequirements.getEAN(m);
		if (StringTool.isNothing(ean)) {
			invoice.reject(InvoiceState.REJECTCODE.NO_MANDATOR, Messages.Validator_NoEAN);
			res.add(Result.SEVERITY.ERROR, 3, Messages.Validator_NoEAN, invoice, true);
		}
		if (xp.getDiagnoses().isEmpty()) {
			invoice.reject(InvoiceState.REJECTCODE.NO_DIAG, Messages.Validator_NoDiagnosis);
			res.add(Result.SEVERITY.ERROR, 8, Messages.Validator_NoDiagnosis, invoice, true);
		}
		
		
		IContact costBearer =
			(coverage != null) ? coverage.getCostBearer() : null;
		// kostentraeger is optional for tiers garant else check if valid
		if (costBearer == null && xp.tiers != null && xp.tiers.equals(XMLExporter.TIERS_GARANT)) {
			return res;
		} else {
			if (costBearer == null) {
				invoice.reject(InvoiceState.REJECTCODE.NO_GUARANTOR, Messages.Validator_NoName);
				res.add(Result.SEVERITY.ERROR, 7, Messages.Validator_NoName, invoice, true);
				return res;
			}
			ean = TarmedRequirements.getEAN(costBearer);
			
			if (StringTool.isNothing(ean) || (!ean.matches(TarmedRequirements.EAN_PATTERN))) {
				invoice.reject(InvoiceState.REJECTCODE.NO_GUARANTOR, Messages.Validator_NoEAN2);
				res.add(Result.SEVERITY.ERROR, 6, Messages.Validator_NoEAN2, invoice, true);
			}
			String bez = costBearer.getDescription1();
			if (StringTool.isNothing(bez)) {
				invoice.reject(InvoiceState.REJECTCODE.NO_GUARANTOR, Messages.Validator_NoName);
				res.add(Result.SEVERITY.ERROR, 7, Messages.Validator_NoName, invoice, true);
			}
		}
		return res;
	}
}
