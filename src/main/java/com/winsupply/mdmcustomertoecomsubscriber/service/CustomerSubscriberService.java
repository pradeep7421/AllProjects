package com.winsupply.mdmcustomertoecomsubscriber.service;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccountNumber;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerLocation;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerResupply;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerSubAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountId;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountNumberId;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerLocationId;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerResupplyId;
import com.winsupply.mdmcustomertoecomsubscriber.exception.ECMException;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Address;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Email;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.FederalId;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Phone;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerAccountNumberRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerLocationRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerResupplyRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerSubAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.LocationRepository;
import jakarta.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
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

    private final LocationRepository mLocationRepository;

    private final CustomerAccountRepository mCustomerAccountRepository;

    private final CustomerSubAccountRepository mCustomerSubAccountRepository;

    private final CustomerLocationRepository mCustomerLocationRepository;

    private final CustomerAccountNumberRepository mCustomerAccountNumberRepository;

    private final CustomerResupplyRepository mCustomerResupplyRepository;

    /**
     * Process the Quotes Message
     *
     * @param pPayload        - the Payload
     * @param pMessageHeaders - the Message Headers
     * @throws JAXBException - the JAXBException
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
            if (lActionCode != null && "delete".equalsIgnoreCase(lActionCode)) {
                // TODO deleteCustomer(lCustomerEcmId);
            } else {
                createOrUpdateCustomer(lCustomerMessageVO);
            }

        } catch (final Exception lException) {
            mLogger.error("Exception -> ", lException);
        }
    }

    /**
     * <b>createOrUpdateCustomer</b> - Create Or Update customer
     *
     * @param pCustomerMessageVO - the CustomerMessage VO
     * @throws ECMException - the ECMException
     */
    private void createOrUpdateCustomer(final CustomerMessageVO pCustomerMessageVO) throws ECMException {
        final String lCustomerECMId = pCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = null;
        Optional<Customer> lCustomerDBRecord = mCustomerRepository.findById(lCustomerECMId);

        if (lCustomerDBRecord.isEmpty()) {
            lCustomer = new Customer();
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

        importCustomerAccountsData(lCustomer, lInterCompanyId, lFilteredAccounts);

        importPhonesData(lCustomer, pCustomerMessageVO.getPhones());

        importElectronicAddressesData(lCustomer, pCustomerMessageVO.getEmails());

        importAddressesData(lCustomer, pCustomerMessageVO.getAddresses());

        mCustomerRepository.save(lCustomer);
    }

    /**
     * <b>importCustomerAccountsData</b> - Import customer WISE accounts data
     *
     * @param pCustomer           - the Customer record
     * @param pInterCompanyId     - the InterCompanyId
     * @param pFilteredAccountMap - the FilteredAccount Map
     * @throws ECMException - the ECMException
     */
    private void importCustomerAccountsData(final Customer pCustomer, final String pInterCompanyId, final Map<String, Account> pFilteredAccountMap)
            throws ECMException {
        final List<CustomerAccount> lCustomerAccounts = new ArrayList<>();
        final String lCustomerECMId = pCustomer.getCustomerECMId();
        final boolean lIsInterCompanyCustomerMessage = StringUtils.hasText(pInterCompanyId) ? Boolean.TRUE : Boolean.FALSE;

        final Short lInActive = 0;
        final List<CustomerSubAccount> lInActiveCustomerSubAccounts = mCustomerSubAccountRepository
                .findByCustomerCustomerECMIdAndStatusId(lCustomerECMId, lInActive);
        final List<String> lInActiveCustomerSubAccountList = lInActiveCustomerSubAccounts.stream().map(
                lInActiveCustomerSubAccount -> lInActiveCustomerSubAccount.getCompanyNumber() + "-" + lInActiveCustomerSubAccount.getAccountNumber())
                .toList();

        mCustomerAccountRepository.deleteAllByCustomerECMId(lCustomerECMId);
        mCustomerSubAccountRepository.deleteAllByCustomerECMId(lCustomerECMId);
        mCustomerLocationRepository.deleteAllByCustomerECMId(lCustomerECMId);
        mCustomerAccountNumberRepository.deleteAllByCustomerECMId(lCustomerECMId);

        if (pFilteredAccountMap.size() > 0) {
            final Location lWinDefaultCompany = pCustomer.getWinDefaultCompany();
            final Set<String> lAccountNumbers = new HashSet<>();
            final List<String> lCompanyNumbers = new ArrayList<>();
            int lIndex = 0;
            Location lDefaultLC = null;

            for (final Map.Entry<String, Account> lEntry : pFilteredAccountMap.entrySet()) {
                final Account lAccount = lEntry.getValue();
                final String lCompanyNumber = lAccount.getCompanyNumber();

                final Optional<Location> lLocation = mLocationRepository.findById(lCompanyNumber);
                if (lLocation.isEmpty()) {
                    mLogger.warn("customerECMId - {} :: LC {} not found", lCustomerECMId, lCompanyNumber);
                    continue;
                }
                lAccountNumbers.add(lAccount.getAccountNumber());
                lCompanyNumbers.add(lCompanyNumber);

                // Setting the default LC
                if ((!lIsInterCompanyCustomerMessage && lIndex == 0)
                        || (lIsInterCompanyCustomerMessage && pInterCompanyId.equalsIgnoreCase(lCompanyNumber))) {
                    lDefaultLC = lLocation.get();
                }

                final List<CustomerAccount> lCustomerAccountAttributes = createCustomerAccountAttributes(lCustomerECMId, lAccount);
                if (!lCustomerAccountAttributes.isEmpty()) {
                    lCustomerAccounts.addAll(lCustomerAccountAttributes);
                }
                // TODO - Process Sub Account using lInActiveCustomerSubAccountList
                lIndex++;
            }
            if (lWinDefaultCompany == null || !lCompanyNumbers.contains(lWinDefaultCompany.getCompanyNumber())) {
                pCustomer.setWinDefaultCompany(lDefaultLC);
            }
            if (!lCustomerAccounts.isEmpty()) {
                mCustomerAccountRepository.saveAll(lCustomerAccounts);
            }
            if (!lAccountNumbers.isEmpty()) {
                final List<CustomerAccountNumber> lCustomerAccountNumbers = lAccountNumbers.stream()
                        .map(lAccountNumber -> createCustomerAccountNumber(lCustomerECMId, lAccountNumber)).toList();
                mCustomerAccountNumberRepository.saveAll(lCustomerAccountNumbers);
            }
            if (!lCompanyNumbers.isEmpty()) {
                final List<CustomerLocation> lCustomerLocations = lCompanyNumbers.stream()
                        .map(lCompanyNumber -> createCustomerLocation(lCustomerECMId, lCompanyNumber)).toList();
                mCustomerLocationRepository.saveAll(lCustomerLocations);
            }

            // Checking for default LC
            if (pCustomer.getWinDefaultCompany() == null) {
                throw new ECMException("Default Local Company not set for the Customer - " + lCustomerECMId);
            }
        } else {
            mLogger.info("Reset Customer WISE account data since accounts are null or empty :: lCustomerEcmId {}", lCustomerECMId);
        }
    }

    /**
     *
     * @param pCustomer
     * @param pAddresses
     */
    private void importAddressesData(final Customer pCustomer, final List<Address> pAddresses) {
        // TODO Auto-generated method stub

    }

    /**
     *
     * @param pCustomer
     * @param pEmails
     */
    private void importElectronicAddressesData(final Customer pCustomer, final List<Email> pEmails) {
        // TODO Auto-generated method stub

    }

    /**
     *
     * @param pCustomer
     * @param pPhones
     */
    private void importPhonesData(final Customer pCustomer, final List<Phone> pPhones) {
        // TODO Auto-generated method stub

    }

    /**
     * <b>createCustomerAccountAttributes</b> - This method creates the
     * CustomerAccount attributes
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @param pAccount       - the WISE Account
     * @return List<CustomerAccount>
     */
    private List<CustomerAccount> createCustomerAccountAttributes(final String pCustomerECMId, final Account pAccount) {
        final List<CustomerAccount> lCustomerAccountAttributes = new ArrayList<>();

        if (StringUtils.hasText(pAccount.getAccountNumber())) {
            final CustomerAccountId lWiseCustomerNumberId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("wiseCustomerNumber").build();
            final CustomerAccount lWiseCustomerNumberEntry = CustomerAccount.builder().id(lWiseCustomerNumberId)
                    .attributeValue(pAccount.getAccountNumber()).build();
            lCustomerAccountAttributes.add(lWiseCustomerNumberEntry);
        }
        if (StringUtils.hasText(pAccount.getInterCompanyCode())) {
            final CustomerAccountId lInterCompanyCodeId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("interCompanyCode").build();
            final CustomerAccount lInterCompanyCodeEntry = CustomerAccount.builder().id(lInterCompanyCodeId)
                    .attributeValue(pAccount.getInterCompanyCode()).build();
            lCustomerAccountAttributes.add(lInterCompanyCodeEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountEcommerceStatus())) {
            final CustomerAccountId lAccEcomStatusId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("accEcomStatus").build();
            final CustomerAccount lAccEcomStatusEntry = CustomerAccount.builder().id(lAccEcomStatusId)
                    .attributeValue(pAccount.getAccountEcommerceStatus()).build();
            lCustomerAccountAttributes.add(lAccEcomStatusEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getFreightPercent())) {
            final CustomerAccountId lFreightPercentId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("freightPercent").build();
            final CustomerAccount lFreightPercentEntry = CustomerAccount.builder().id(lFreightPercentId)
                    .attributeValue(pAccount.getAccountDetail().getFreightPercent()).build();
            lCustomerAccountAttributes.add(lFreightPercentEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getFreightCost())) {
            final CustomerAccountId lFreightCostId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("freightCost").build();
            final CustomerAccount lFreightCostEntry = CustomerAccount.builder().id(lFreightCostId)
                    .attributeValue(pAccount.getAccountDetail().getFreightCost()).build();
            lCustomerAccountAttributes.add(lFreightCostEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getProRewardsId())) {
            final CustomerAccountId lProRewardsId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("proRewardsID").build();
            final CustomerAccount lProRewardsEntry = CustomerAccount.builder().id(lProRewardsId)
                    .attributeValue(pAccount.getAccountDetail().getProRewardsId()).build();
            lCustomerAccountAttributes.add(lProRewardsEntry);

        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getPoReqCode())) {
            final CustomerAccountId lPoReqCodeId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("poReqCode").build();
            final CustomerAccount lPoReqCodeEntry = CustomerAccount.builder().id(lPoReqCodeId)
                    .attributeValue(pAccount.getAccountDetail().getPoReqCode()).build();
            lCustomerAccountAttributes.add(lPoReqCodeEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getBillToAccount())) {
            final CustomerAccountId lBillToAccountId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("billToAccount").build();
            final CustomerAccount lBillToAccountEntry = CustomerAccount.builder().id(lBillToAccountId)
                    .attributeValue(pAccount.getAccountDetail().getBillToAccount()).build();
            lCustomerAccountAttributes.add(lBillToAccountEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getCreditStatusCode())) {
            final CustomerAccountId lCreditStatusCodeId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("creditStatusCode").build();
            final CustomerAccount lCreditStatusCodeEntry = CustomerAccount.builder().id(lCreditStatusCodeId)
                    .attributeValue(pAccount.getAccountDetail().getCreditStatusCode()).build();
            lCustomerAccountAttributes.add(lCreditStatusCodeEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getCreditLimit())) {
            final CustomerAccountId lCreditLimitId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber())
                    .customerECMId(pCustomerECMId).attributeName("creditLimit").build();
            final CustomerAccount lCreditLimitEntry = CustomerAccount.builder().id(lCreditLimitId)
                    .attributeValue(pAccount.getAccountDetail().getCreditLimit()).build();
            lCustomerAccountAttributes.add(lCreditLimitEntry);
        }
        if (StringUtils.hasText(pAccount.getAccountDetail().getCashSale())) {
            final CustomerAccountId lCashSaleId = CustomerAccountId.builder().companyNumber(pAccount.getCompanyNumber()).customerECMId(pCustomerECMId)
                    .attributeName("cashSale").build();
            final CustomerAccount lCashSaleEntry = CustomerAccount.builder().id(lCashSaleId).attributeValue(pAccount.getAccountDetail().getCashSale())
                    .build();
            lCustomerAccountAttributes.add(lCashSaleEntry);
        }
        return lCustomerAccountAttributes;
    }

    /**
     * <b>validateAndFilterAccounts</b> - Validates and filters WISE accounts
     *
     * @param pWiseAccounts - the WISE Accounts
     * @throws ECMException - the ECMException
     * @return Map<String, Account>
     */
    private Map<String, Account> validateAndFilterAccounts(final List<Account> pWiseAccounts) throws ECMException {
        final Map<String, Account> lFilteredAccountsMap = new LinkedHashMap<>(pWiseAccounts.size());
        if (pWiseAccounts != null && !pWiseAccounts.isEmpty()) {
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

    /**
     * <b>createCustomerAccountNumber</b> - Create customer accountNumber
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @param pAccountNumber - the Account Number
     * @return CustomerAccountNumber
     */
    private CustomerAccountNumber createCustomerAccountNumber(final String pCustomerECMId, final String pAccountNumber) {
        final CustomerAccountNumberId lCustomerAccountNumberId = CustomerAccountNumberId.builder().customerECMId(pCustomerECMId)
                .accountNumber(pAccountNumber).build();
        return CustomerAccountNumber.builder().id(lCustomerAccountNumberId).build();
    }

    /**
     * <b>createCustomerLocation</b> - Create customer location
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @param pCompanyNumber - the Company Number
     * @return CustomerLocation
     */
    private CustomerLocation createCustomerLocation(final String pCustomerECMId, final String pCompanyNumber) {
        final CustomerLocationId lCustomerLocationId = CustomerLocationId.builder().customerECMId(pCustomerECMId).companyNumber(pCompanyNumber)
                .build();
        return CustomerLocation.builder().id(lCustomerLocationId).build();
    }

}
