package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Address;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Address data processor
 *
 * @author Ankit Jain
 *
 */
@Component
@RequiredArgsConstructor
public class AddressProcessor {

    private final AddressRepository mAddressRepository;

    /**
     * This method imports the addresses data
     *
     * @param pCustomer  - the Customer
     * @param pAddresses - the Addresses
     */
    @Transactional
    public void importAddressesData(final Customer pCustomer, final List<Address> pAddresses) {
        if (pAddresses != null && !pAddresses.isEmpty()) {
            final boolean lIsShipAddrSet = setShippingAddress(pCustomer, pAddresses);
            setBillingAddress(pCustomer, pAddresses, lIsShipAddrSet);
        }
    }

    /**
     * <b>setShippingAddr</b> - Set Shipping Address.
     *
     * @param pCustomer
     * @param pAddressList
     * @return
     */
    private boolean setShippingAddress(final Customer pCustomer, List<Address> pAddressList) {
        boolean lIsShipAddrSet = false;
        for (final Address lAddressVO : pAddressList) {
            if (StringUtils.hasText(lAddressVO.getType()) && Constants.SHIP_TO.equalsIgnoreCase(lAddressVO.getType())) {
                com.winsupply.mdmcustomertoecomsubscriber.entities.Address lShippingAddress = pCustomer.getDefaultShippingAddress();
                if (null == lShippingAddress) {
                    lShippingAddress = new com.winsupply.mdmcustomertoecomsubscriber.entities.Address();
                }
                lShippingAddress = populateAddressObject(lShippingAddress, lAddressVO, pCustomer.getCustomerName());
                pCustomer.setDefaultShippingAddress(lShippingAddress);
                lIsShipAddrSet = true;
                break;
            }
        }
        return lIsShipAddrSet;
    }

    /**
     * <b>setBillingAddr</b> - Set Billing Address.
     *
     * @param pCustomer - the Customer
     * @param pAddressList - the Address List
     * @param pIsShipAddressSet - the Is Shipping Address Set
     */
    private void setBillingAddress(final Customer pCustomer, List<Address> pAddressList, final boolean pIsShipAddressSet) {
        for (final Address lAddress : pAddressList) {
            if (StringUtils.hasText(lAddress.getType()) && Constants.BILL_TO.equalsIgnoreCase(lAddress.getType())) {
                com.winsupply.mdmcustomertoecomsubscriber.entities.Address lBillingAddress = pCustomer.getDefaultBillingAddress();
                if (null == lBillingAddress) {
                    lBillingAddress = new com.winsupply.mdmcustomertoecomsubscriber.entities.Address();
                }
                lBillingAddress = populateAddressObject(lBillingAddress, lAddress, pCustomer.getCustomerName());
                pCustomer.setDefaultBillingAddress(lBillingAddress);

                if (!pIsShipAddressSet) {
                    pCustomer.setDefaultShippingAddress(lBillingAddress);
                }
                break;
            }
        }
    }

    /**
     * This method is used to create the ATG Address from the Address vo from the
     * json feed file.
     *
     * @param pAddressEntity - the Address
     * @param pAddress - the address
     * @param pCustomerName - the Customer Name
     * @return lNewAddressObject
     */
    private com.winsupply.mdmcustomertoecomsubscriber.entities.Address populateAddressObject(
            com.winsupply.mdmcustomertoecomsubscriber.entities.Address pAddressEntity, Address pAddress, String pCustomerName) {
        pAddressEntity.setAddress1(pAddress.getAddressLine1());
        if (StringUtils.hasText(pAddress.getAddressLine2())) {
            pAddressEntity.setAddress2(pAddress.getAddressLine2());
        }
        pAddressEntity.setCity(pAddress.getCity());
        pAddressEntity.setState(pAddress.getState());
        pAddressEntity.setPostalCode(pAddress.getPostalCode());
        pAddressEntity.setCompanyName(pCustomerName);
        return mAddressRepository.save(pAddressEntity);
    }
}
