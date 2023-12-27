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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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

    private final CustomerSubAccountRepository mCustomerSubAccountRepository;

    private final AddressRepository mAddressRepository;

    /**
     * This method process the subAccount data
     *
     * @param pAccount                     - the Account
     * @param pInActiveCustomerSubAccounts - InActive Customer SubAccount List
     * @param pCustomer                    - the Customer
     * @param pLocation                    - the Location
     * @return - List<String> (lSubAccountCustomerNumbers)
     */
    public List<String> processSubAccountsData(final Account pAccount, final List<String> pInActiveCustomerSubAccounts, final Customer pCustomer,
            final Location pLocation) {
        final List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        final List<CustomerSubAccount> lCustomerSubAccounts = new ArrayList<>();
        final List<Account.SubAccount> lSubAccounts = pAccount.getWiseSubAccounts();
        for (final SubAccount lSubAccount : lSubAccounts) {
            final String lSubAccountCustomerNumber = pAccount.getCompanyNumber() + "-" + lSubAccount.getSubAccountNumber();
            lSubAccountCustomerNumbers.add(lSubAccountCustomerNumber);
            mLogger.debug("Processing subAccount : {}", lSubAccountCustomerNumber);
            final CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
            lCustomerSubAccount.setCustomer(pCustomer);
            lCustomerSubAccount.setLocation(pLocation);
            lCustomerSubAccount.setAccountNumber(lSubAccount.getSubAccountNumber());
            lCustomerSubAccount.setSubAccountName(lSubAccount.getFullName());
            if (null != pInActiveCustomerSubAccounts && pInActiveCustomerSubAccounts.contains(lSubAccountCustomerNumber)) {
                lCustomerSubAccount.setStatusId((short) 0);
            } else {
                lCustomerSubAccount.setStatusId((short) 1);
            }

            final SubAccountDetail lSubAccountDetail = lSubAccount.getSubAccountDetail();
            if (lSubAccountDetail != null) {
                if (StringUtils.hasText(lSubAccountDetail.getFreightPercent())) {
                    lCustomerSubAccount.setFreightPercent(new BigDecimal(lSubAccountDetail.getFreightPercent()));
                }
                if (StringUtils.hasText(lSubAccountDetail.getFreightCost())) {
                    lCustomerSubAccount.setFreightCost(new BigDecimal(lSubAccountDetail.getFreightCost()));
                }

                lCustomerSubAccount.setPoRequiredCode(lSubAccountDetail.getPoReqCode());
                lCustomerSubAccount.setCreditStatusCode(lSubAccountDetail.getCreditStatusCode());
            }

            setSubAccountAddress(lCustomerSubAccount, lSubAccount.getSubAccountAddresses());
            lCustomerSubAccounts.add(lCustomerSubAccount);
        }
        mCustomerSubAccountRepository.saveAll(lCustomerSubAccounts);
        return lSubAccountCustomerNumbers;
    }

    /**
     * This method will update the subAccount address
     *
     * @param pCustomerSubAccount  - the Customer SubAccount
     * @param pSubAccountAddresses - SubAccount Addresses
     */
    private void setSubAccountAddress(final CustomerSubAccount pCustomerSubAccount,
            final List<com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AddressVO> pSubAccountAddresses) {
        Address lAddress = new Address();

        for (final com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AddressVO lSubAccountAddress : pSubAccountAddresses) {
            if (StringUtils.hasText(lSubAccountAddress.getType()) && Constants.SHIP_TO.equalsIgnoreCase(lSubAccountAddress.getType())) {
                lAddress.setAddress1(lSubAccountAddress.getAddressLine1());
                if (StringUtils.hasText(lSubAccountAddress.getAddressLine2())) {
                    lAddress.setAddress2(lSubAccountAddress.getAddressLine2());
                }
                lAddress.setCity(lSubAccountAddress.getCity());
                lAddress.setState(lSubAccountAddress.getState());
                lAddress.setPostalCode(lSubAccountAddress.getPostalCode());
                break;
            }
        }
        lAddress = mAddressRepository.save(lAddress);
        pCustomerSubAccount.setCustomerAddress(lAddress);
    }
}
