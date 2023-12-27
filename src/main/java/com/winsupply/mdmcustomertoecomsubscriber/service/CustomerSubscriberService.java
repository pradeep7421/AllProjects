package com.winsupply.mdmcustomertoecomsubscriber.service;

import com.win.email.service.EmailService;
import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.config.EmailConfig;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerResupply;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerResupplyId;
import com.winsupply.mdmcustomertoecomsubscriber.exception.ECMException;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AtgAccount;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Email;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.FederalId;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.PhoneVO;
import com.winsupply.mdmcustomertoecomsubscriber.processor.AddressProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.ContactProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.CustomerAccountProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.CustomerMergeProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerResupplyRepository;
import jakarta.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Customer Subscriber Service
 *
 * @author Amritanshu
 */
@Service
@RequiredArgsConstructor
public class CustomerSubscriberService {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final ResourceBundle mResourceBundle = ResourceBundle.getBundle(Constants.MESSAGE_BUNDLE, Locale.US);

    private final CustomerRepository mCustomerRepository;

    private final CustomerResupplyRepository mCustomerResupplyRepository;

    private final CustomerAccountProcessor mCustomerAccountProcessor;

    private final AddressProcessor mAddressProcessor;

    private final CustomerMergeProcessor mCustomerMergeProcessor;

    private final ContactProcessor mContactProcessor;

    private EmailService mEmailService;

    private final EmailConfig mEmailConfig;

    private int mFailureCount;

    private int mSuccessCount;

    private Timer mTimer;

    /**
     * <b>initialize</b> - Initialize the EmailService component
     */
    @PostConstruct
    public void initialize() {
        mEmailService = new EmailService(mEmailConfig.getMailFrom(), mEmailConfig.getMailTos(), mEmailConfig.getHost(), mEmailConfig.getThreshold(),
                mEmailConfig.getSecondThreshold(), mEmailConfig.getThirdThreshold(), mEmailConfig.getSuccessThreshold(), mEmailConfig.getBatchSize(),
                mEmailConfig.getTimePeriod());
        final TimerTask lTimeTask = mEmailService.initializeTimerTask();
        mTimer = new Timer();
        mTimer.schedule(lTimeTask, 0, mEmailConfig.getTimePeriod());

    }

    /**
     * Process the Customer Message
     *
     * @param pPayload        - the Payload
     * @param pMessageHeaders - the Message Headers
     */
    @Transactional
    public void processCustomerMessage(final String pPayload, final MessageHeaders pMessageHeaders) {
        final String lActionCode = (String) pMessageHeaders.get("action_code");
        String lCustomerECMId = null;
        try {
            final CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(pPayload, CustomerMessageVO.class);
            lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

            mLogger.info("Processing customerECMId {}, Action {}...", lCustomerECMId, lActionCode);
            if (!StringUtils.hasText(lCustomerECMId)) {
                mLogger.warn("SKIPING as customerECMId is missing for - {}", pPayload);
                return;
            }
            if (StringUtils.hasText(lActionCode) && "delete".equalsIgnoreCase(lActionCode)) {
                // TODO deleteCustomer(lCustomerEcmId);
            } else {
                final Customer lCustomer = createOrUpdateCustomer(lCustomerMessageVO);
                mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);

                final List<AtgAccount> lExistingCustomers = lCustomerMessageVO.getAtgAccounts();
                if (lExistingCustomers != null && (lExistingCustomers.size() > 1
                        || (lExistingCustomers.size() == 1 && !lExistingCustomers.get(0).getAtgSystemSrcId().equals(lCustomer.getCustomerECMId())))) {
                    mCustomerMergeProcessor.mergeCustomer(lCustomer, lExistingCustomers);
                }
            }

            sendSuccessEmail();
        } catch (final Exception lException) {
            mLogger.error("Exception -> ", lException);
            sendFailureEmail(lCustomerECMId, lException);
        } finally {
            mCustomerAccountProcessor.setSubAccountCustomerNumbers(null);
        }
    }

    /**
     * <b>createOrUpdateCustomer</b> - Create Or Update customer
     *
     * @param pCustomerMessageVO - the CustomerMessage VO
     * @return - Customer
     * @throws ECMException - the ECMException
     */
    private Customer createOrUpdateCustomer(final CustomerMessageVO pCustomerMessageVO) throws ECMException {
        final String lCustomerECMId = pCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = null;
        final Optional<Customer> lCustomerDBRecord = mCustomerRepository.findById(lCustomerECMId);

        if (lCustomerDBRecord.isEmpty()) {
            lCustomer = new Customer();
            lCustomer.setCustomerECMId(lCustomerECMId);
        } else {
            mLogger.debug("Customer with customerECMId: {} is found in DB", lCustomerECMId);
            lCustomer = lCustomerDBRecord.get();
        }
        lCustomer.setCustomerName(pCustomerMessageVO.getFullName());
        lCustomer.setFederalTaxId(getFederalTaxId(pCustomerMessageVO.getFederalIds()));
        lCustomer.setWincca(pCustomerMessageVO.getWinCCA());
        lCustomer = mCustomerRepository.save(lCustomer);

        setResupplyLocation(pCustomerMessageVO);

        final Map<String, Account> lFilteredAccounts = validateAndFilterAccounts(pCustomerMessageVO.getWiseAccounts());
        final String lInterCompanyId = pCustomerMessageVO.getInterCompanyId();

        mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, lFilteredAccounts);

        importPhonesData(lCustomer, pCustomerMessageVO.getPhones());

        importElectronicAddressesData(lCustomer, pCustomerMessageVO.getEmails());

        mAddressProcessor.importAddressesData(lCustomer, pCustomerMessageVO.getAddresses());

        return lCustomer;
    }

    /**
     * <b>setResupplyLocation</b> - It sets the resupply location data
     *
     * @param pCustomerMessageVO - the pCustomerMessageVO
     */
    private void setResupplyLocation(final CustomerMessageVO pCustomerMessageVO) {
        mCustomerResupplyRepository.deleteAllByCustomerECMId(pCustomerMessageVO.getCustomerEcmId());

        if (pCustomerMessageVO.getResupplyLocations() != null && !pCustomerMessageVO.getResupplyLocations().isEmpty()) {
            final List<CustomerResupply> lResupplyLocations = pCustomerMessageVO.getResupplyLocations().stream()
                    .map(lResupplyLocation -> createResupplyLocation(pCustomerMessageVO.getCustomerEcmId(), lResupplyLocation)).toList();
            mCustomerResupplyRepository.saveAll(lResupplyLocations);
        }
    }

    /**
     * <b>importElectronicAddressesData</b> - This method set the customer's email
     *
     * @param pCustomer - the Customer
     * @param pEmails   - the E-mails
     */
    private void importElectronicAddressesData(final Customer pCustomer, final List<Email> pEmails) {
        if (pEmails != null && !pEmails.isEmpty()) {
            for (final Email lEmail : pEmails) {
                final String lEmailAddress = lEmail.getEmailAddress();
                final String lType = lEmail.getEmailType();
                if (StringUtils.hasText(lType) && Constants.EW_EMAIL_TYPE.equalsIgnoreCase(lType)) {
                    pCustomer.setEmail(lEmailAddress);
                    break;
                }
            }
        } else {
            mLogger.debug("pEmails are empty, Resetting the email property");
            pCustomer.setEmail(null);
        }
    }

    /**
     * <b>importPhonesData</b> - This method imports the phones details
     *
     * @param pCustomer - the Customer
     * @param pPhones   - the Phones
     */
    private void importPhonesData(final Customer pCustomer, final List<PhoneVO> pPhones) {
        String lPhoneNumber = null;
        String lFaxNumber = null;
        if (pPhones != null && !pPhones.isEmpty()) {
            for (final PhoneVO lPhone : pPhones) {
                final String lType = lPhone.getPhoneType();
                if (StringUtils.hasText(lPhone.getPhoneNumber()) && StringUtils.hasText(lType)) {
                    if (Constants.LB.equalsIgnoreCase(lType)) {
                        lPhoneNumber = lPhone.getPhoneNumber().replaceAll(Constants.PHONE_EXTRACT_REGEX, "");
                    } else if (Constants.FX.equalsIgnoreCase(lType)) {
                        lFaxNumber = lPhone.getPhoneNumber().replaceAll(Constants.PHONE_EXTRACT_REGEX, "");
                    }
                }
            }
        }
        pCustomer.setPhone(lPhoneNumber);
     // TODO - BLOCKED pCustomer.setFax(lFaxNumber);
    }

    /**
     * <b>validateAndFilterAccounts</b> - Validates and filters WISE accounts
     *
     * @param pWiseAccounts - the WISE Accounts
     * @return Map<String, Account>
     * @throws ECMException - the ECMException
     */
    private Map<String, Account> validateAndFilterAccounts(final List<Account> pWiseAccounts) throws ECMException {
        Map<String, Account> lFilteredAccountsMap = null;
        if (pWiseAccounts != null && !pWiseAccounts.isEmpty()) {
            lFilteredAccountsMap = new LinkedHashMap<>(pWiseAccounts.size());
            for (final Account lWiseAccount : pWiseAccounts) {
                processWiseAccount(lFilteredAccountsMap, lWiseAccount);
            }
        }
        mLogger.debug("lFilteredAccountsMap : {}", lFilteredAccountsMap);
        return lFilteredAccountsMap;

    }

    /**
     * <b>processWiseAccount</b> - Process WISE Account
     *
     * @param pFilteredAccountsMap - the Filtered Accounts Map
     * @param pWiseAccount         - the WISE Account
     * @throws ECMException - the ECMException
     */
    private void processWiseAccount(final Map<String, Account> pFilteredAccountsMap, final Account pWiseAccount) throws ECMException {
        final String lLocalCompNumber = pWiseAccount.getCompanyNumber();
        final String lAccountEcomStatus = pWiseAccount.getAccountEcommerceStatus();

        if (StringUtils.hasText(lLocalCompNumber)) {
            if (pFilteredAccountsMap.containsKey(lLocalCompNumber)) {
                final Account lPrevWiseCustomer = pFilteredAccountsMap.get(lLocalCompNumber);
                final String lPrevAccountEcomStatus = lPrevWiseCustomer.getAccountEcommerceStatus();

                if (Constants.YES_KEY.equals(lAccountEcomStatus) && Constants.YES_KEY.equals(lPrevAccountEcomStatus)) {
                    throw new ECMException(mResourceBundle.getString("duplicate.accountEcomStatus"));
                } else if (Constants.YES_KEY.equals(lAccountEcomStatus)) {
                    pFilteredAccountsMap.put(lLocalCompNumber, pWiseAccount);
                }
            } else if (Constants.YES_KEY.equals(lAccountEcomStatus)) {
                pFilteredAccountsMap.put(lLocalCompNumber, pWiseAccount);
            }
        }
    }

    /**
     * <b>getFederalTaxId</b> this method will return federalTaxId
     *
     * @param pFederalIds - the Federal Ids
     * @return String
     */
    private String getFederalTaxId(final List<FederalId> pFederalIds) {
        String lFederalTaxId = null;
        if (pFederalIds != null && !pFederalIds.isEmpty()) {
            for (final FederalId lFederalIdNode : pFederalIds) {
                if ("tax_fed".equalsIgnoreCase(lFederalIdNode.getTaxIdType())) {
                    lFederalTaxId = lFederalIdNode.getFederalId();
                    break;
                }
            }
        }
        return lFederalTaxId;
    }

    /**
     * <b>createResupplyLocation</b> - Create Resupply Location
     *
     * @param pCustomerECMId    - the Customer ECM Id
     * @param pResupplyLocation - the Resupply Location
     * @return CustomerResupply
     */
    private CustomerResupply createResupplyLocation(final String pCustomerECMId, final String pResupplyLocation) {
        final CustomerResupplyId lCustomerResupplyId = CustomerResupplyId.builder().customerECMId(pCustomerECMId).resupplyLocation(pResupplyLocation)
                .build();
        return CustomerResupply.builder().id(lCustomerResupplyId).build();
    }

    /**
     * <b>sendSuccessEmail</b> - It sends the success email
     *
     */
    private void sendSuccessEmail() {
        mSuccessCount++;
        final String lFailureEmailSubject = MessageFormat.format(mResourceBundle.getString(Constants.EMAIL_FAILURE_SUBJECT),
                mEmailConfig.getEnvironment());
        final String lPayloadStorageStr = mEmailService.getPayloadStorage() == null ? "" : mEmailService.getPayloadStorage().toString();
        final String lFailureEmailMessage = MessageFormat.format(mResourceBundle.getString(Constants.EMAIL_FAILURE_BODY),
                "Failed Payload  ->  " + lPayloadStorageStr);

        final String lSuccessEmailSubject = MessageFormat.format(mResourceBundle.getString(Constants.EMAIL_SUCCESS_SUBJECT),
                mEmailConfig.getEnvironment());
        final String lSuccessEmailMessage = mResourceBundle.getString(Constants.EMAIL_SUCCESS_BODY);
        final boolean lIsSuccess = mEmailService.successCheck(mFailureCount, mSuccessCount, lFailureEmailMessage, lFailureEmailSubject,
                lSuccessEmailMessage, lSuccessEmailSubject);
        if (lIsSuccess) {
            mFailureCount = 0;
        }
    }

    /**
     * <b>sendFailureEmail</b> - It sends the failure email
     *
     * @param pCustomerECMId - the pCustomerECMId
     * @param pException - the Exception
     */
    private void sendFailureEmail(final String pCustomerECMId, final Exception pException) {
        mFailureCount++;
        mSuccessCount = 0;

        final StringWriter lErrorWriter = new StringWriter();
        pException.printStackTrace(new PrintWriter(lErrorWriter));
        String lErrorStr = lErrorWriter.toString();
        if (lErrorStr.length() > 600) {
            lErrorStr = lErrorStr.substring(0, 600);
        }
        final String lMessage = "Message with Customer ECM Id  ->  " + pCustomerECMId + "     ::     Exception ->  " + lErrorStr;

        final String lEmailSubject = MessageFormat.format(mResourceBundle.getString(Constants.EMAIL_FAILURE_SUBJECT), mEmailConfig.getEnvironment());
        final String lEmailMessage = MessageFormat.format(mResourceBundle.getString(Constants.EMAIL_FAILURE_BODY), lMessage);
        mEmailService.setEmailSubject(lEmailSubject);
        mEmailService.setEmailBody(lEmailMessage);
        mEmailService.failureCheck(mFailureCount, pCustomerECMId, "IMPORTANT");
    }
}
