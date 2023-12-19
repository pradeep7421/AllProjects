package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListGroup;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListToCustomer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Quote;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ListToCustomerId;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AtgAccount;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.CustomerResupplyRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ListGroupRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ListToCustomerRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.QuoteRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Customer merge processor
 *
 * @author Ankit Jain
 *
 */
@Component
@RequiredArgsConstructor
public class CustomerMergeProcessor {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final QuoteRepository mQuoteRepository;

    private final ListGroupRepository mListGroupRepository;

    private final ListToCustomerRepository mListToCustomerRepository;

    private final ContactRepository mContactRepository;

    private final CustomerRepository mCustomerRepository;

    private final CustomerResupplyRepository mCustomerResupplyRepository;

    private final CustomerAccountProcessor mCustomerAccountProcessor;

    /**
     * This method will merge the old customer data with new customer
     *
     * @param pNewCustomer       - the New Customer
     * @param pExistingCustomers - the Existing Customers
     */
    public void mergeCustomer(final Customer pNewCustomer, final List<AtgAccount> pExistingCustomers) {
        final List<String> lExistingCustomerECMIds = pExistingCustomers.stream().map(AtgAccount::getAtgSystemSrcId).toList();
        mLogger.debug("Processing Customer : {} -> lExistingCustomerECMIds : {}", pNewCustomer.getCustomerECMId(), lExistingCustomerECMIds);
        for (final String lExistingCustomerECMId : lExistingCustomerECMIds) {
            try {
                // delete old customer data
                final Optional<Customer> lExistingCustomerOpt = mCustomerRepository.findById(lExistingCustomerECMId);
                lExistingCustomerOpt.ifPresent(lExistingCustomer -> {
                    mCustomerResupplyRepository.deleteAllByCustomerECMId(lExistingCustomerECMId);
                    mCustomerAccountProcessor.resetCustomerAccountsData(lExistingCustomerECMId);

                    final List<Contact> lContacts = mContactRepository.findByCustomerCustomerECMId(lExistingCustomerECMId);
                    if (!CollectionUtils.isEmpty(lContacts)) {
                        associateContactsWithNewCustomer(pNewCustomer, lContacts);
                    }

                    // Check and Move existing quotes
                    checkAndMoveExistingQuotes(pNewCustomer, lExistingCustomerECMId);
                    // Check and Move existing list groups
                    checkAndMoveExistingListGroups(pNewCustomer, lExistingCustomerECMId);
                    // Check and Move existing shared list
                    checkAndMoveExistingSharedLists(pNewCustomer, lExistingCustomerECMId);

                    mCustomerRepository.deleteById(lExistingCustomerECMId);
                });

            } catch (final Exception lException) {
                mLogger.error("Exception while deleting lExistingCustomerECMId -> {}", lExistingCustomerECMId);
                throw lException;
            }
        }
    }

    /**
     * This method associates contacts with new customer
     *
     * @param pCustomer - the Customer
     * @param pContacts - the Contacts
     */
    private void associateContactsWithNewCustomer(final Customer pCustomer, final List<Contact> pContacts) {
        for (final Contact lContact : pContacts) {
            lContact.setCustomer(pCustomer);
        }
        mContactRepository.saveAll(pContacts);
    }

    /**
     * <b>checkAndMoveExistingQuotes</b> - Check and move existing quotes
     *
     * @param pCustomer              - the New Customer
     * @param pExistingCustomerECMId - the Existing Customer ECM Id
     */
    private void checkAndMoveExistingQuotes(final Customer pCustomer, final String pExistingCustomerECMId) {
        final List<Quote> lQuotes = mQuoteRepository.findByCustomerCustomerECMId(pExistingCustomerECMId);
        if (lQuotes != null && !lQuotes.isEmpty()) {
            for (final Quote lQuote : lQuotes) {
                lQuote.setCustomer(pCustomer);
                mLogger.debug("Moved Quote : {} from Customer : {} to {} ", lQuote.getQuoteId(), pExistingCustomerECMId,
                        pCustomer.getCustomerECMId());
            }
            mQuoteRepository.saveAll(lQuotes);
        }
    }

    /**
     * <b>checkAndMoveExistingListGroups</b> - Check and move existing list groups
     *
     * @param pCustomer              - the New Customer
     * @param pExistingCustomerECMId - the Existing Customer ECM Id
     */
    private void checkAndMoveExistingListGroups(final Customer pCustomer, final String pExistingCustomerECMId) {
        final List<ListGroup> lListGroups = mListGroupRepository.findByCustomerCustomerECMId(pExistingCustomerECMId);
        if (null != lListGroups && !lListGroups.isEmpty()) {
            for (final ListGroup lListGroup : lListGroups) {
                lListGroup.setCustomer(pCustomer);
                mLogger.debug("Moved List Group : {} from Customer : {} to {} ", lListGroup.getGroupId(), pExistingCustomerECMId,
                        pCustomer.getCustomerECMId());
            }
            mListGroupRepository.saveAll(lListGroups);
        }
    }

    /**
     * <b>checkAndMoveExistingSharedLists</b> - Check and move existing shared lists
     *
     * @param pCustomer              - the New Customer
     * @param pExistingCustomerECMId - the Existing Customer ECM Id
     */
    private void checkAndMoveExistingSharedLists(final Customer pCustomer, final String pExistingCustomerECMId) {
        final List<ListToCustomer> lListToCustomersForDelete = mListToCustomerRepository.findByIdCustomerECMId(pExistingCustomerECMId);

        if (null != lListToCustomersForDelete && !lListToCustomersForDelete.isEmpty()) {
            final List<Integer> lListIdsToMove = lListToCustomersForDelete.stream().map(lListToCustomer -> lListToCustomer.getId().getListId())
                    .toList();

            final List<ListToCustomer> lListToCustomersForAdd = createListToCustomers(lListIdsToMove, pCustomer.getCustomerECMId());

            mListToCustomerRepository.deleteAll(lListToCustomersForDelete);
            mListToCustomerRepository.saveAll(lListToCustomersForAdd);
        }
    }

    /**
     * This method create ListToCustomer Entity
     *
     * @param pListIds       - the list ids
     * @param pCustomerECMId - the Customer ECM id
     * @return List<ListToCustomer>
     */
    private List<ListToCustomer> createListToCustomers(final List<Integer> pListIds, final String pCustomerECMId) {
        return pListIds.stream().map(lListId -> {
            final ListToCustomer lListToCustomer = new ListToCustomer();
            final ListToCustomerId lListToCustomerId = new ListToCustomerId();
            lListToCustomerId.setListId(lListId);
            lListToCustomerId.setCustomerECMId(pCustomerECMId);
            lListToCustomer.setId(lListToCustomerId);
            return lListToCustomer;
        }).toList();
    }

}
