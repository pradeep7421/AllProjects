package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccountNumber;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerLocation;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerSubAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import com.winsupply.mdmcustomertoecomsubscriber.exception.ECMException;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Account;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerAccountNumberRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerLocationRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerSubAccountRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.LocationRepository;
import com.winsupply.readfile.PayLoadReadFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerAccountProcessorTest {
    @InjectMocks
    CustomerAccountProcessor CustomerAccountProcessor;
    @Mock
    CustomerAccountRepository mCustomerAccountRepository;
    @Mock
    CustomerSubAccountRepository mCustomerSubAccountRepository;
    @Mock
    CustomerLocationRepository mCustomerLocationRepository;
    @Mock
    CustomerAccountNumberRepository mCustomerAccountNumberRepository;
    @Mock
    LocationRepository mLocationRepository;
    @Mock
    CustomerSubAccountProcessor mCustomerSubAccountProcessor;

    @Test
    void testImportWiseAccountsData() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerECMId);

        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();
        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        String lCompanyNumber = lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber();
        pFilteredAccountMap.put(lCompanyNumber, lCustomerMessageVO.getWiseAccounts().get(0));
        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
        lCustomerSubAccount.setAccountNumber(lCustomerMessageVO.getWiseAccounts().get(0).getAccountNumber());

        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(lLocation);
        lCustomerSubAccount.setLocation(lLocation);
        lInActiveCustomerSubAccounts.add(lCustomerSubAccount);
        List<CustomerAccount> lCustomerAccounts = new ArrayList<>();
        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();
        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();
        List<CustomerLocation> lCustomerLocations = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), anyList(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);
        when(mCustomerAccountRepository.saveAll(anyList())).thenReturn(lCustomerAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerAccountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportWiseAccountsData_withEmptyOptionalLocation() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lCompanyNumber = lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber();

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerECMId);

        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();
        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        pFilteredAccountMap.put(lCompanyNumber, lCustomerMessageVO.getWiseAccounts().get(0));

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
        lCustomerSubAccount.setAccountNumber(lCustomerMessageVO.getWiseAccounts().get(0).getAccountNumber());
        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(lLocation);
        lCustomerSubAccount.setLocation(lLocation);
        lInActiveCustomerSubAccounts.add(lCustomerSubAccount);

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.empty());
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        assertThrows(ECMException.class, () -> CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap));
        verify(mLocationRepository, times(1)).findById(anyString());

    }

    @Test
    void testImportWiseAccountsData_withBlankInterCompanyId() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithMultipleAccountDetailsFields.json");
        lPayLoad = lPayLoad.replace("\"interCompanyId\": \"00066\"", "\"interCompanyId\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Customer lCustomer = new Customer();
        Location lLocation = null;

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        for (Account lAccount : lCustomerMessageVO.getWiseAccounts()) {
            String lCompanyNumber = lAccount.getCompanyNumber();
            pFilteredAccountMap.put(lCompanyNumber, lAccount);

            if (lLocation == null) {
                lLocation = new Location();
                lLocation.setCompanyNumber(lAccount.getCompanyNumber());
                lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
                lCustomer.setWinDefaultCompany(lLocation);
            }
        }

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
        lCustomerSubAccount.setAccountNumber(lCustomerMessageVO.getWiseAccounts().get(0).getAccountNumber());
        lCustomerSubAccount.setLocation(lLocation);
        lInActiveCustomerSubAccounts.add(lCustomerSubAccount);

        List<CustomerLocation> lCustomerLocations = new ArrayList<>();
        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();
        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(1).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), anyList(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerLocationRepository, times(1)).saveAll(anyList());
        verify(mLocationRepository, times(2)).findById(anyString());
    }

    @Test
    void testImportWiseAccountsData_withMultipleAccountDetailsFields() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithMultipleAccountDetailsFields.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();
        Customer lCustomer = new Customer();
        Location lLocation = null;

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        for (Account lAccount : lCustomerMessageVO.getWiseAccounts()) {
            String lCompanyNumber = lAccount.getCompanyNumber();
            pFilteredAccountMap.put(lCompanyNumber, lAccount);

            if (lLocation == null) {
                lLocation = new Location();
                lLocation.setCompanyNumber(lAccount.getCompanyNumber());
                lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
                lCustomer.setWinDefaultCompany(lLocation);
            }
        }

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
        lCustomerSubAccount.setAccountNumber(lCustomerMessageVO.getWiseAccounts().get(0).getAccountNumber());
        lCustomerSubAccount.setLocation(lLocation);
        lInActiveCustomerSubAccounts.add(lCustomerSubAccount);

        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();

        List<CustomerLocation> lCustomerLocations = new ArrayList<>();

        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(1).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), anyList(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerAccountNumberRepository, times(1)).saveAll(anyList());
        verify(mCustomerLocationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportWiseAccountsData_withNullInActiveCustomerSubAccounts() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithAccountDetailsFieldsAndBlankfield.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Customer lCustomer = new Customer();
        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(null);
        lCustomer.setCustomerECMId(lCustomerECMId);

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        String lCompanyNumber = lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber();
        pFilteredAccountMap.put(lCompanyNumber, lCustomerMessageVO.getWiseAccounts().get(0));

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = null;
        List<CustomerAccount> lCustomerAccounts = new ArrayList<>();
        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();
        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();
        List<CustomerLocation> lCustomerLocations = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), any(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);
        when(mCustomerAccountRepository.saveAll(anyList())).thenReturn(lCustomerAccounts);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        assertThrows(ECMException.class, () -> CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap));
        verify(mCustomerSubAccountProcessor, times(1)).processSubAccountsData(any(Account.class), any(), any(Customer.class), any(Location.class));
    }

    @Test
    void testImportWiseAccountsData_withEmptyInActiveCustomerSubAccounts() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithAccountDetailsFieldsAndBlankfield.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();
        String lCompanyNumber = lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber();

        Customer lCustomer = new Customer();
        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(null);
        lCustomer.setCustomerECMId(lCustomerECMId);

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        pFilteredAccountMap.put(lCompanyNumber, lCustomerMessageVO.getWiseAccounts().get(0));

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();
        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();
        List<CustomerLocation> lCustomerLocations = new ArrayList<>();
        List<CustomerAccount> lCustomerAccounts = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), any(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);
        when(mCustomerAccountRepository.saveAll(anyList())).thenReturn(lCustomerAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        assertThrows(ECMException.class, () -> CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap));
        verify(mCustomerLocationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportWiseAccountsData_withEmptyMapObject() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithAccountDetailsFieldsAndBlankfield.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Customer lCustomer = new Customer();
        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(null);
        lCustomer.setCustomerECMId(lCustomerECMId);

        Map<String, Account> pFilteredAccountMap = new HashMap<>();

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerLocationRepository, times(0)).saveAll(anyList());
    }

    @Test
    void testImportWiseAccountsData_withNullMapObject() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithAccountDetailsFieldsAndBlankfield.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = new Customer();
        Location lLocation = new Location();
        lLocation.setCompanyNumber(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(null);
        lCustomer.setCustomerECMId(lCustomerECMId);

        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Map<String, Account> pFilteredAccountMap = null;

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        CustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerLocationRepository, times(0)).saveAll(anyList());
    }

}
