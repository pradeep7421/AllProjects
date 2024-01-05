package com.winsupply.mdmcustomertoecomsubscriber.service;

import com.win.email.service.EmailService;
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
import com.winsupply.readfile.PayLoadReadFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    CustomerSubscriberService mCustomerSubscriberService;

    @Mock
    CustomerRepository mCustomerRepository;

    @Mock
    CustomerResupplyRepository mCustomerResupplyRepository;

    @Mock
    MessageHeaders mMessageHeaders;
    @Mock
    CustomerMergeProcessor mCustomerMergeProcessor;
    @Mock
    CustomerAccountProcessor mCustomerAccountProcessor;
    @Mock
    AddressProcessor mAddressProcessor;
    @Mock
    EmailConfig mEmailConfig;
    @Mock
    EmailService mEmailService;
    @Mock
    TimerTask mTimerTask;

    @Mock
    ContactProcessor mContactProcessor;

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

    @BeforeEach
    void setEmail() {
        ReflectionTestUtils.setField(mCustomerSubscriberService, "mEmailService", mEmailService);
    }

    @Test
    void testProcessCustomerMessage() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModified.json");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        List<String> lPayloadStorages = new ArrayList<>();

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        boolean lIsSuccess = true;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_WithOneAtgAccount() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModifiedWith1AtgAccount.json");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<String> lPayloadStorages = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        boolean lIsSuccess = false;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_ForElse_WithChangesInFields() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModifiedWith1AtgAccount.json");
        lPayLoad = lPayLoad.replace("\"atgSystemSrcId\": \"24369121\"", "\"atgSystemSrcId\": \"0000000\"");
        lPayLoad = lPayLoad.replace("\"vmiLocations\": []", "\"vmiLocations\": null");
        lPayLoad = lPayLoad.replace("\"wiseAccounts\": []", "\"wiseAccounts\": null");
        lPayLoad = lPayLoad.replace("\"phoneType\": \"FX\"", "\"phoneType\": \"ANY\"");
        lPayLoad = lPayLoad.replace("\"emailType\": \"ANY\"", "\"emailType\": \"\"");
        lPayLoad = lPayLoad.replace("\"federalIds\": []", "\"federalIds\": null");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<String> lPayloadStorages = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        boolean lIsSuccess = false;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_ForElse_WithEmptyOrNullChecks() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModifiedWithNullOrEmptyCheck.json");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<String> lPayloadStorages = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        boolean lIsSuccess = false;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_WithEmptyCompanyNo_PhoneNo_NullEmails() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModifiedWithNullEmailList.json");
        lPayLoad = lPayLoad.replace("\"companyNumber\": \"00065\"", "\"companyNumber\": \"\"");
        lPayLoad = lPayLoad.replace("\"phoneNumber\": \"4848889420\"", "\"phoneNumber\": \"\"");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        boolean lIsSuccess = true;

        List<String> lPayloadStorages = null;
        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).getPayloadStorage();
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_HandlingException() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModified.json");
        lPayLoad = lPayLoad.replace("\"accountEcommerceStatus\": \"N\"", "\"accountEcommerceStatus\": \"Y\"");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).setEmailBody(anyString());
        verify(mEmailService, times(1)).failureCheck(anyInt(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_ForElse_WithEmptyOrNullChecksFederalId() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadModifiedWithNullOrEmptyCheck.json");
        lPayLoad = lPayLoad.replace("\"federalIds\": []", "\"federalIds\": [\r\n" + "        {}\r\n" + "        ]");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<String> lPayloadStorages = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        boolean lIsSuccess = false;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_ForElse_WithNullPhonesList() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithEmptyPhoneList.json");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<String> lPayloadStorages = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        boolean lIsSuccess = false;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_ForElse_WithEmptyPhones() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithEmptyPhoneList.json");
        lPayLoad = lPayLoad.replace("\"phones\": null", "\"phones\": []");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        List<String> lPayloadStorages = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        boolean lIsSuccess = false;
        when(mEmailService.successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(lIsSuccess);
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), any(), any());
        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mEmailService, times(1)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_ForElse_WithEmptyCustomerEcmID() throws IOException, ECMException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithEmptyPhoneList.json");
        lPayLoad = lPayLoad.replace("\"customerEcmId\": \"24369121\"", "\"customerEcmId\": \"\"");

        Map<String, Object> lHeaders = new HashMap<>();
        String lActionCode = "not_delete";
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
    }
}
