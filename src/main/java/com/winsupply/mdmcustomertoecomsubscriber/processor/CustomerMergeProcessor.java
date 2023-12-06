package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListGroup;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ListToCustomer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Quote;
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
     * @param pCustomer    - the customer
     * @param pAtgAccounts - the ATG accounts
     */
    public void mergeCustomer(final Customer pCustomer, List<AtgAccount> pAtgAccounts) {
        List<String> lOldCustomerECMIds = pAtgAccounts.stream().map(AtgAccount::getAtgSystemSrcId).toList();

        if (!lOldCustomerECMIds.isEmpty()) {
            mLogger.debug("lOldCustomerECMIds : {}", lOldCustomerECMIds);
            for (final String lOldCustomerECMId : lOldCustomerECMIds) {
                try {
                    // delete old customer data
                    Optional<Customer> lOldCustomerOpt = mCustomerRepository.findById(lOldCustomerECMId);
                    lOldCustomerOpt.ifPresent(lOldCustomer -> {
                        mCustomerResupplyRepository.deleteAllByCustomerECMId(lOldCustomerECMId);
                        mCustomerAccountProcessor.resetCustomerAccountsData(lOldCustomerECMId);
                        mCustomerAccountProcessor.deleteCustomerAddress(lOldCustomer);
                        mCustomerRepository.save(lOldCustomer);

                        List<Contact> lContacts = mContactRepository.findByCustomerCustomerECMId(lOldCustomerECMId);
                        if (!CollectionUtils.isEmpty(lContacts)) {
                            associateContactsWithNewCustomer(pCustomer, lContacts);
                        }

                        // Check and Move existing quotes
                        checkAndMoveExistingQuotes(pCustomer, lOldCustomerECMId);
                        // Check and Move existing list group
                        checkAndMoveExistingListGroups(pCustomer, lOldCustomerECMId);
                        // Check and Move existing list
                        checkAndMoveExistingLists(pCustomer, lOldCustomerECMId);

                        mCustomerRepository.deleteById(lOldCustomerECMId);
                    });
                } catch (final Exception lRepoException) {
                    mLogger.error("Exception while deleting lOldCustomerECMId -> {}", lOldCustomerECMId);
                    throw lRepoException;
                }
            }
        }

    }

    /**
     * This method associates contacts with new customer
     *
     * @param pCustomer - the Customer
     * @param pContacts - the Contacts
     */
    private void associateContactsWithNewCustomer(final Customer pCustomer, List<Contact> pContacts) {
        for (final Contact lContact : pContacts) {
            lContact.setCustomer(pCustomer);
        }
        mContactRepository.saveAll(pContacts);
    }

    /**
     * <b>checkAndMoveExistingQuotes</b> - Check and move existing quotes
     *
     * @param pCustomer         - the New Customer
     * @param pOldCustomerECMId - the Old Customer ECM Id
     */
    private void checkAndMoveExistingQuotes(final Customer pCustomer, final String pOldCustomerECMId) {
        final List<Quote> lQuotes = mQuoteRepository.findByCustomerCustomerECMId(pOldCustomerECMId);
        if (lQuotes != null && !lQuotes.isEmpty()) {
            for (final Quote lQuoteItem : lQuotes) {
                lQuoteItem.setCustomer(pCustomer);
                mLogger.info("Moved Quote : {} to Customer : {} ", lQuoteItem.getQuoteId(), pCustomer.getCustomerECMId());
            }
            mQuoteRepository.saveAll(lQuotes);
        }
    }

    /**
     * <b>checkAndMoveExistingListGroups</b> - Check and move existing list groups
     *
     * @param pCustomer         - the New Customer
     * @param pOldCustomerECMId - the Old Customer ECM Id
     */
    private void checkAndMoveExistingListGroups(final Customer pCustomer, final String pOldCustomerECMId) {
        List<ListGroup> lListGroups = mListGroupRepository.findByCustomerCustomerECMId(pOldCustomerECMId);
        if (null != lListGroups && !lListGroups.isEmpty()) {
            for (final ListGroup lListGroupItem : lListGroups) {
                lListGroupItem.setCustomer(pCustomer);
                mLogger.info("Moved List Group : {} to Customer : {}", lListGroupItem.getGroupId(), pCustomer.getCustomerECMId());
            }
            mListGroupRepository.saveAll(lListGroups);
        }
    }

    /**
     * <b>checkAndMoveExistingLists</b> - Check and move existing lists
     *
     * @param pCustomer         - the New Customer
     * @param pOldCustomerECMId - the Old Customer ECM Id
     */
    private void checkAndMoveExistingLists(final Customer pCustomer, final String pOldCustomerECMId) {
        List<ListToCustomer> lListToCustomers = mListToCustomerRepository.findByIdCustomerECMId(pOldCustomerECMId);

        if (null != lListToCustomers && !lListToCustomers.isEmpty()) {
            lListToCustomers.forEach(lListToCustomer -> {
                lListToCustomer.getId().setCustomerECMId(pCustomer.getCustomerECMId());
                mLogger.info("Moved List : {} to Customer : {}", lListToCustomer.getId().getListId(), lListToCustomer.getId().getCustomerECMId());
            });
            mListToCustomerRepository.saveAll(lListToCustomers);
        }
    }
}
