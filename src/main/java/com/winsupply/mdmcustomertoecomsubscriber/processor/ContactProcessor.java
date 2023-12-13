package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactEmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactIndustryPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactRole;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.EmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Industry;
import com.winsupply.mdmcustomertoecomsubscriber.entities.OrderEmailAddress;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Phone;
import com.winsupply.mdmcustomertoecomsubscriber.entities.PhoneNumberType;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactEmailPreferenceId;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactIndustryPreferenceId;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.Contact.ContactEmail;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactEmailPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactIndustryPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.ContactRoleRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.EmailPreferenceRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.IndustryRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.OrderEmailAddressRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.PhoneRepository;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.PhoneTypeRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Contact data processor
 *
 * @author Ankit Jain
 *
 */
@Component
@RequiredArgsConstructor
public class ContactProcessor {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final ContactRepository mContactRepository;

    private final ContactRoleRepository mContactRoleRepository;

    private final ContactEmailPreferenceRepository mContactEmailPreferenceRepository;

    private final ContactIndustryPreferenceRepository mContactIndustryPreferenceRepository;

    private final PhoneRepository mPhoneRepository;

    private final OrderEmailAddressRepository mOrderEmailAddressRepository;

    private final AddressRepository mAddressRepository;

    private final PhoneTypeRepository mPhoneTypeRepository;

    private final IndustryRepository mIndustryRepository;

    private final EmailPreferenceRepository mEmailPreferenceRepository;

    /**
     * <b>createOrUpdateContacts</b> - It creates or updates the customer contacts
     *
     * @param pCustomer          - the Customer
     * @param pCustomerMessageVO - the CustomerMessage
     */
    public void createOrUpdateContacts(final Customer pCustomer, final CustomerMessageVO pCustomerMessageVO) {
        List<CustomerMessageVO.Contact> lContacts = pCustomerMessageVO.getContacts();
        for (CustomerMessageVO.Contact lContactVO : lContacts) {
            final String lUserId = lContactVO.getUserId();
            if (StringUtils.hasText(lUserId) && Utility.isValidEmail(lUserId)) {
                processContactData(pCustomer, pCustomerMessageVO, lContactVO);
            } else {
                mLogger.debug("Skipping Contact with ContactECMId : {}, due to invalid userId : {}", lContactVO.getContactEcmId(), lUserId);
            }
        }
    }

    /**
     * <b>processContactData</b> - it process the contact's data
     *
     * @param pCustomer - the Customer
     * @param pCustomerMessageVO - the Customer Message
     * @param pContactVO - Contact
     */
    private void processContactData(final Customer pCustomer, final CustomerMessageVO pCustomerMessageVO,
            final CustomerMessageVO.Contact pContactVO) {
        final String lFirstName = pContactVO.getFirstName();
        final String lLastName = pContactVO.getLastName();
        if (!StringUtils.hasText(lFirstName) || !StringUtils.hasText(lLastName)) {
            mLogger.error("First Name or Last Name is missing for contact with id : {}", pContactVO.getContactEcmId());
        } else {
            // TODO - need to delete contact based on contactAtgAccounts
            Optional<Contact> lContactOpt = mContactRepository.findByLogin(pContactVO.getUserId());
            Contact lContactEntity;
            if (lContactOpt.isPresent()) {
                lContactEntity = lContactOpt.get();
                mLogger.debug("Contact with contactECMId: {} is found in DB", lContactEntity.getContactECMId());

                if ("N".equalsIgnoreCase(pContactVO.getContactECommerceStatus())) {
                    lContactEntity.setEcmActive((short) 0);
                    mContactRepository.save(lContactEntity);
                    return;
                }
            } else {
                lContactEntity = new Contact();
                lContactEntity.setLogin(pContactVO.getUserId());
                lContactEntity.setContactECMId(pContactVO.getContactEcmId());
            }

            if (StringUtils.hasText(pCustomerMessageVO.getInterCompanyId())) {
                mLogger.debug("Setting role as lcAdmin for LC Customer Feed");
                pContactVO.setRole(Constants.LC_ADMIN_ROLE);
            }

            lContactEntity.setCustomer(pCustomer);
            populateRole(lContactEntity, pContactVO);
            lContactEntity = mContactRepository.save(lContactEntity);
            importContactData(lContactEntity, pContactVO);
        }
    }

    /**
     * It imports the contact data
     *
     * @param pContactEntity - the Contact Entity
     * @param pContact       - the ContactVO
     *
     */
    private void importContactData(final Contact pContactEntity, final CustomerMessageVO.Contact pContact) {
        pContactEntity.setFirstName(pContact.getFirstName());
        pContactEntity.setLastName(pContact.getLastName());
        pContactEntity.setEcmActive((short) 1);

        mContactEmailPreferenceRepository.deleteAllByIdContactEcmId(pContactEntity.getContactECMId());
        mContactIndustryPreferenceRepository.deleteAllByIdContactEcmId(pContactEntity.getContactECMId());
        if (null != pContactEntity.getAddress()) {
            mPhoneRepository.deleteAllByAddressId(pContactEntity.getAddress().getId());
            mOrderEmailAddressRepository.deleteAllByAddressId(pContactEntity.getAddress().getId());
        }

        Set<ContactEmailPreference> lContactEmailPreferenceSet = createContactEmailPreferences(pContact);
        Set<ContactIndustryPreference> lContactIndustryPreferenceSet = createContactIndustryPreferences(pContact);
        Set<Phone> lPhoneNumbersSet = createPhones(pContactEntity, pContact);
        Set<com.winsupply.mdmcustomertoecomsubscriber.entities.OrderEmailAddress> lOrderEmailAddressSet = createOrderEmailAddress(pContactEntity,
                pContact);

        if (!lContactEmailPreferenceSet.isEmpty()) {
            mContactEmailPreferenceRepository.saveAll(lContactEmailPreferenceSet);
        }
        if (!lContactIndustryPreferenceSet.isEmpty()) {
            mContactIndustryPreferenceRepository.saveAll(lContactIndustryPreferenceSet);
        }
        if (!lPhoneNumbersSet.isEmpty()) {
            mPhoneRepository.saveAll(lPhoneNumbersSet);
        }
        if (!CollectionUtils.isEmpty(lOrderEmailAddressSet)) {
            mOrderEmailAddressRepository.saveAll(lOrderEmailAddressSet);
        }

    }

    /**
     * <b>createContactEmailPreferences</b> - it creates the email preference
     *
     * @param pContactVO - the Contact VO
     * @return - Set<ContactEmailPreference>
     */
    private Set<ContactEmailPreference> createContactEmailPreferences(final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getCommunicationPreference() && !pContactVO.getCommunicationPreference().isEmpty()) {
            return pContactVO.getCommunicationPreference().stream().map(lPreferences -> {
                Optional<EmailPreference> lPreferenceOpt = mEmailPreferenceRepository.findByEmailPreferenceDesc(lPreferences);
                if (lPreferenceOpt.isPresent()) {
                    EmailPreference lEmailPreference = lPreferenceOpt.get();
                    ContactEmailPreference lContactEmailPreference = new ContactEmailPreference();
                    ContactEmailPreferenceId lContactEmailPreferenceId = new ContactEmailPreferenceId();
                    lContactEmailPreferenceId.setContactEcmId(pContactVO.getContactEcmId());
                    lContactEmailPreferenceId.setEmailPreferenceId(lEmailPreference.getEmailPreferenceId());
                    lContactEmailPreference.setId(lContactEmailPreferenceId);
                    return lContactEmailPreference;
                }
                return null;
            }).collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * <b>populateRole</b> - It populates the contact role
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - the Contact VO
     */
    private void populateRole(final Contact pContactEntity, final CustomerMessageVO.Contact pContactVO) {
        Integer lRoleId;
        if (StringUtils.hasText(pContactVO.getRole())) {
            lRoleId = Utility.getContactRole(pContactVO.getRole());
        } else {
            lRoleId = Utility.getContactRole(Constants.PROCUREMENT_MANAGER_ROLE);
        }

        Optional<ContactRole> lRoleOpt = mContactRoleRepository.findById(lRoleId);
        lRoleOpt.ifPresent(pContactEntity::setRole);
    }

    /**
     * <b>createOrderEmailAddress</b> - it creates the list of OrderEmailAddresses
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - Contact VO
     * @return- Set<OrderEmailAddress>
     */
    private Set<OrderEmailAddress> createOrderEmailAddress(final Contact pContactEntity, final CustomerMessageVO.Contact pContactVO) {
        Set<OrderEmailAddress> lOrderEmailAddresses = null;
        if (null != pContactVO.getContactEmails() && !pContactVO.getContactEmails().isEmpty()) {
            lOrderEmailAddresses = new HashSet<>();
            for (final ContactEmail lContactEmail : pContactVO.getContactEmails()) {
                switch (lContactEmail.getEmailType()) {
                    case Constants.ON_EMAIL_TYPE:
                        Address lAddress;
                        if (null == pContactEntity.getAddress()) {
                            lAddress = new Address();
                            lAddress = mAddressRepository.save(lAddress);
                            pContactEntity.setAddress(lAddress);
                        } else {
                            lAddress = pContactEntity.getAddress();
                        }

                        OrderEmailAddress lOrderEmailAddress = new OrderEmailAddress();
                        lOrderEmailAddress.setAddressId(lAddress.getId());
                        lOrderEmailAddress.setOrderEmailAddress(lContactEmail.getEmailAddress());
                        lOrderEmailAddresses.add(lOrderEmailAddress);
                        break;
                    case Constants.EW_EMAIL_TYPE:
                        pContactEntity.setEmail(lContactEmail.getEmailAddress());
                        break;
                    default:
                        break;
                }
            }
        }

        return lOrderEmailAddresses;
    }

    /**
     * <b>createPhones</b> - it creates the list of phones
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - the Contact VO
     * @return - Set<Phone>
     */
    private Set<Phone> createPhones(final Contact pContactEntity, final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getContactPhones() && !pContactVO.getContactPhones().isEmpty()) {
            return pContactVO.getContactPhones().stream().map(lPhone -> {
                Phone lPhoneEntity = new Phone();
                if (null == pContactEntity.getAddress()) {
                    Address lAddress = new Address();
                    lAddress = mAddressRepository.save(lAddress);
                    pContactEntity.setAddress(lAddress);
                    lPhoneEntity.setAddress(lAddress);
                } else {
                    lPhoneEntity.setAddress(pContactEntity.getAddress());
                }
                lPhoneEntity.setPhoneNumber(lPhone.getPhoneNumber());
                Optional<PhoneNumberType> lPhoneNumberTypeOpt = mPhoneTypeRepository
                        .findByPhoneNumberTypeDesc(Utility.getPhoneNumberType(lPhone.getPhoneType()));
                lPhoneNumberTypeOpt.ifPresent(lPhoneEntity::setPhoneNumberType);
                return lPhoneEntity;
            }).collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * <b>createContactIndustryPreferences</b> - It creates the list of industry
     * preferences
     *
     * @param pContactVO - the Contact VO
     * @return - Set<ContactIndustryPreference>
     */
    private Set<ContactIndustryPreference> createContactIndustryPreferences(final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getIndustries() && !pContactVO.getIndustries().isEmpty()) {
            return Arrays.stream(pContactVO.getIndustries().split(",")).map(lEmailPreference -> {
                Optional<Industry> lIndustryOpt = mIndustryRepository.findByIndustryDesc(lEmailPreference);
                if (lIndustryOpt.isPresent()) {
                    ContactIndustryPreference lContactIndustryPreference = new ContactIndustryPreference();
                    ContactIndustryPreferenceId lContactIndustryPreferenceId = new ContactIndustryPreferenceId();
                    lContactIndustryPreferenceId.setContactEcmId(pContactVO.getContactEcmId());
                    lContactIndustryPreferenceId.setIndustryId(lIndustryOpt.get().getIndustryId());
                    lContactIndustryPreference.setId(lContactIndustryPreferenceId);
                    return lContactIndustryPreference;
                }
                return null;
            }).collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
}
