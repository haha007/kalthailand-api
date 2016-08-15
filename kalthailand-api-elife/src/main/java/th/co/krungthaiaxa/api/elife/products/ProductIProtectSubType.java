package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.DatedAmount;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.repository.ProductIGenRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isFalse;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amountTHB;

public enum ProductIProtectSubType {
    IPROTECT_5, IPROTECT10, IPROTECT85;
}
