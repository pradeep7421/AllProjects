package com.winsupply.mdmcustomertoecomsubscriber.service;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerResupply;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerResupplyId;
import com.winsupply.mdmcustomertoecomsubscriber.exception.ECMException;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AtgAccount;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Email;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.FederalId;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Phone;
import com.winsupply.mdmcustomertoecomsubscriber.processor.AddressProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.ContactProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.CustomerAccountProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.CustomerMergeProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerResupplyRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
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

    /**
     * Process the Quotes Message
     *
     * @param pPayload        - the Payload
     * @param pMessageHeaders - the Message Headers
     */
    @Transactional
    public void processCustomerMessage(final String pPayload, final MessageHeaders pMessageHeaders) {
        final String lActionCode = (String) pMessageHeaders.get("action_code");
        try {
            final CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(pPayload, CustomerMessageVO.class);
            final String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

            mLogger.info("Processing customerECMId {}, Action {}...", lCustomerECMId, lActionCode);
            if (!StringUtils.hasText(lCustomerECMId)) {
                mLogger.warn("SKIPING as customerECMId is missing for - {}", pPayload);
                return;
            }
            if (StringUtils.hasText(lActionCode) && "delete".equalsIgnoreCase(lActionCode)) {
                // TODO deleteCustomer(lCustomerEcmId);
            } else {
                Customer lCustomer = createOrUpdateCustomer(lCustomerMessageVO);

                final List<AtgAccount> lAtgAccounts = lCustomerMessageVO.getAtgAccounts();
                if (lAtgAccounts != null && (lAtgAccounts.size() > 1
                        || (lAtgAccounts.size() == 1 && !lAtgAccounts.get(0).getAtgSystemSrcId().equals(lCustomer.getCustomerECMId())))) {
                    mCustomerMergeProcessor.mergeCustomer(lCustomer, lAtgAccounts);
                }

                mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
            }

        } catch (final Exception lException) {
            mLogger.error("Exception -> ", lException);
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
        Optional<Customer> lCustomerDBRecord = mCustomerRepository.findById(lCustomerECMId);

        if (lCustomerDBRecord.isEmpty()) {
            lCustomer = new Customer();
            lCustomer.setCustomerECMId(lCustomerECMId);
        } else {
            lCustomer = lCustomerDBRecord.get();
        }
        lCustomer.setCustomerName(pCustomerMessageVO.getFullName());
        lCustomer.setFederalTaxId(getFederalTaxId(pCustomerMessageVO.getFederalIds()));
        lCustomer.setWincca(pCustomerMessageVO.getWinCCA());

        mCustomerResupplyRepository.deleteAllByCustomerECMId(lCustomerECMId);

        if (pCustomerMessageVO.getVmiLocations() != null && !pCustomerMessageVO.getVmiLocations().isEmpty()) {
            final List<CustomerResupply> lResupplyLocations = pCustomerMessageVO.getVmiLocations().stream()
                    .map(lVmiLocation -> createResupplyLocation(lCustomerECMId, lVmiLocation)).toList();
            mCustomerResupplyRepository.saveAll(lResupplyLocations);
        }

        final Map<String, Account> lFilteredAccounts = validateAndFilterAccounts(pCustomerMessageVO.getWiseAccounts());
        final String lInterCompanyId = pCustomerMessageVO.getInterCompanyId();
        lCustomer = mCustomerRepository.save(lCustomer);

        mCustomerAccountProcessor.importCustomerAccountsData(lCustomer, lInterCompanyId, lFilteredAccounts);

        importPhonesData(lCustomer, pCustomerMessageVO.getPhones());

        importElectronicAddressesData(lCustomer, pCustomerMessageVO.getEmails());

        mAddressProcessor.importAddressesData(lCustomer, pCustomerMessageVO.getAddresses());

        return mCustomerRepository.save(lCustomer);
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
                }
            }
        } else {
            mLogger.debug("pEmails is empty, Resetting the email property");
            pCustomer.setEmail(null);
        }
    }

    /**
     * <b>importPhonesData</b> - This method imports the phones details
     *
     * @param pCustomer - the Customer
     * @param pPhones   - the Phones
     */
    private void importPhonesData(final Customer pCustomer, final List<Phone> pPhones) {
        if (pPhones != null && !pPhones.isEmpty()) {
            for (final Phone lPhone : pPhones) {
                String lPhoneNumber = lPhone.getPhoneNumber();
                final String lType = lPhone.getPhoneType();
                if (StringUtils.hasText(lPhoneNumber) && StringUtils.hasText(lType)) {
                    lPhoneNumber = lPhoneNumber.replaceAll(Constants.PHONE_EXTRACT_REGEX, "");
                    if (Constants.LB.equalsIgnoreCase(lType)) {
                        pCustomer.setPhone(lPhoneNumber);
                    } else if (Constants.FX.equalsIgnoreCase(lType)) {
                        // TODO - set FAX
                        // pCustomer.setFax(lPhoneNumber);
                    }
                }
            }
        } else {
            mLogger.debug("pPhones is empty, resetting the phone and fax number property");
            pCustomer.setPhone(null);
            // TODO - set FAX
            // pCustomer.setFax(null);
        }
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

                if ("Y".equals(lAccountEcomStatus) && "Y".equals(lPrevAccountEcomStatus)) {
                    throw new ECMException(mResourceBundle.getString("duplicate.accountEcomStatus"));
                } else if ("Y".equals(lAccountEcomStatus)) {
                    pFilteredAccountsMap.put(lLocalCompNumber, pWiseAccount);
                }
            } else {
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
}
