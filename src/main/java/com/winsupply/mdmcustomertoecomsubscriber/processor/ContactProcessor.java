package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactEmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactIndustryPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactRole;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.EmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Industry;
import com.winsupply.mdmcustomertoecomsubscriber.entities.OrderEmailAddress;
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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public void createOrUpdateContacts(Customer pCustomer, CustomerMessageVO pCustomerMessageVO) {
        List<CustomerMessageVO.Contact> lContacts = pCustomerMessageVO.getContacts();
        for (CustomerMessageVO.Contact lContactVO : lContacts) {
            final String lUserId = lContactVO.getUserId();
            if (StringUtils.hasText(lUserId) && Utility.isValidEmail(lUserId)) {
                final String lFirstName = lContactVO.getFirstName();
                final String lLastName = lContactVO.getLastName();
                if (!StringUtils.hasText(lFirstName) || !StringUtils.hasText(lLastName)) {
                    mLogger.error("First Name or Last Name is missing for contact with id : {}", lContactVO.getContactEcmId());
                } else {
                    // TODO - need to delete contact based on contactAtgAccounts
                    Optional<Contact> lContactOpt = mContactRepository.findByLogin(lUserId);
                    Contact lContactEntity;
                    if (lContactOpt.isPresent()) {
                        lContactEntity = lContactOpt.get();
                    } else {
                        lContactEntity = new Contact();
                        lContactEntity.setLogin(lUserId);
                        lContactEntity.setContactECMId(lContactVO.getContactEcmId());
                    }

                    // Reset the contact data here

                    if ("N".equalsIgnoreCase(lContactVO.getContactECommerceStatus())) {
                        lContactEntity.setEcmActive((short) 0);
                        continue;
                    } else {
                        lContactEntity.setEcmActive((short) 1);
                    }

                    if (!StringUtils.hasText(pCustomerMessageVO.getInterCompanyId())) {
                        mLogger.debug("Setting role as lcAdmin for LC Customer Feed");
                        lContactVO.setRole(Constants.LC_ADMIN_ROLE);
                    }

                    lContactEntity.setCustomer(pCustomer);
                    importContactData(lContactEntity, lContactVO);
                }
            } else {
                mLogger.debug("Skipping Contact with ContactECMId : {}, due to invalid userId : {}", lContactVO.getContactEcmId(), lUserId);
            }
        }
    }

    /**
     * It imports the contact data
     *
     * @param lContactEntity - the Contact Entity
     * @param lContact       - the ContactVO
     *
     */
    private void importContactData(Contact lContactEntity, CustomerMessageVO.Contact lContact) {
        lContactEntity.setFirstName(lContact.getFirstName());
        lContactEntity.setLastName(lContact.getLastName());
        lContactEntity.setEcmActive((short) 1);

        Set<ContactEmailPreference> lContactEmailPreferenceSet = createContactEmailPreferences(lContact);
        populateRole(lContactEntity, lContact);
        Set<ContactIndustryPreference> lContactIndustryPreferenceSet = createContactIndustryPreferences(lContact);
        Set<com.winsupply.mdmcustomertoecomsubscriber.entities.Phone> lPhoneNumbersSet = createPhones(lContactEntity, lContact);
        Set<com.winsupply.mdmcustomertoecomsubscriber.entities.OrderEmailAddress> lOrderEmailAddressSet = createOrderEmailAddress(lContactEntity,
                lContact);

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
        mContactRepository.save(lContactEntity);
    }

    /**
     * <b>createContactEmailPreferences</b> - it creates the email preference
     *
     * @param pContactVO - the Contact VO
     * @return - Set<ContactEmailPreference>
     */
    private Set<ContactEmailPreference> createContactEmailPreferences(CustomerMessageVO.Contact pContactVO) {
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
     * @param lContactEntity - the Contact Entity
     * @param lContactVO     - the Contact VO
     */
    private void populateRole(Contact lContactEntity, CustomerMessageVO.Contact lContactVO) {
        Integer lRoleId;
        if (StringUtils.hasText(lContactVO.getRole())) {
            lRoleId = Utility.getContactRole(lContactVO.getRole());
        } else {
            lRoleId = Utility.getContactRole(Constants.PROCUREMENT_MANAGER_ROLE);
        }

        Optional<ContactRole> lRoleOpt = mContactRoleRepository.findById(lRoleId);
        lRoleOpt.ifPresent(lContactEntity::setRole);
    }

    /**
     * <b>createOrderEmailAddress</b> - it creates the list of OrderEmailAddresses
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - Contact VO
     * @return- Set<OrderEmailAddress>
     */
    private Set<OrderEmailAddress> createOrderEmailAddress(Contact pContactEntity, CustomerMessageVO.Contact pContactVO) {
        Set<OrderEmailAddress> lOrderEmailAddresses = null;
        if (null != pContactVO.getContactEmails() && !pContactVO.getContactEmails().isEmpty()) {
            lOrderEmailAddresses = new HashSet<>();
            // TODO - check with Amritanshu for login improvement with property
            // isPreferredContactMethod
            for (final ContactEmail lContactEmail : pContactVO.getContactEmails()) {
                switch (lContactEmail.getEmailType()) {
                case Constants.ON_EMAIL_TYPE:
                    OrderEmailAddress lOrderEmailAddress = new OrderEmailAddress();
                    lOrderEmailAddress.setAddressId(pContactEntity.getAddress().getId());
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
     * @return - Set<com.winsupply.mdmcustomertoecomsubscriber.entities.Phone>
     */
    private Set<com.winsupply.mdmcustomertoecomsubscriber.entities.Phone> createPhones(Contact pContactEntity, CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getContactPhones() && !pContactVO.getContactPhones().isEmpty()) {
            return pContactVO.getContactPhones().stream().map(lPhone -> {
                com.winsupply.mdmcustomertoecomsubscriber.entities.Phone lPhoneEntity = new com.winsupply.mdmcustomertoecomsubscriber.entities.Phone();
                if (null == pContactEntity.getAddress()) {
                    com.winsupply.mdmcustomertoecomsubscriber.entities.Address lAddress = new com.winsupply.mdmcustomertoecomsubscriber.entities.Address();
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
    private Set<ContactIndustryPreference> createContactIndustryPreferences(CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getIndustries() && !pContactVO.getIndustries().isEmpty()) {
            return Arrays.stream(pContactVO.getIndustries().split(Constants.COMMA)).map(lEmailPreference -> {
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
