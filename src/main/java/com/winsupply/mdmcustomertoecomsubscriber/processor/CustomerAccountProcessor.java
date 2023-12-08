package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccountNumber;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerLocation;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerSubAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountId;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountNumberId;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerLocationId;
import com.winsupply.mdmcustomertoecomsubscriber.exception.ECMException;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerAccountNumberRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerLocationRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerSubAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.LocationRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Account data processor
 *
 * @author Ankit Jain
 *
 */
@Component
@RequiredArgsConstructor
public class CustomerAccountProcessor {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final CustomerAccountRepository mCustomerAccountRepository;

    private final CustomerSubAccountRepository mCustomerSubAccountRepository;

    private final CustomerLocationRepository mCustomerLocationRepository;

    private final CustomerAccountNumberRepository mCustomerAccountNumberRepository;

    private final LocationRepository mLocationRepository;

    private final AddressRepository mAddressRepository;

    private final CustomerSubAccountProcessor mCustomerSubAccountProcessor;

    /**
     * <b>importCustomerAccountsData</b> - Import customer WISE accounts data
     *
     * @param pCustomer           - the Customer record
     * @param pInterCompanyId     - the InterCompanyId
     * @param pFilteredAccountMap - the FilteredAccount Map
     * @throws ECMException - the ECMException
     */
    public void importCustomerAccountsData(final Customer pCustomer, final String pInterCompanyId, final Map<String, Account> pFilteredAccountMap)
            throws ECMException {
        final List<CustomerAccount> lCustomerAccounts = new ArrayList<>();
        final String lCustomerECMId = pCustomer.getCustomerECMId();
        final boolean lIsInterCompanyMessage = StringUtils.hasText(pInterCompanyId) ? Boolean.TRUE : Boolean.FALSE;

        final List<String> lInActiveCustomerSubAccounts = getInactiveSubAccounts(lCustomerECMId);

        resetCustomerAccountsData(lCustomerECMId);

        if (null != pFilteredAccountMap && !pFilteredAccountMap.isEmpty()) {
            final Location lWinDefaultCompany = pCustomer.getWinDefaultCompany();
            final Set<String> lAccountNumbers = new HashSet<>();
            final List<String> lCompanyNumbers = new ArrayList<>();
            int lIndex = 0;
            Location lDefaultLC = null;

            for (final Map.Entry<String, Account> lFilteredAccount : pFilteredAccountMap.entrySet()) {
                final Account lAccount = lFilteredAccount.getValue();
                final String lCompanyNumber = lAccount.getCompanyNumber();

                final Optional<Location> lLocation = mLocationRepository.findById(lCompanyNumber);
                if (lLocation.isEmpty()) {
                    mLogger.warn("customerECMId - {} :: LC {} not found", lCustomerECMId, lCompanyNumber);
                    continue;
                }
                lAccountNumbers.add(lAccount.getAccountNumber());
                lCompanyNumbers.add(lCompanyNumber);

                // Setting the default LC
                mLogger.debug("lIsInterCompanyMessage: {}, pInterCompanyId : {}, Index : {}", lIsInterCompanyMessage, pInterCompanyId, lIndex);
                if ((!lIsInterCompanyMessage && lIndex == 0)
                        || (lIsInterCompanyMessage && pInterCompanyId.equalsIgnoreCase(lCompanyNumber))) {
                    lDefaultLC = lLocation.get();
                }

                final List<CustomerAccount> lCustomerAccountAttributes = createCustomerAccountAttributes(lCustomerECMId, lAccount);
                if (!lCustomerAccountAttributes.isEmpty()) {
                    lCustomerAccounts.addAll(lCustomerAccountAttributes);
                }

                mCustomerSubAccountProcessor.processSubAccountsData(lAccount, lInActiveCustomerSubAccounts, pCustomer);
                lIndex++;
            }
            if (lWinDefaultCompany == null || !lCompanyNumbers.contains(lWinDefaultCompany.getCompanyNumber())) {
                pCustomer.setWinDefaultCompany(lDefaultLC);
            }
            if (!lCustomerAccounts.isEmpty()) {
                mCustomerAccountRepository.saveAll(lCustomerAccounts);
            }

            setCustomerAccountNumbers(lAccountNumbers, lCustomerECMId);
            setCustomerLocations(lCompanyNumbers, lCustomerECMId);

            // Checking for default LC
            if (pCustomer.getWinDefaultCompany() == null) {
                throw new ECMException("Default Local Company not set for the Customer - " + lCustomerECMId);
            }
        } else {
            mLogger.info("Reset Customer WISE account data since accounts are null or empty :: lCustomerEcmId {}", lCustomerECMId);
        }
    }

    /**
     * <b>setCustomerAccountNumbers</b> - it sets the customer account numbers
     *
     * @param pAccountNumbers - the Account Numbers
     * @param pCustomerECMId - the Customer ECM Id
     */
    private void setCustomerAccountNumbers(final Set<String> pAccountNumbers, final String pCustomerECMId) {
        if (!pAccountNumbers.isEmpty()) {
            final List<CustomerAccountNumber> lCustomerAccountNumbers = pAccountNumbers.stream()
                    .map(lAccountNumber -> createCustomerAccountNumber(pCustomerECMId, lAccountNumber)).toList();
            mCustomerAccountNumberRepository.saveAll(lCustomerAccountNumbers);
        }
    }

    /**
     * <b>setCustomerLocations</b> - it sets the Customer Locations
     *
     * @param pCompanyNumbers - the Company Numbers
     * @param pCustomerECMId  - the Customer ECM Id
     */
    private void setCustomerLocations(final List<String> pCompanyNumbers, final String pCustomerECMId) {
        if (!pCompanyNumbers.isEmpty()) {
            final List<CustomerLocation> lCustomerLocations = pCompanyNumbers.stream()
                    .map(lCompanyNumber -> createCustomerLocation(pCustomerECMId, lCompanyNumber)).toList();
            mCustomerLocationRepository.saveAll(lCustomerLocations);
        }
    }

    /**
     * <b>getInactiveSubAccounts</b> - It returns the Inactive subAccounts of customer
     *
     * @param lCustomerECMId - the Customer ECM Id
     * @return - List<String>
     */
    private List<String> getInactiveSubAccounts(final String lCustomerECMId) {
        final Short lInActive = 0;
        final List<CustomerSubAccount> lInActiveCustomerSubAccounts = mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(
                lCustomerECMId, lInActive);

        List<String> lInActiveCustomerSubAccountList = null;
        if (null != lInActiveCustomerSubAccounts && !lInActiveCustomerSubAccounts.isEmpty()) {
            lInActiveCustomerSubAccountList = lInActiveCustomerSubAccounts.stream()
                    .map(lInActiveCustomerSubAccount -> lInActiveCustomerSubAccount.getLocation().getCompanyNumber() + "-"
                            + lInActiveCustomerSubAccount.getAccountNumber()).toList();
        }

        mLogger.debug("lInActiveCustomerSubAccountList : {}", lInActiveCustomerSubAccountList);
        return lInActiveCustomerSubAccountList;
    }

    /**
     * <b>resetCustomerData</b> - It resets the customer data
     *
     * @param pCustomerECMId - the CustomerECMId
     */
    public void resetCustomerAccountsData(final String pCustomerECMId) {
        mCustomerAccountRepository.deleteAllByCustomerECMId(pCustomerECMId);
        deleteSubAccountAndAddress(pCustomerECMId);
        mCustomerLocationRepository.deleteAllByCustomerECMId(pCustomerECMId);
        mCustomerAccountNumberRepository.deleteAllByCustomerECMId(pCustomerECMId);
    }

    /**
     * <b>deleteSubAccountAndAddress</b> - It deletes the customer Sub Accounts and
     * addresses
     *
     * @param pCustomerECMId - the Customer ECM Id
     */
    private void deleteSubAccountAndAddress(final String pCustomerECMId) {
        List<CustomerSubAccount> lSubAccounts = mCustomerSubAccountRepository.findByCustomerCustomerECMId(pCustomerECMId);
        if (!CollectionUtils.isEmpty(lSubAccounts)) {
            List<Long> lAddressIds = lSubAccounts.stream().map(CustomerSubAccount::getCustomerAddress).map(Address::getId).toList();
            mCustomerSubAccountRepository.deleteAllByCustomerECMId(pCustomerECMId);
            mAddressRepository.deleteAllById(lAddressIds);
        }
    }

    /**
     * <b>deleteCustomerAddress</b> - It deletes the customer shipping or billing
     * address
     *
     * @param pCustomer - the Customer
     */
    public void deleteCustomerAddress(final Customer pCustomer) {
        Address lDefaultBillingAddress = pCustomer.getDefaultBillingAddress();
        if (null != lDefaultBillingAddress) {
            pCustomer.setDefaultBillingAddress(null);
            mAddressRepository.deleteById(lDefaultBillingAddress.getId());
        }

        Address lDefaultShippingAddress = pCustomer.getDefaultShippingAddress();
        if (null != lDefaultShippingAddress) {
            pCustomer.setDefaultShippingAddress(null);
            mAddressRepository.deleteById(lDefaultShippingAddress.getId());
        }
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
