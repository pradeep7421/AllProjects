package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.common.utils.Utils;
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
    CustomerAccountProcessor mCustomerAccountProcessor;
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
    void testImportWiseAccountsData_ResetCustomerAccData_withNull_FilteredAccMap() throws IOException, ECMException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = new Customer();
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

        mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerLocationRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerAccountNumberRepository, times(1)).deleteAllByCustomerECMId(anyString());
    }

    @Test
    void testImportWiseAccountsData_ResetCustomerAccDataWithEmpty_FilteredAccountMap() throws IOException, ECMException {

        String lListenerMessege = Utils.readFile("customerPayloadWithAccountDetailsFieldsAndBlankfield.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Customer lCustomer = createCustomer(lCustomerMessageVO, lCustomerECMId);

        Map<String, Account> pFilteredAccountMap = new HashMap<>();

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);

        mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerSubAccountRepository, times(1)).findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0));
        verify(mCustomerAccountRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerSubAccountRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerLocationRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerAccountNumberRepository, times(1)).deleteAllByCustomerECMId(anyString());
    }

    @Test
    void testImportWiseAccountsData_processFilteredAccounts_WithValidAndNonEmptyData() throws IOException, ECMException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"interCompanyId\": \"\"", "\"interCompanyId\": \"00065\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();
        Customer lCustomer = new Customer();
        Location lLocation = null;

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        lLocation = getFilteredAccountMap(lCustomerMessageVO, lCustomer, lLocation, pFilteredAccountMap);
        lCustomer.setWinDefaultCompany(lLocation);

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
        lCustomerSubAccount.setAccountNumber(lCustomerMessageVO.getWiseAccounts().get(0).getAccountNumber());
        lCustomerSubAccount.setLocation(lLocation);
        lInActiveCustomerSubAccounts.add(lCustomerSubAccount);

        List<CustomerAccount> lCustomerAccounts = new ArrayList<>();
        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();
        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();
        List<CustomerLocation> lCustomerLocations = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), anyList(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountRepository.saveAll(anyList())).thenReturn(lCustomerAccounts);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);

        mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mCustomerAccountRepository, times(1)).saveAll(anyList());
        verify(mCustomerAccountNumberRepository, times(1)).saveAll(anyList());
        verify(mCustomerLocationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportWiseAccountsData_ExceptionForProcessFilteredAccounts_With2WiseAccounts_WithNull_InActiveCustomerSubAccounts_EmptyCustomerAccAttributes()
            throws IOException, ECMException {

        String lListenerMessege = Utils.readFile("customerPayload_WithEmpty_CustomerAccAttributes.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Customer lCustomer = new Customer();
        Location lLocation = null;

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        lLocation = getFilteredAccountMap(lCustomerMessageVO, lCustomer, lLocation, pFilteredAccountMap);
        lCustomer.setWinDefaultCompany(null);

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = null;
        List<String> lSubAccountCustomerNumbersForAccount = new ArrayList<>();
        List<CustomerAccountNumber> lCustomerAccountNumbers = new ArrayList<>();
        List<CustomerLocation> lCustomerLocations = new ArrayList<>();

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(1).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), any(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);

        assertThrows(ECMException.class, () -> mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap));
        verify(mCustomerSubAccountProcessor, times(2)).processSubAccountsData(any(Account.class), any(), any(Customer.class), any(Location.class));
        verify(mCustomerAccountNumberRepository, times(1)).saveAll(anyList());
        verify(mCustomerLocationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportWiseAccountsData_ExceptionForProcessFilteredAccounts_withEmpty_OptionalLocationAnd_NullCustomersLocation()
            throws IOException, ECMException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = new Customer();
        Location lLocation = null;

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        lLocation = getFilteredAccountMap(lCustomerMessageVO, lCustomer, lLocation, pFilteredAccountMap);
        lCustomer.setWinDefaultCompany(lLocation);

        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        List<CustomerSubAccount> lInActiveCustomerSubAccounts = new ArrayList<>();
        CustomerSubAccount lCustomerSubAccount = new CustomerSubAccount();
        lCustomerSubAccount.setAccountNumber(lCustomerMessageVO.getWiseAccounts().get(0).getAccountNumber());

        lCustomerSubAccount.setLocation(lLocation);
        lInActiveCustomerSubAccounts.add(lCustomerSubAccount);

        when(mCustomerSubAccountRepository.findByCustomerCustomerECMIdAndStatusId(anyString(), eq((short) 0)))
                .thenReturn(lInActiveCustomerSubAccounts);
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.empty());

        assertThrows(ECMException.class, () -> mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap));
        verify(mCustomerAccountRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerSubAccountRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerAccountNumberRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mLocationRepository, times(1)).findById(anyString());
    }

    @Test
    void testImportWiseAccountsData_withEmpty_InterCompanyId_ForInterCompanyMessege() throws IOException, ECMException {

        String lListenerMessege = Utils.readFile("customerPayload_WithEmpty_InterCompanyId.json");
        lListenerMessege = lListenerMessege.replace("\"interCompanyId\": \"00066\"", "\"interCompanyId\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();
        String lInterCompanyId = lCustomerMessageVO.getInterCompanyId();

        Customer lCustomer = new Customer();
        Location lLocation = null;

        Map<String, Account> pFilteredAccountMap = new HashMap<>();
        lLocation = getFilteredAccountMap(lCustomerMessageVO, lCustomer, lLocation, pFilteredAccountMap);

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
        Mockito.doNothing().when(mCustomerAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerSubAccountRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerLocationRepository).deleteAllByCustomerECMId(lCustomerECMId);
        Mockito.doNothing().when(mCustomerAccountNumberRepository).deleteAllByCustomerECMId(lCustomerECMId);
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mLocationRepository.findById(lCustomerMessageVO.getWiseAccounts().get(1).getCompanyNumber())).thenReturn(Optional.of(lLocation));
        when(mCustomerSubAccountProcessor.processSubAccountsData(any(Account.class), anyList(), any(Customer.class), any(Location.class)))
                .thenReturn(lSubAccountCustomerNumbersForAccount);
        when(mCustomerAccountNumberRepository.saveAll(anyList())).thenReturn(lCustomerAccountNumbers);
        when(mCustomerLocationRepository.saveAll(anyList())).thenReturn(lCustomerLocations);

        mCustomerAccountProcessor.importWiseAccountsData(lCustomer, lInterCompanyId, pFilteredAccountMap);
        verify(mLocationRepository, times(2)).findById(anyString());
        verify(mCustomerSubAccountProcessor, times(2)).processSubAccountsData(any(Account.class), anyList(), any(Customer.class),
                any(Location.class));
        verify(mCustomerAccountNumberRepository, times(1)).saveAll(anyList());
        verify(mCustomerLocationRepository, times(1)).saveAll(anyList());
    }

    private Customer createCustomer(final CustomerMessageVO pCustomerMessageVO, final String pCustomerECMId) {

        Customer lCustomer = new Customer();
        Location lLocation = new Location();
        lLocation.setCompanyNumber(pCustomerMessageVO.getWiseAccounts().get(0).getCompanyNumber());
        lCustomer.setWinDefaultCompany(null);
        lCustomer.setCustomerECMId(pCustomerECMId);
        return lCustomer;
    }

    private Location getFilteredAccountMap(final CustomerMessageVO pCustomerMessageVO, final Customer pCustomer, Location pLocation,
            final Map<String, Account> pFilteredAccountMap) {

        for (Account lAccount : pCustomerMessageVO.getWiseAccounts()) {
            String lCompanyNumber = lAccount.getCompanyNumber();
            pFilteredAccountMap.put(lCompanyNumber, lAccount);

            if (pLocation == null) {
                pLocation = new Location();
                pLocation.setCompanyNumber(lAccount.getCompanyNumber());
                pCustomer.setCustomerECMId(pCustomerMessageVO.getCustomerEcmId());
            }
        }
        return pLocation;
    }
}
