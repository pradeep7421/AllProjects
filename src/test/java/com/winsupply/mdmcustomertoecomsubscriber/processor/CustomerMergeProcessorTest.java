package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.common.utils.Utils;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListGroup;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListToCustomer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Order;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Quote;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ListToCustomerId;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AtgAccount;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerResupplyRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ListGroupRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ListToCustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.OrderRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerMergeProcessorTest {
    @InjectMocks
    CustomerMergeProcessor mCustomerMergeProcessor;
    @Mock
    QuoteRepository mQuoteRepository;
    @Mock
    ListGroupRepository mListGroupRepository;
    @Mock
    ListToCustomerRepository mListToCustomerRepository;
    @Mock
    ContactRepository mContactRepository;
    @Mock
    CustomerRepository mCustomerRepository;
    @Mock
    CustomerResupplyRepository mCustomerResupplyRepository;
    @Mock
    OrderRepository mOrderRepository;
    @Mock
    CustomerAccountProcessor mCustomerAccountProcessor;

    @Test
    void testMergeCustomer_handlingException_WhileDeleting_ExistingCustomer() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "      \"atgSystemSrcId\":null,\r\n" + "      \"type\":\"Type A\" \r\n" + "        }\r\n" + "    ]");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId("243612222");
        List<AtgAccount> lExistingCustomers = new ArrayList<>();
        lExistingCustomers.add(lCustomerMessageVO.getAtgAccounts().get(0));

        when(mCustomerRepository.findById(null)).thenReturn(Optional.of(lCustomer));
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(null);
        Mockito.doNothing().when(mCustomerAccountProcessor).resetCustomerAccountsData(anyString());
        assertThrows(Exception.class, () -> mCustomerMergeProcessor.mergeCustomer(lCustomer, lExistingCustomers));
        verify(mCustomerRepository, times(1)).findById(null);
        verify(mCustomerAccountProcessor, times(1)).resetCustomerAccountsData(null);
    }

    @Test
    void testMergeCustomer_ForCustomerNotExistingInDB_WhileDeleting_ExistingCustomer() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "      \"atgSystemSrcId\":null,\r\n" + "      \"type\":\"Type A\" \r\n" + "        }\r\n" + "    ]");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId("243612222");
        List<AtgAccount> lExistingCustomers = new ArrayList<>();
        lExistingCustomers.add(lCustomerMessageVO.getAtgAccounts().get(0));

        when(mCustomerRepository.findById(null)).thenReturn(Optional.empty());
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(null);
        Mockito.doNothing().when(mCustomerAccountProcessor).resetCustomerAccountsData(anyString());
        mCustomerMergeProcessor.mergeCustomer(lCustomer, lExistingCustomers);
        verify(mCustomerRepository, times(1)).findById(null);
    }

    @Test
    void testMergeCustomer_ForExistingCustomerInDb() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "      \"atgSystemSrcId\":\"24369121\",\r\n" + "      \"type\":\"Type A\" \r\n" + "        }\r\n" + "    ]");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId("243612222");

        List<ListToCustomer> lListToCustomersForDelete = new ArrayList<>();
        ListToCustomer lListToCustomer = new ListToCustomer();
        ListToCustomerId id = new ListToCustomerId();
        id.setCustomerECMId(lCustomerECMId);
        id.setListId(1001);
        lListToCustomer.setId(id);
        lListToCustomersForDelete.add(lListToCustomer);

        List<ListGroup> lListGroups = new ArrayList<>();
        lListGroups.add(new ListGroup());

        List<AtgAccount> lExistingCustomers = new ArrayList<>();
        lExistingCustomers.add(lCustomerMessageVO.getAtgAccounts().get(0));

        List<Contact> lContacts = new ArrayList<>();
        lContacts.add(new Contact());

        List<Quote> lQuotes = new ArrayList<>();
        lQuotes.add(new Quote());

        List<Order> lOrders = new ArrayList<>();
        lOrders.add(new Order());

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.of(lCustomer));
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).resetCustomerAccountsData(anyString());
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mContactRepository.saveAll(anyList())).thenReturn(lContacts);
        when(mQuoteRepository.findByCustomerCustomerECMId(lCustomerECMId)).thenReturn(lQuotes);
        when(mQuoteRepository.saveAll(anyList())).thenReturn(lQuotes);
        when(mListToCustomerRepository.findByIdCustomerECMId(anyString())).thenReturn(lListToCustomersForDelete);
        Mockito.doNothing().when(mListToCustomerRepository).deleteAll(anyList());
        when(mListToCustomerRepository.saveAll(anyList())).thenReturn(lListToCustomersForDelete);
        when(mOrderRepository.findAllByCustomerCustomerECMId(lCustomerECMId)).thenReturn(lOrders);
        when(mOrderRepository.saveAll(anyList())).thenReturn(lOrders);
        when(mListGroupRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lListGroups);
        when(mListGroupRepository.saveAll(anyList())).thenReturn(lListGroups);
        Mockito.doNothing().when(mCustomerRepository).deleteById(anyString());

        mCustomerMergeProcessor.mergeCustomer(lCustomer, lExistingCustomers);
        verify(mCustomerAccountProcessor, times(1)).resetCustomerAccountsData(anyString());
        verify(mCustomerRepository, times(1)).deleteById(anyString());
        verify(mListGroupRepository, times(1)).findByCustomerCustomerECMId(anyString());
        verify(mListGroupRepository, times(1)).saveAll(anyList());
        verify(mCustomerRepository, times(1)).deleteById(anyString());
    }

    @Test
    void testMergeCustomer_ForExistingCustomerInDb_WithEmpty_Contacts_Quotes_ListGroups_ListsToCustomer_Orders() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "      \"atgSystemSrcId\":\"24369121\",\r\n" + "      \"type\":\"Type A\" \r\n" + "        }\r\n" + "    ]");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId("243612222");
        List<AtgAccount> lExistingCustomers = new ArrayList<>();
        lExistingCustomers.add(lCustomerMessageVO.getAtgAccounts().get(0));

        List<Contact> lContacts = new ArrayList<>();
        List<Quote> lQuotes = new ArrayList<>();
        List<ListGroup> lListGroups = new ArrayList<>();
        List<ListToCustomer> lListToCustomersForDelete = new ArrayList<>();
        List<Order> lOrders = new ArrayList<>();

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.of(lCustomer));
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).resetCustomerAccountsData(anyString());
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mQuoteRepository.findByCustomerCustomerECMId(lCustomerECMId)).thenReturn(lQuotes);
        when(mListGroupRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lListGroups);
        when(mListToCustomerRepository.findByIdCustomerECMId(anyString())).thenReturn(lListToCustomersForDelete);
        when(mOrderRepository.findAllByCustomerCustomerECMId(lCustomerECMId)).thenReturn(lOrders);
        Mockito.doNothing().when(mCustomerRepository).deleteById(anyString());

        mCustomerMergeProcessor.mergeCustomer(lCustomer, lExistingCustomers);
        verify(mCustomerAccountProcessor, times(1)).resetCustomerAccountsData(anyString());
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
        verify(mOrderRepository, times(1)).findAllByCustomerCustomerECMId(anyString());
        verify(mCustomerRepository, times(1)).deleteById(anyString());
    }

    @Test
    void testMergeCustomer_ForExistingCustomerInDb_WithNull_Contacts_Quotes_ListGroups_ListsToCustomer_Orders() throws IOException {

        String lListenerMessege = Utils.readFile("customerPayload.json");
        lListenerMessege = lListenerMessege.replace("\"atgAccounts\": []", "\"atgAccounts\": [\r\n" + "        {\r\n"
                + "      \"atgSystemSrcId\":\"24369121\",\r\n" + "      \"type\":\"Type A\" \r\n" + "        }\r\n" + "    ]");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);
        String lCustomerECMId = lCustomerMessageVO.getCustomerEcmId();

        Customer lCustomer = new Customer();
        lCustomer.setCustomerECMId("243612222");
        List<AtgAccount> lExistingCustomers = new ArrayList<>();
        lExistingCustomers.add(lCustomerMessageVO.getAtgAccounts().get(0));

        List<Contact> lContacts = new ArrayList<>();
        List<Quote> lQuotes = null;
        List<ListGroup> lListGroups = null;
        List<ListToCustomer> lListToCustomersForDelete = null;
        List<Order> lOrders = null;

        when(mCustomerRepository.findById(anyString())).thenReturn(Optional.of(lCustomer));
        Mockito.doNothing().when(mCustomerResupplyRepository).deleteAllByCustomerECMId(anyString());
        Mockito.doNothing().when(mCustomerAccountProcessor).resetCustomerAccountsData(anyString());
        when(mContactRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lContacts);
        when(mQuoteRepository.findByCustomerCustomerECMId(lCustomerECMId)).thenReturn(lQuotes);
        when(mListGroupRepository.findByCustomerCustomerECMId(anyString())).thenReturn(lListGroups);
        when(mListToCustomerRepository.findByIdCustomerECMId(anyString())).thenReturn(lListToCustomersForDelete);
        when(mOrderRepository.findAllByCustomerCustomerECMId(lCustomerECMId)).thenReturn(lOrders);
        Mockito.doNothing().when(mCustomerRepository).deleteById(anyString());

        mCustomerMergeProcessor.mergeCustomer(lCustomer, lExistingCustomers);
        verify(mCustomerAccountProcessor, times(1)).resetCustomerAccountsData(anyString());
        verify(mContactRepository, times(1)).findByCustomerCustomerECMId(anyString());
        verify(mOrderRepository, times(1)).findAllByCustomerCustomerECMId(anyString());
        verify(mCustomerRepository, times(1)).deleteById(anyString());
    }
}
