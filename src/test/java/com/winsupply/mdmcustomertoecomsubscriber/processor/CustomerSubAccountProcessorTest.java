package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.common.utils.Utils;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerSubAccountRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerSubAccountProcessorTest {

    @InjectMocks
    private CustomerSubAccountProcessor mCustomerSubAccountProcessor;

    @Mock
    private CustomerSubAccountRepository mCustomerSubAccountRepository;

    @Mock
    private AddressRepository mAddressRepository;

    @Test
    void testProcessSubAccountsData_SetSubAccAddress() throws IOException {
        String lListenerMessege = Utils.readFile("customerPayloadProcessSubAccData.json");
        lListenerMessege = lListenerMessege.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"W 12TH WC \"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Account lAccount = lCustomerMessageVO.getWiseAccounts().get(0);

        List<String> pInActiveCustomerSubAccounts = new ArrayList<>();
        pInActiveCustomerSubAccounts.add(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber() + "-"
                + lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountNumber());

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());

        Address lAddress = new Address();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddress);
        mCustomerSubAccountProcessor.processSubAccountsData(lAccount, pInActiveCustomerSubAccounts, lCustomer, lLocation);
        verify(mAddressRepository, times(1)).save(any(Address.class));
        verify(mCustomerSubAccountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessSubAccountsData_SetSubAccAddress_WithEmpty_FreightPercent_FreightCost_AndEmptyAddressLine2InSetSubAccountAddress()
            throws IOException {
        String lListenerMessege = Utils.readFile("customerPayloadProcessSubAccData.json");
        lListenerMessege = lListenerMessege.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"\"");
        lListenerMessege = lListenerMessege.replace("\"freightPercent\": \"0.00\"", "\"freightPercent\": \"\"");
        lListenerMessege = lListenerMessege.replace("\"freightCost\": \"0.00\"", "\"freightCost\": \"\"");
        lListenerMessege = lListenerMessege.replace("\"type\": \"Ship to\"", "\"type\": \"Ship to\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Account lAccount = lCustomerMessageVO.getWiseAccounts().get(0);

        List<String> pInActiveCustomerSubAccounts = new ArrayList<>();
        pInActiveCustomerSubAccounts.add("");

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());

        Address lAddress = new Address();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddress);
        mCustomerSubAccountProcessor.processSubAccountsData(lAccount, pInActiveCustomerSubAccounts, lCustomer, lLocation);
        verify(mAddressRepository, times(1)).save(any(Address.class));
        verify(mCustomerSubAccountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessSubAccountsData_SetSubAccAddress_WithTypeEmptyFieldIn_InSubAccountAddresses() throws IOException {
        String lListenerMessege = Utils.readFile("payLoad.json");
        lListenerMessege = lListenerMessege.replace("\"type\": \"Ship to\"", "\"type\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Account lAccount = lCustomerMessageVO.getWiseAccounts().get(0);

        List<String> pInActiveCustomerSubAccounts = null;

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());

        Address lAddress = new Address();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddress);
        mCustomerSubAccountProcessor.processSubAccountsData(lAccount, pInActiveCustomerSubAccounts, lCustomer, lLocation);
        verify(mCustomerSubAccountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessSubAccountsData_SetSubAccAddress_WithTypeBillTo_InSubAccountAddresses() throws IOException {
        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"type\": \"Ship to\"", "\"type\": \"Bill to\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Account lAccount = lCustomerMessageVO.getWiseAccounts().get(0);

        List<String> pInActiveCustomerSubAccounts = new ArrayList<>();
        pInActiveCustomerSubAccounts.add("");

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());

        Address lAddress = new Address();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddress);
        mCustomerSubAccountProcessor.processSubAccountsData(lAccount, pInActiveCustomerSubAccounts, lCustomer, lLocation);
        verify(mCustomerSubAccountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessSubAccountsData_SetSubAccAddress_WithNullWiseSubAccount() throws IOException {
        String lListenerMessege = Utils.readFile("customerPayloadwithWiseSubAccountAsNull.json");
        lListenerMessege = lListenerMessege.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"W 12TH WC \"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Account lAccount = lCustomerMessageVO.getWiseAccounts().get(0);

        List<String> pInActiveCustomerSubAccounts = new ArrayList<>();
        pInActiveCustomerSubAccounts.add("");

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());

        Address lAddress = new Address();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddress);
        mCustomerSubAccountProcessor.processSubAccountsData(lAccount, pInActiveCustomerSubAccounts, lCustomer, lLocation);
        verify(mCustomerSubAccountRepository, times(1)).saveAll(anyList());
    }

}
