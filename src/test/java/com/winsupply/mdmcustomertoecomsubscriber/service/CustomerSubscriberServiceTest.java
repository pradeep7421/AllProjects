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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ResourceUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//@SpringBootTest
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

    public static String readFile(final String pFilePath) throws IOException {
        final File lFile = ResourceUtils.getFile("classpath:" + pFilePath);
        final String lContent = new String(Files.readAllBytes(lFile.toPath()));
        return lContent;
    }

    void setUp() {
        
        System.out.println("hashCode of mocked emailservice object --------------------"+mEmailService.hashCode());
        List<String> lMailTos = new ArrayList<>();
        when(mEmailConfig.getMailTos()).thenReturn(lMailTos);
        when(mEmailConfig.getHost()).thenReturn("host created");
        when(mEmailConfig.getThreshold()).thenReturn(101l);
        when(mEmailConfig.getSecondThreshold()).thenReturn(102l);
        when(mEmailConfig.getThirdThreshold()).thenReturn(103l);
        when(mEmailConfig.getSuccessThreshold()).thenReturn(104l);
        when(mEmailConfig.getBatchSize()).thenReturn(5);
        when(mEmailConfig.getTimePeriod()).thenReturn(60l);
        mCustomerSubscriberService.initialize();
        System.out.println("hashCode of mocked emailservice object --------------------"+mEmailService.hashCode());

        
    }

    @Test
    void testProcessCustomerMessage() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithoutCustomerEcmId() throws IOException, ECMException {
        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        lPayLoad = lPayLoad.replace(lCustomerMessageVO.getCustomerEcmId(), "");

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(0)).findById(anyString());
        verify(mContactProcessor, times(0)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(0)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(0)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(0)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(0)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessCustomerMessage_WithAtgAccounts() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        lPayLoad = lPayLoad.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "            \"atgSystemSrcId\": \"24369122\",\r\n" + "            \"type\": \"ATG\"\r\n" + "        }\r\n" + "    ]");

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(1)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mEmailConfig, times(2)).getEnvironment();

    }

    @Test
    void testProcessCustomerMessage_WithSameAtgSystemIdAndCustomerEcmId() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        lPayLoad = lPayLoad.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "            \"atgSystemSrcId\": \"24369121\",\r\n" + "            \"type\": \"ATG\"\r\n" + "        }\r\n" + "    ]");

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mEmailConfig, times(2)).getEnvironment();

    }

    @Test
    void testProcessCustomerMessage_WithNullAtgAccounts() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        lPayLoad = lPayLoad.replace("\"atgAccounts\": []", "\"atgAccounts\": null");

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mEmailConfig, times(2)).getEnvironment();

    }

    @Test
    void testProcessCustomerMessage_WithAtgAccountsMoreThen1() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        lPayLoad = lPayLoad.replace("\"atgAccounts\": []",
                "\"atgAccounts\": [\r\n" + "        {\r\n" + "            \"atgSystemSrcId\": \"24369321\",\r\n" + "            \"type\": \"ATG\"\r\n"
                        + "        },   {\r\n" + "            \"atgSystemSrcId\": \"2556655\",\r\n" + "            \"type\": \"ATG\"\r\n"
                        + "        }\r\n" + "    ]");

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(1)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);
        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
        verify(mEmailConfig, times(2)).getEnvironment();

    }

    @Test
    void testProcessCustomerMessage_CustomerNotInDB() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        lPayLoad = lPayLoad.replace("\"atgAccounts\": []",
                "\"atgAccounts\": [\r\n" + "        {\r\n" + "            \"atgSystemSrcId\": \"24369321\",\r\n" + "            \"type\": \"ATG\"\r\n"
                        + "        },   {\r\n" + "            \"atgSystemSrcId\": \"2556655\",\r\n" + "            \"type\": \"ATG\"\r\n"
                        + "        }\r\n" + "    ]");

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(1)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithResupllyLocation() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        lPayLoad = lPayLoad.replace("\"vmiLocations\": []",
                "\"vmiLocations\": [\r\n" + "            \"NewYork\",\r\n" + "            \"washinton DC\"      \r\n" + "    ]");

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(1)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithNullResupllyLocation() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        lPayLoad = lPayLoad.replace("\"vmiLocations\": []", "\"vmiLocations\": null");

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyResupllyLocation() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithNullEmailsList() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"emails\": [\r\n" + "        {\r\n" + "            \"rowidObject\": \"3671532       \",\r\n"
                + "            \"emailAddress\": \"JKDMECHANICALINC@GMAIL.COM\",\r\n" + "            \"emailType\": \"EW\",\r\n"
                + "            \"optOutInd\": \"\",\r\n" + "            \"preferenceLevel\": \"\",\r\n" + "            \"comments\": \"\",\r\n"
                + "            \"isPreferredContactMethod\": \"N\",\r\n" + "            \"emailValidationDate\": \"\",\r\n"
                + "            \"emailStatusDescription\": \"\",\r\n" + "            \"emailValidationStatus\": \"\"\r\n" + "        }\r\n" + "    ]",
                "\"emails\": null");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyEmailsList() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"emails\": [\r\n" + "        {\r\n" + "            \"rowidObject\": \"3671532       \",\r\n"
                + "            \"emailAddress\": \"JKDMECHANICALINC@GMAIL.COM\",\r\n" + "            \"emailType\": \"EW\",\r\n"
                + "            \"optOutInd\": \"\",\r\n" + "            \"preferenceLevel\": \"\",\r\n" + "            \"comments\": \"\",\r\n"
                + "            \"isPreferredContactMethod\": \"N\",\r\n" + "            \"emailValidationDate\": \"\",\r\n"
                + "            \"emailStatusDescription\": \"\",\r\n" + "            \"emailValidationStatus\": \"\"\r\n" + "        }\r\n" + "    ]",
                "\"emails\": []");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyEmailType() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"emailType\": \"EW\"", "\"emailType\": \"\"");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithDifferentEmailType() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"emailType\": \"EW\"", "\"emailType\": \"AA\"");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithNullPhonesList() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"phones\": [\r\n" + "        {\r\n" + "            \"rowidObject\": \"1322693       \",\r\n"
                + "            \"phoneNumber\": \"4848889420\",\r\n" + "            \"phoneExtension\": \"\",\r\n"
                + "            \"phoneType\": \"LB\",\r\n" + "            \"optOut\": \"\",\r\n" + "            \"doNotCall\": \"\",\r\n"
                + "            \"preferenceLevel\": \"\",\r\n" + "            \"comments\": \"\",\r\n"
                + "            \"isPreferredContactMethod\": \"N\"\r\n" + "        }\r\n" + "    ]", "\"phones\": null");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyPhonesList() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"phones\": [\r\n" + "        {\r\n" + "            \"rowidObject\": \"1322693       \",\r\n"
                + "            \"phoneNumber\": \"4848889420\",\r\n" + "            \"phoneExtension\": \"\",\r\n"
                + "            \"phoneType\": \"LB\",\r\n" + "            \"optOut\": \"\",\r\n" + "            \"doNotCall\": \"\",\r\n"
                + "            \"preferenceLevel\": \"\",\r\n" + "            \"comments\": \"\",\r\n"
                + "            \"isPreferredContactMethod\": \"N\"\r\n" + "        }\r\n" + "    ]", "\"phones\": []");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyPhonesNumberAndPhoneType() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"phoneNumber\": \"4848889420\"", "\"phoneNumber\": \"\"");
        lPayLoad = lPayLoad.replace("\"phoneType\": \"LB\"", "\"phoneType\": \"\"");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyPhoneType() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"phoneType\": \"LB\"", "\"phoneType\": \"\"");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyPhoneNo() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);
        lPayLoad = lPayLoad.replace("\"phoneNumber\": \"4848889420\"", "\"phoneNumber\": \"\"");

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithPhoneTypeFX() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        lPayLoad = lPayLoad.replace("\"phoneType\": \"LB\"", "\"phoneType\": \"FX\"");

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithDifferentPhoneType() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        lPayLoad = lPayLoad.replace("\"phoneType\": \"LB\"", "\"phoneType\": \"AA\"");

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");
        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.empty());
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);
        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(any(Customer.class), anyString(), anyMap());

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(any(Customer.class), anyString(), anyMap());
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithNullWiseAccount() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoadWiseAccountNull.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);

        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(lCustomer, lCustomerMessageVO.getInterCompanyId(), null);

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(lCustomer, lCustomerMessageVO.getInterCompanyId(), null);
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_WithEmptyWiseAccount() throws IOException, ECMException {

        setUp();
        String lPayLoad = readFile("payLoadWiseAccountNullEmpty.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);

        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(lCustomer, lCustomerMessageVO.getInterCompanyId(), null);

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(lCustomer, lCustomerMessageVO.getInterCompanyId(), null);
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testProcessCustomerMessage_processWiseAccount() throws IOException, ECMException {
        System.out.println("hashCode of mocked emailservice object --------------------"+mEmailService.hashCode());
        System.out.println(mEmailService.getBatchSize());

//         setUp();
        
        mEmailService = Mockito.mock(EmailService.class);

        String lPayLoad = readFile("payLoadWithWiseAccounts.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders mMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mContactProcessor).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        Mockito.doNothing().when(mCustomerMergeProcessor).mergeCustomer(any(Customer.class), anyList());
        when(mEmailConfig.getEnvironment()).thenReturn("env 1");

        List<String> lPayloadStorages = new ArrayList<>();
        when(mEmailService.getPayloadStorage()).thenReturn(lPayloadStorages);
        when(mEmailService.successCheck(101, 102, "fail msg", "fail subject", "success subject", "success subject")).thenReturn(true);
        Mockito.doNothing().when(mEmailService).setEmailSubject(anyString());
        Mockito.doNothing().when(mEmailService).setEmailBody(anyString());
        Mockito.doNothing().when(mEmailService).failureCheck(anyInt(), anyString(), anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).setSubAccountCustomerNumbers(null);

        Customer lCustomer = new Customer();
        lCustomer.setPhone("12345665");

        when(mCustomerRepository.findById(lCustomerMessageVO.getCustomerEcmId())).thenReturn(Optional.of(lCustomer));
        when(mCustomerRepository.save(any(Customer.class))).thenReturn(lCustomer);
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());

        List<CustomerResupply> lResupplyLocations = new ArrayList<>();

        when(mCustomerResupplyRepository.saveAll(anyList())).thenReturn(lResupplyLocations);

        Mockito.doNothing().when(mCustomerAccountProcessor).importWiseAccountsData(lCustomer, lCustomerMessageVO.getInterCompanyId(), null);

        Mockito.doNothing().when(mAddressProcessor).importAddressesData(lCustomer, lCustomerMessageVO.getAddresses());

        mCustomerSubscriberService.processCustomerMessage(lPayLoad, mMessageHeaders);
        verify(mCustomerRepository, times(1)).findById(anyString());
        verify(mContactProcessor, times(1)).createOrUpdateContacts(any(Customer.class), any(CustomerMessageVO.class));
        verify(mCustomerMergeProcessor, times(0)).mergeCustomer(any(Customer.class), anyList());
        verify(mCustomerAccountProcessor, times(1)).setSubAccountCustomerNumbers(null);

        verify(mCustomerAccountProcessor, times(1)).importWiseAccountsData(lCustomer, lCustomerMessageVO.getInterCompanyId(), null);
        verify(mCustomerRepository, times(1)).save(any(Customer.class));
        verify(mCustomerResupplyRepository, times(1)).deleteAllByCustomerECMId(anyString());
        verify(mCustomerResupplyRepository, times(0)).saveAll(anyList());
        verify(mAddressProcessor, times(1)).importAddressesData(any(Customer.class), anyList());
        verify(mEmailService, times(0)).getPayloadStorage();
        verify(mEmailService, times(0)).successCheck(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    }

    @Test
    void testInitialize() {
        List<String> lMailTos = new ArrayList<>();
        when(mEmailConfig.getMailTos()).thenReturn(lMailTos);
        when(mEmailConfig.getHost()).thenReturn("host created");
        when(mEmailConfig.getThreshold()).thenReturn(101l);
        when(mEmailConfig.getSecondThreshold()).thenReturn(102l);
        when(mEmailConfig.getThirdThreshold()).thenReturn(103l);
        when(mEmailConfig.getSuccessThreshold()).thenReturn(104l);
        when(mEmailConfig.getBatchSize()).thenReturn(5);
        when(mEmailConfig.getTimePeriod()).thenReturn(60l);

        mCustomerSubscriberService.initialize();
        verify(mEmailConfig, times(1)).getMailTos();
        verify(mEmailConfig, times(1)).getHost();
        verify(mEmailConfig, times(1)).getThreshold();
        verify(mEmailConfig, times(1)).getSecondThreshold();
        verify(mEmailConfig, times(1)).getThirdThreshold();
        verify(mEmailConfig, times(1)).getSuccessThreshold();
        verify(mEmailConfig, times(1)).getBatchSize();
        verify(mEmailConfig, times(2)).getTimePeriod();
    }
}
