package com.winsupply.mdmcustomertoecomsubscriber.service;

import com.win.email.service.EmailService;
import com.winsupply.common.utils.Utils;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.config.EmailConfig;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerResupply;
import com.winsupply.mdmcustomertoecomsubscriber.exception.ECMException;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.processor.AddressProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.ContactProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.CustomerAccountProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.processor.CustomerMergeProcessor;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerResupplyRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerSubscriberServiceTest {

    @InjectMocks
    private CustomerSubscriberService mCustomerSubscriberService;

    @Mock
    private CustomerRepository mCustomerRepository;

    @Mock
    private CustomerResupplyRepository mCustomerResupplyRepository;

    @Mock
    private MessageHeaders lMessageHeaders;

    @Mock
    private CustomerMergeProcessor mCustomerMergeProcessor;

    @Mock
    private CustomerAccountProcessor mCustomerAccountProcessor;

    @Mock
    private AddressProcessor mAddressProcessor;

    @Mock
    private EmailConfig mEmailConfig;

    @Mock
    private EmailService mEmailService;

    @Mock
    private TimerTask mTimerTask;

    @Mock
    private ContactProcessor mContactProcessor;

    @Test
    void testInitialize() {
        List<String> lMailTos = new ArrayList<>();
        when(mEmailConfig.getMailTos()).thenReturn(lMailTos);
        when(mEmailConfig.getHost()).thenReturn("host created");
        when(mEmailConfig.getThreshold()).thenReturn(101L);
        when(mEmailConfig.getSecondThreshold()).thenReturn(102L);
        when(mEmailConfig.getThirdThreshold()).thenReturn(103L);
        when(mEmailConfig.getSuccessThreshold()).thenReturn(104L);
        when(mEmailConfig.getBatchSize()).thenReturn(5);
        when(mEmailConfig.getTimePeriod()).thenReturn(60L);

        mCustomerSubscriberService.initialize();
        verify(mEmailConfig, times(1)).getBatchSize();
        verify(mEmailConfig, times(2)).getTimePeriod();
    }

    @Test
    void testProcessCustomerMessage_Exception_WithInvalidPayload() {
        String lListenerMessege = Utils.readFile("customerPayload_Invalid.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(1, null, "IMPORTANT");
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mEmailService, times(1)).setEmailBody(anyString());
        verify(mEmailService, times(1)).failureCheck(1, null, "IMPORTANT");
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_WithEmptyCustomerEcmID() {
        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"customerEcmId\": \"24369121\"", "\"customerEcmId\": \"\"");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_ForExistingCustomersUpdating_With_AtgAccounts_FederalIdsAsTaxFedAndTaxNotFed_VmiLocations()
            throws IOException, ECMException {
        String lListenerMessege = Utils.readFile("customerPayload_WithAtgAcc_FederalId_VmiLocation.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        List<String> lPayloadStorages = new ArrayList<>();

        boolean lIsSuccess = true;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mAddressProcessor, times(1)).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(1)).mergeCustomer(any(Customer.class), anyList());
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_NewCustomerCreating_With_OneAtgAccount_AndEmpty_VmiLocation() throws IOException, ECMException {
        String lListenerMessege = Utils.readFile("customerPayload_With1AtgAccount.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();

        List<String> lPayloadStorages = null;

        boolean lIsSuccess = false;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mAddressProcessor, times(1)).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(1)).mergeCustomer(any(Customer.class), anyList());
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_ForExistingCustomerUpdating_WithNull_VmiLoc_WiseAcc__Phones_Emails_FederalId_AtgAcc()
            throws IOException, ECMException {
        String lListenerMessege = Utils.readFile("customerPayload_With1AtgAccount_Null_VmiLoc_WiseAcc_Phones_Emails_FederalId_AtgAcc.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();

        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lPayloadStorages = null;

        boolean lIsSuccess = false;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mAddressProcessor, times(1)).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_NewCustomerCreating_WithEmpty_Phones_Emails_FederalId_CompanyNoForWiseAccounts()
            throws IOException, ECMException {
        String lListenerMessege = Utils.readFile("customerPayload_WithEmpty_Phones_Emails_FederalId_CompanyNo.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        CustomerMessageVO lCustomerMessageVO = com.winsupply.mdmcustomertoecomsubscriber.common.Utility.unmarshallData(lListenerMessege,
                CustomerMessageVO.class);

        Customer lCustomer = new Customer();

        List<String> lPayloadStorages = null;

        boolean lIsSuccess = false;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mAddressProcessor, times(1)).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_ExceptionForCreatingNewCustomer_ForMultipleWiseAccounts_WithAccECommStatus_Y() {
        String lListenerMessege = Utils.readFile("customerPayload_HandlingException_WithMultipleWiseAccounts_WithAccECommStatus_Y.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        Customer lCustomer = new Customer();

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mEmailService, times(1)).failureCheck(anyInt(), anyString(), anyString());
        verify(mEmailService, times(1)).setEmailBody(anyString());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }

    @Test
    void testProcessCustomerMessage_ToDeleteCustomerWith_DeleteActionCode() {
        String lListenerMessege = Utils.readFile("customerPayloadWithOnlyCustomerEcmId.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("delete");

        List<String> lPayloadStorages = null;

        boolean lIsSuccess = false;

        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lListenerMessege, lMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @BeforeEach
    void setEmail() {
        ReflectionTestUtils.setField(mCustomerSubscriberService, "mEmailService", mEmailService);
    }

}
