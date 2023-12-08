package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerSubAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account.SubAccount;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account.SubAccount.SubAccountDetail;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerSubAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.LocationRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Sub Account data processor
 *
 * @author Ankit Jain
 *
 */
@Component
@RequiredArgsConstructor
public class CustomerSubAccountProcessor {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final LocationRepository mLocationRepository;

    private final CustomerSubAccountRepository mCustomerSubAccountRepository;

    private final AddressRepository mAddressRepository;

    /**
     * This method process the subAccount data
     *
     * @param pAccount                     - the Account
     * @param pInActiveCustomerSubAccounts - InActive Customer SubAccount List
     * @param pCustomer                    - the Customer
     */
    @Transactional
    public void processSubAccountsData(final Account pAccount, List<String> pInActiveCustomerSubAccounts, Customer pCustomer) {
        List<CustomerSubAccount> lCustomerSubAccounts = new ArrayList<>();
        List<Account.SubAccount> lSubAccounts = pAccount.getWiseSubAccounts();
        for (final SubAccount lSubAccount : lSubAccounts) {
            final String lSubAccountId = pAccount.getCompanyNumber() + "-" + lSubAccount.getSubAccountNumber();
            mLogger.debug("processing subAccount with Id: {}", lSubAccountId);
            CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
            lCustomerSubAccount.setCustomer(pCustomer);
            final Optional<Location> lLocationOpt = mLocationRepository.findById(pAccount.getCompanyNumber());
            lLocationOpt.ifPresent(lCustomerSubAccount::setLocation);

            lCustomerSubAccount.setAccountNumber(lSubAccount.getSubAccountNumber());
            lCustomerSubAccount.setSubAccountName(lSubAccount.getFullName());
            if (null != pInActiveCustomerSubAccounts && pInActiveCustomerSubAccounts.contains(lSubAccountId)) {
                lCustomerSubAccount.setStatusId((short) 0);
            } else {
                lCustomerSubAccount.setStatusId((short) 1);
            }

            final SubAccountDetail lSubAccountDetail = lSubAccount.getSubAccountDetail();
            if (lSubAccountDetail != null) {
                if (StringUtils.hasText(lSubAccountDetail.getFreightPercent())) {
                    lCustomerSubAccount.setFreightPercent(new BigDecimal(lSubAccountDetail.getFreightPercent()));
                } else {
                    lCustomerSubAccount.setFreightPercent(null);
                }
                if (StringUtils.hasText(lSubAccountDetail.getFreightCost())) {
                    lCustomerSubAccount.setFreightCost(new BigDecimal(lSubAccountDetail.getFreightCost()));
                } else {
                    lCustomerSubAccount.setFreightCost(null);
                }

                lCustomerSubAccount.setPoRequiredCode(lSubAccountDetail.getPoReqCode());
                lCustomerSubAccount.setCreditStatusCode(lSubAccountDetail.getCreditStatusCode());
            }

            setSubAccountAddress(lCustomerSubAccount, lSubAccount.getSubAccountAddresses());
            lCustomerSubAccounts.add(lCustomerSubAccount);
        }
        mCustomerSubAccountRepository.saveAll(lCustomerSubAccounts);
    }

    /**
     * This method will update the subAccount address
     *
     * @param pCustomerSubAccount  - the Customer SubAccount
     * @param pSubAccountAddresses - SubAccount Addresses
     */
    private void setSubAccountAddress(CustomerSubAccount pCustomerSubAccount,
            List<com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Address> pSubAccountAddresses) {
        Address lAddress;
        if (null != pCustomerSubAccount.getCustomerAddress()) {
            lAddress = pCustomerSubAccount.getCustomerAddress();
        } else {
            lAddress = new Address();
        }

        for (final com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Address lSubAddress : pSubAccountAddresses) {
            if (StringUtils.hasText(lSubAddress.getType()) && Constants.SHIP_TO.equalsIgnoreCase(lSubAddress.getType())) {
                lAddress.setAddress1(lSubAddress.getAddressLine1());
                if (StringUtils.hasText(lSubAddress.getAddressLine2())) {
                    lAddress.setAddress2(lSubAddress.getAddressLine2());
                }
                lAddress.setCity(lSubAddress.getCity());
                lAddress.setState(lSubAddress.getState());
                lAddress.setPostalCode(lSubAddress.getPostalCode());
                break;
            }
        }
        lAddress = mAddressRepository.save(lAddress);
        pCustomerSubAccount.setCustomerAddress(lAddress);
    }
}
