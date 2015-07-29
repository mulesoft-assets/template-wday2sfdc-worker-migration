/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

import com.workday.hr.EventTargetTransactionLogEntryDataType;
import com.workday.hr.TransactionLogEntryType;
import com.workday.hr.WorkerType;

public class TransactionLogProcessor implements MessageProcessor{

	@Override
	public MuleEvent process(MuleEvent event) throws MuleException {
		WorkerType worker = (WorkerType) event.getMessage().getPayload();
		EventTargetTransactionLogEntryDataType log = worker.getWorkerData().getTransactionLogEntryData();
		Calendar lastModifiedDate = null;
		if (log != null) {
			Calendar now = Calendar.getInstance();
			for (TransactionLogEntryType entry : log.getTransactionLogEntry()) {
				if (entry.getTransactionLogData().getTransactionEntryMoment() != null
						&& entry.getTransactionLogData().getTransactionEntryMoment().compareTo(now) <= 0) {
					if (entry.getTransactionLogData().getTransactionEffectiveMoment() != null
							&& entry.getTransactionLogData().getTransactionEffectiveMoment().compareTo(now) <= 0) {
						if (lastModifiedDate != null && entry.getTransactionLogData().getTransactionEntryMoment().compareTo(lastModifiedDate) > 0) {
							lastModifiedDate = entry.getTransactionLogData().getTransactionEntryMoment();
						} else
							lastModifiedDate = entry.getTransactionLogData().getTransactionEntryMoment();
					} else if (entry.getTransactionLogData().getTransactionEffectiveMoment() == null) {
						if (lastModifiedDate == null
								|| (lastModifiedDate != null && entry.getTransactionLogData().getTransactionEntryMoment().compareTo(lastModifiedDate) > 0))
							lastModifiedDate = entry.getTransactionLogData().getTransactionEntryMoment();
					}
				}
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.getMessage().setPayload(lastModifiedDate == null ? null : sdf.format(lastModifiedDate.getTime()));
		return event;
	}
	
}
