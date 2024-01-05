package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerSubAccountRepository;
import com.winsupply.readfile.PayLoadReadFile;
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
    CustomerSubAccountProcessor mCustomerSubAccountProcessor;
    @Mock
    CustomerSubAccountRepository mCustomerSubAccountRepository;
    @Mock
    AddressRepository mAddressRepository;

    @Test
    void testProcessSubAccountsData() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"W 12TH WC \"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
        verify(mCustomerSubAccountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessSubAccountsData_WithEmptyAddressLine2() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"\"");
        lPayLoad = lPayLoad.replace("\"freightPercent\": \"0.00\"", "\"freightPercent\": \"\"");
        lPayLoad = lPayLoad.replace("\"freightCost\": \"0.00\"", "\"freightCost\": \"\"");
        lPayLoad = lPayLoad.replace("\"type\": \"Ship to\"", "\"type\": \"Ship to\"");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
    void testProcessSubAccountsData_WithEmptyGetType() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"\"");
        lPayLoad = lPayLoad.replace("\"freightPercent\": \"0.00\"", "\"freightPercent\": \"\"");
        lPayLoad = lPayLoad.replace("\"freightCost\": \"0.00\"", "\"freightCost\": \"\"");
        lPayLoad = lPayLoad.replace("\"type\": \"Ship to\"", "\"type\": \"\"");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
    void testProcessSubAccountsData_WithEmptyFields() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"W 12TH WC \"");
        lPayLoad = lPayLoad.replace("\"freightPercent\": \"0.00\"", "\"freightPercent\": \"\"");
        lPayLoad = lPayLoad.replace("\"freightCost\": \"0.00\"", "\"freightCost\": \"\"");
        lPayLoad = lPayLoad.replace("\"type\": \"Ship to\"", "\"type\": \"Not Ship\"");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
    void testProcessSubAccountsDataWithNullWiseSubAccount() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadwithWiseSubAccountAsNull.json");
        lPayLoad = lPayLoad.replace("\"addressLine2\": \"\"", "\"addressLine2\": \"W 12TH WC \"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
