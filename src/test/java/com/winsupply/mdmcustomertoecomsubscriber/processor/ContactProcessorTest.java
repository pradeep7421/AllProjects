package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.common.utils.Utils;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactEmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactIndustryPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactLocationPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactRole;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.EmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Industry;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListGroup;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Order;
import com.winsupply.mdmcustomertoecomsubscriber.entities.OrderEmailAddress;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Quote;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactEmailPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactIndustryPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactLocationPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactOtherAddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRecentlyViewedItemRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRoleRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.EmailPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.IndustryRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ListGroupRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.OrderEmailAddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.OrderRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.PhoneRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.PhoneTypeRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.QuoteRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactProcessorTest {
    @InjectMocks
    ContactProcessor mContactProcessor;
    @Mock
    ContactRepository mContactRepository;
    @Mock
    ContactRoleRepository mContactRoleRepository;
    @Mock
    ContactEmailPreferenceRepository mContactEmailPreferenceRepository;
    @Mock
    ContactIndustryPreferenceRepository mContactIndustryPreferenceRepository;
    @Mock
    PhoneRepository mPhoneRepository;
    @Mock
    OrderEmailAddressRepository mOrderEmailAddressRepository;
    @Mock
    AddressRepository mAddressRepository;
    @Mock
    PhoneTypeRepository mPhoneTypeRepository;
    @Mock
    IndustryRepository mIndustryRepository;
    @Mock
    EmailPreferenceRepository mEmailPreferenceRepository;
    @Mock
    QuoteRepository mQuoteRepository;
    @Mock
    ListGroupRepository mListGroupRepository;
    @Mock
    ContactLocationPreferenceRepository mContactLocationPreferenceRepository;
    @Mock
    ContactRecentlyViewedItemRepository mContactRecentlyViewedItemRepository;
    @Mock
    ContactOtherAddressRepository mContactOtherAddressRepository;
    @Mock
    OrderRepository mOrderRepository;
    @Mock
    CustomerAccountProcessor mCustomerAccountProcessor;

    @Test
    void testCreateOrUpdateContacts_WithEmptyContactsInPayLoad() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayloadWithEmptyContacts.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        List<Contact> lContacts = new ArrayList<>();
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyContactsFromCustomerMessageVO_AndWithNonEmpty_ContactsfromDB_Quotes_ListGroups_Orders()
            throws IOException {

        String lListenerMessege = Utils.readFile("customerPayloadWithEmptyContacts.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Contact lContact = new Contact();
        List<Contact> lContacts = new ArrayList<>();
        lContact.setContactECMId(lCustomerMessageVO.getCustomerEcmId());
        lContacts.add(lContact);

        List<Quote> lQuotes = new ArrayList<>();
        lQuotes.add(new Quote());

        List<ListGroup> lListGroups = new ArrayList<>();
        lListGroups.add(new ListGroup());

        List<Order> lOrders = new ArrayList<>();
        lOrders.add(new Order());

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        Mockito.doNothing().when(mContactEmailPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactIndustryPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactLocationPreferenceRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactRecentlyViewedItemRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactOtherAddressRepository).deleteAllByIdContactECMId(anyString());
        when(mQuoteRepository.findByContactContactECMId(anyString())).thenReturn(lQuotes);
        when(mQuoteRepository.saveAll(anyList())).thenReturn(lQuotes);
        when(mListGroupRepository.findByContactContactECMId(anyString())).thenReturn(lListGroups);
        when(mListGroupRepository.saveAll(anyList())).thenReturn(lListGroups);
        when(mOrderRepository.findAllByContactContactECMId(anyString())).thenReturn(lOrders);
        when(mOrderRepository.saveAll(anyList())).thenReturn(lOrders);
        when(mOrderRepository.findAllByApproverContactContactECMId(anyString())).thenReturn(lOrders);
        Mockito.doNothing().when(mContactRepository).deleteById(anyString());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mOrderRepository, times(2)).saveAll(anyList());
        verify(mContactRepository, times(1)).deleteById(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyQuotes_ListGroups_Order_NonEmptyContactsfromDB() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayloadWithEmptyContacts.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getCustomerEcmId());
        lContacts.add(lContact);

        List<Quote> lQuotes = new ArrayList<>();

        List<ListGroup> lListGroups = new ArrayList<>();

        List<Order> lOrders = new ArrayList<>();

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        Mockito.doNothing().when(mContactEmailPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactIndustryPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactLocationPreferenceRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactRecentlyViewedItemRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactOtherAddressRepository).deleteAllByIdContactECMId(anyString());
        when(mQuoteRepository.findByContactContactECMId(anyString())).thenReturn(lQuotes);
        when(mListGroupRepository.findByContactContactECMId(anyString())).thenReturn(lListGroups);
        when(mOrderRepository.findAllByContactContactECMId(anyString())).thenReturn(lOrders);
        when(mOrderRepository.findAllByApproverContactContactECMId(anyString())).thenReturn(lOrders);
        Mockito.doNothing().when(mContactRepository).deleteById(anyString());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mOrderRepository, times(1)).findAllByApproverContactContactECMId(anyString());
        verify(mContactRepository, times(1)).deleteById(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyContactsInCustomerMessegeVo_AndWithQuotes_ListGroups_As_Null() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayloadWithEmptyContacts.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getCustomerEcmId());
        List<Contact> lContacts = new ArrayList<>();
        lContacts.add(lContact);

        List<Quote> lQuotes = null;

        List<ListGroup> lListGroups = null;

        List<Order> lOrders = new ArrayList<>();

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        Mockito.doNothing().when(mContactEmailPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactIndustryPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactLocationPreferenceRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactRecentlyViewedItemRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactOtherAddressRepository).deleteAllByIdContactECMId(anyString());
        when(mQuoteRepository.findByContactContactECMId(anyString())).thenReturn(lQuotes);
        when(mListGroupRepository.findByContactContactECMId(anyString())).thenReturn(lListGroups);
        when(mOrderRepository.findAllByContactContactECMId(anyString())).thenReturn(lOrders);
        when(mOrderRepository.findAllByApproverContactContactECMId(anyString())).thenReturn(lOrders);
        Mockito.doNothing().when(mContactRepository).deleteById(anyString());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mOrderRepository, times(1)).findAllByApproverContactContactECMId(anyString());
        verify(mContactRepository, times(1)).deleteById(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithContactsInPayLoadAndDB() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc_123\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getCustomerEcmId() + "-");
        lContacts.add(lContact);

        List<Quote> lQuotes = new ArrayList<>();

        List<ListGroup> lListGroups = new ArrayList<>();

        List<Order> lOrders = new ArrayList<>();

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        Mockito.doNothing().when(mContactEmailPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactIndustryPreferenceRepository).deleteAllByIdContactEcmId(anyString());
        Mockito.doNothing().when(mContactLocationPreferenceRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactRecentlyViewedItemRepository).deleteAllByIdContactECMId(anyString());
        Mockito.doNothing().when(mContactOtherAddressRepository).deleteAllByIdContactECMId(anyString());
        when(mQuoteRepository.findByContactContactECMId(anyString())).thenReturn(lQuotes);
        when(mListGroupRepository.findByContactContactECMId(anyString())).thenReturn(lListGroups);
        when(mOrderRepository.findAllByContactContactECMId(anyString())).thenReturn(lOrders);
        when(mOrderRepository.findAllByApproverContactContactECMId(anyString())).thenReturn(lOrders);
        Mockito.doNothing().when(mContactRepository).deleteById(anyString());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mOrderRepository, times(1)).findAllByApproverContactContactECMId(anyString());
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyContactsInDB() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        List<Contact> lContacts = new ArrayList<>();

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(anyString(), anyString())).thenReturn(lPreferenceList);

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mCustomerAccountProcessor, times(1)).getSubAccountCustomerNumbers();
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithNoneContactsToDeleteFromDbAndInvalidEmail() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId());
        lContacts.add(lContact);

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactLocationPreferenceRepository, times(1))
                .findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(), "defaultJob");
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithNonEmptyContactsInDBAndValidEmail() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");
        lContacts.add(lContact);

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.of(lContact));
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyLastName() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"firstName\": \"lou\"", "\"firstName\": \"lou\"");
        lListenerMessege = lListenerMessege.replace("\"lastName\": \"raymond\"", "\"lastName\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        List<Contact> lContacts = new ArrayList<>();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());

    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyFirstNameAndLastName() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"firstName\": \"lou\"", "\"firstName\": \"\"");
        lListenerMessege = lListenerMessege.replace("\"lastName\": \"raymond\"", "\"lastName\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        List<Contact> lContacts = new ArrayList<>();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mCustomerAccountProcessor, times(1)).getSubAccountCustomerNumbers();
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEcomStatusAsY() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"interCompanyId\": \"\"", "\"interCompanyId\": \"2001\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");
        lContacts.add(lContact);

        ContactRole lContactRole = new ContactRole();

        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.of(lContact));
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyOptionalContact() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"interCompanyId\": \"\"", "\"interCompanyId\": \"2001\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");
        lContacts.add(lContact);

        ContactRole lContactRole = new ContactRole();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithNonNullAddress() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");

        lListenerMessege = lListenerMessege.replace("\"industries\": \"\"", "\"industries\": \"industry A ,industry B\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"communicationPreference\": []",
                "\"communicationPreference\": [\r\n" + "               \"prefrence1\",\r\n" + "               \"preference2\"\r\n" + "            ]");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");
        Address lAddress = new Address();
        lAddress.setId(101);
        lContact.setAddress(lAddress);
        lContacts.add(lContact);

        ContactRole lContactRole = new ContactRole();

        List<ContactEmailPreference> lContactEmailPreferenceList = new ArrayList<>();

        EmailPreference lEmailPreference = new EmailPreference();
        lEmailPreference.setEmailPreferenceDesc("preference1");

        List<ContactIndustryPreference> lContactIndustryPreferencesList = new ArrayList<>();

        Industry lIndustry = new Industry();
        lIndustry.setIndustryId((short) 10);

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        Mockito.doNothing().when(mPhoneRepository).deleteAllByAddressId(anyInt());
        Mockito.doNothing().when(mOrderEmailAddressRepository).deleteAllByAddressId(anyInt());
        when(mEmailPreferenceRepository.findByEmailPreferenceDescIgnoreCase(anyString())).thenReturn(Optional.of(lEmailPreference));
        when(mContactEmailPreferenceRepository.saveAll(anySet())).thenReturn(lContactEmailPreferenceList);
        when(mIndustryRepository.findByIndustryDescIgnoreCase(anyString())).thenReturn(Optional.of(lIndustry));
        when(mContactIndustryPreferenceRepository.saveAll(anySet())).thenReturn(lContactIndustryPreferencesList);

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyOptionalEmailPrefrence() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"industries\": \"\"", "\"industries\": \"industry A ,industry B\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"communicationPreference\": []",
                "\"communicationPreference\": [\r\n" + "               \"prefrence1\",\r\n" + "               \"preference2\"\r\n" + "            ]");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");

        Address lAddress = new Address();
        lAddress.setId(101);
        lContact.setAddress(lAddress);
        lContacts.add(lContact);

        ContactRole lContactRole = new ContactRole();

        List<ContactEmailPreference> lContactEmailPreferenceList = new ArrayList<>();

        Industry lIndustry = new Industry();
        lIndustry.setIndustryId((short) 10);

        List<ContactIndustryPreference> lContactIndustryPreferencesList = new ArrayList<>();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        when(mEmailPreferenceRepository.findByEmailPreferenceDescIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactEmailPreferenceRepository.saveAll(anySet())).thenReturn(lContactEmailPreferenceList);
        when(mIndustryRepository.findByIndustryDescIgnoreCase(anyString())).thenReturn(Optional.of(lIndustry));
        when(mContactIndustryPreferenceRepository.saveAll(anySet())).thenReturn(lContactIndustryPreferencesList);
        Mockito.doNothing().when(mPhoneRepository).deleteAllByAddressId(anyInt());
        Mockito.doNothing().when(mOrderEmailAddressRepository).deleteAllByAddressId(anyInt());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithNullCommunicationPrefrence() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"industries\": \"\"", "\"industries\": \"industry A ,industry B\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"communicationPreference\": []", "\"communicationPreference\": null");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");

        Address lAddress = new Address();
        lAddress.setId(101);
        lContact.setAddress(lAddress);
        lContacts.add(lContact);

        ContactRole lContactRole = new ContactRole();

        Industry lIndustry = new Industry();
        lIndustry.setIndustryId((short) 10);
        List<ContactIndustryPreference> lContactIndustryPreferencesList = new ArrayList<>();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        when(mIndustryRepository.findByIndustryDescIgnoreCase(anyString())).thenReturn(Optional.of(lIndustry));
        when(mContactIndustryPreferenceRepository.saveAll(anySet())).thenReturn(lContactIndustryPreferencesList);
        Mockito.doNothing().when(mPhoneRepository).deleteAllByAddressId(anyInt());
        Mockito.doNothing().when(mOrderEmailAddressRepository).deleteAllByAddressId(anyInt());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithAddressAsNull() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"emailType\": \"EW\"", "\"emailType\": \"ON-EMAIL\"");
        lListenerMessege = lListenerMessege.replace("\"industries\": \"\"", "\"industries\": \"industry A ,industry B\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"communicationPreference\": []", "\"communicationPreference\": null");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");

        Address lAddress = null;
        lContact.setAddress(lAddress);
        lContacts.add(lContact);

        Industry lIndustry = new Industry();
        lIndustry.setIndustryId((short) 10);

        ContactRole lContactRole = new ContactRole();

        List<ContactIndustryPreference> lContactIndustryPreferencesList = new ArrayList<>();

        List<OrderEmailAddress> lOrderEmailAddressList = new ArrayList<>();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        when(mIndustryRepository.findByIndustryDescIgnoreCase(anyString())).thenReturn(Optional.of(lIndustry));
        when(mContactIndustryPreferenceRepository.saveAll(anySet())).thenReturn(lContactIndustryPreferencesList);
        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddress);
        when(mOrderEmailAddressRepository.saveAll(anySet())).thenReturn(lOrderEmailAddressList);

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithAddressAndEmptyOptionalIndustry() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"emailType\": \"EW\"", "\"emailType\": \"ON-EMAIL\"");

        lListenerMessege = lListenerMessege.replace("\"industries\": \"\"", "\"industries\": \"industry A ,industry B\"");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"communicationPreference\": []", "\"communicationPreference\": null");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());
        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");

        Address lAddress = new Address();
        lAddress.setId(101);
        lContact.setAddress(lAddress);
        lContacts.add(lContact);

        Industry lIndustry = new Industry();
        lIndustry.setIndustryId((short) 10);

        ContactRole lContactRole = new ContactRole();
        List<ContactIndustryPreference> lContactIndustryPreferencesList = new ArrayList<>();
        List<OrderEmailAddress> lOrderEmailAddressList = new ArrayList<>();

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        when(mIndustryRepository.findByIndustryDescIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactIndustryPreferenceRepository.saveAll(anySet())).thenReturn(lContactIndustryPreferencesList);
        when(mOrderEmailAddressRepository.saveAll(anySet())).thenReturn(lOrderEmailAddressList);
        Mockito.doNothing().when(mPhoneRepository).deleteAllByAddressId(anyInt());
        Mockito.doNothing().when(mOrderEmailAddressRepository).deleteAllByAddressId(anyInt());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithNullContactEmail_ContactPhones_Industries() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayloadWithNullContactEmailContactPhones.json");
        lListenerMessege = lListenerMessege.replace("\"userId\": \"\"", "\"userId\": \"abc123@xyz.com\"");
        lListenerMessege = lListenerMessege.replace("\"emailType\": \"EW\"", "\"emailType\": \"ON-EMAIL\"");
        lListenerMessege = lListenerMessege.replace("\"industries\": \"\"", "\"industries\": null");
        lListenerMessege = lListenerMessege.replace("\"contactECommerceStatus\": \"N\"", "\"contactECommerceStatus\": \"Y\"");
        lListenerMessege = lListenerMessege.replace("\"communicationPreference\": []", "\"communicationPreference\": null");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        ContactRole lContactRole = new ContactRole();
        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        List<Contact> lContacts = new ArrayList<>();

        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");

        Address lAddress = new Address();
        lAddress.setId(101);
        lContact.setAddress(lAddress);
        lContacts.add(lContact);

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        Mockito.doNothing().when(mPhoneRepository).deleteAllByAddressId(anyInt());
        Mockito.doNothing().when(mOrderEmailAddressRepository).deleteAllByAddressId(anyInt());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }

    @Test
    void testCreateOrUpdateContacts_WithEmptyContactEmail_ContactPhones() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayoadWithEmptyContactEmailContactPhonesfields.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        List<String> lSubAccountCustomerNumbers = new ArrayList<>();
        lSubAccountCustomerNumbers.add("00035");

        List<ContactLocationPreference> lPreferenceList = new ArrayList<>();
        ContactLocationPreference lContactLocationPreference = new ContactLocationPreference();
        lContactLocationPreference.setPreferenceValue("IMPORTANT");
        lPreferenceList.add(lContactLocationPreference);

        List<Contact> lContacts = new ArrayList<>();
        Contact lContact = new Contact();
        lContact.setContactECMId(lCustomerMessageVO.getContacts().get(0).getContactEcmId() + "10");
        lContact.setFirstName("lou");
        lContact.setLastName("raymond");

        Address lAddress = new Address();
        lAddress.setId(101);

        lContact.setAddress(lAddress);

        ContactRole lContactRole = new ContactRole();

        List<ContactIndustryPreference> lContactIndustryPreferencesList = new ArrayList<>();

        Industry lIndustry = new Industry();
        lIndustry.setIndustryId((short) 10);

        when(mCustomerAccountProcessor.getSubAccountCustomerNumbers()).thenReturn(lSubAccountCustomerNumbers);
        when(mContactLocationPreferenceRepository.findByIdContactECMIdAndIdPreferenceName(lCustomerMessageVO.getContacts().get(0).getContactEcmId(),
                "defaultJob")).thenReturn(lPreferenceList);
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.findByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(mContactRepository.save(any(Contact.class))).thenReturn(lContact);
        when(mContactRoleRepository.findById(anyInt())).thenReturn(Optional.of(lContactRole));
        when(mIndustryRepository.findByIndustryDescIgnoreCase(anyString())).thenReturn(Optional.of(lIndustry));
        when(mContactIndustryPreferenceRepository.saveAll(anySet())).thenReturn(lContactIndustryPreferencesList);
        Mockito.doNothing().when(mPhoneRepository).deleteAllByAddressId(anyInt());
        Mockito.doNothing().when(mOrderEmailAddressRepository).deleteAllByAddressId(anyInt());

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId(lCustomerMessageVO.getCustomerEcmId());

        mContactProcessor.createOrUpdateContacts(lCustomer, lCustomerMessageVO);
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
    }
}
