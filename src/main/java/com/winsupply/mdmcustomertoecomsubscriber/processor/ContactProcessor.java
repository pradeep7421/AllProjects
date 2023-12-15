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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
        final List<CustomerMessageVO.Contact> lContacts = pCustomerMessageVO.getContacts();
        // TODO - where is the empty check and deletes
        for (final CustomerMessageVO.Contact lContactVO : lContacts) {
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
            mLogger.error("CustomerECMId : {} - First Name or Last Name is missing for contact with id : {}", pCustomer.getCustomerECMId(),
                    pContactVO.getContactEcmId());
        } else {
            // TODO - need to delete contact based on contactAtgAccounts
            final Optional<Contact> lContactOpt = mContactRepository.findByLoginIgnoreCase(pContactVO.getUserId());
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
                lContactEntity.setRegistrationDate(LocalDateTime.now());
            }
            if (StringUtils.hasText(pCustomerMessageVO.getInterCompanyId())) {
                mLogger.debug("CustomerECMId : {} - Setting role as lcAdmin for LC Customer", pCustomer.getCustomerECMId());
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

        createContactEmailPreferences(pContact);
        createContactIndustryPreferences(pContact);
        createPhones(pContactEntity, pContact);
        createEmailAddresses(pContactEntity, pContact);
    }

    /**
     * <b>createContactEmailPreferences</b> - it creates the email preference
     *
     * @param pContactVO - the Contact VO
     * @return - Set<ContactEmailPreference>
     */
    private void createContactEmailPreferences(final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getCommunicationPreference() && !pContactVO.getCommunicationPreference().isEmpty()) {
            final Set<ContactEmailPreference> lContactEmailPreferenceSet = pContactVO.getCommunicationPreference().stream().map(lPreference -> {
                final Optional<EmailPreference> lPreferenceOpt = mEmailPreferenceRepository.findByEmailPreferenceDescIgnoreCase(lPreference);
                if (lPreferenceOpt.isPresent()) {
                    final ContactEmailPreference lContactEmailPreference = new ContactEmailPreference();
                    final ContactEmailPreferenceId lContactEmailPreferenceId = new ContactEmailPreferenceId();
                    lContactEmailPreferenceId.setContactEcmId(pContactVO.getContactEcmId());
                    lContactEmailPreferenceId.setEmailPreferenceId(lPreferenceOpt.get().getEmailPreferenceId());
                    lContactEmailPreference.setId(lContactEmailPreferenceId);
                    return lContactEmailPreference;
                }
                return null;
            }).collect(Collectors.toSet());

            mContactEmailPreferenceRepository.saveAll(lContactEmailPreferenceSet);
        }
    }

    /**
     * <b>populateRole</b> - It populates the contact role
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - the Contact VO
     */
    private void populateRole(final Contact pContactEntity, final CustomerMessageVO.Contact pContactVO) {
        Integer lRoleId = null;
        if (StringUtils.hasText(pContactVO.getRole())) {
            lRoleId = Utility.getContactRole(pContactVO.getRole().replaceAll("\\s", "").toLowerCase());
        }
        if (lRoleId == null) {
            lRoleId = Utility.getContactRole(Constants.PROCUREMENT_MANAGER_ROLE);
        }
        final Optional<ContactRole> lRoleOpt = mContactRoleRepository.findById(lRoleId);
        lRoleOpt.ifPresent(pContactEntity::setRole);
    }

    /**
     * <b>createEmailAddresses</b> - it creates the list of Email Addresses
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - Contact VO
     */
    private void createEmailAddresses(final Contact pContactEntity, final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getContactEmails() && !pContactVO.getContactEmails().isEmpty()) {
            final Set<OrderEmailAddress> lOrderEmailAddresses = new HashSet<>();
            for (final ContactEmail lContactEmail : pContactVO.getContactEmails()) {
                switch (lContactEmail.getEmailType()) {
                    case Constants.ON_EMAIL_TYPE:
                        Address lAddressEntity;
                        if (null == pContactEntity.getAddress()) {
                            lAddressEntity = new Address();
                            lAddressEntity = mAddressRepository.save(lAddressEntity);
                            pContactEntity.setAddress(lAddressEntity);
                        } else {
                            lAddressEntity = pContactEntity.getAddress();
                        }

                        final OrderEmailAddress lOrderEmailAddress = new OrderEmailAddress();
                        lOrderEmailAddress.setAddressId(lAddressEntity.getId());
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
            mOrderEmailAddressRepository.saveAll(lOrderEmailAddresses);
        }
    }

    /**
     * <b>createPhones</b> - it creates the list of phones
     *
     * @param pContactEntity - the Contact Entity
     * @param pContactVO     - the Contact VO
     */
    private void createPhones(final Contact pContactEntity, final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getContactPhones() && !pContactVO.getContactPhones().isEmpty()) {
            final Set<Phone> lPhoneNumbersSet = pContactVO.getContactPhones().stream().map(lPhone -> {
                final Phone lPhoneEntity = new Phone();
                if (null == pContactEntity.getAddress()) {
                    Address lAddress = new Address();
                    lAddress = mAddressRepository.save(lAddress);
                    pContactEntity.setAddress(lAddress);
                    lPhoneEntity.setAddress(lAddress);
                } else {
                    lPhoneEntity.setAddress(pContactEntity.getAddress());
                }
                lPhoneEntity.setPhoneNumber(lPhone.getPhoneNumber());
                final Optional<PhoneNumberType> lPhoneNumberTypeOpt = mPhoneTypeRepository
                        .findByPhoneNumberTypeDesc(Utility.getPhoneNumberType(lPhone.getPhoneType()));
                lPhoneNumberTypeOpt.ifPresent(lPhoneEntity::setPhoneNumberType);
                return lPhoneEntity;
            }).collect(Collectors.toSet());

            mPhoneRepository.saveAll(lPhoneNumbersSet);
        }
    }

    /**
     * <b>createContactIndustryPreferences</b> - It creates the list of industry
     * preferences
     *
     * @param pContactVO - the Contact VO
     */
    private void createContactIndustryPreferences(final CustomerMessageVO.Contact pContactVO) {
        if (null != pContactVO.getIndustries() && !pContactVO.getIndustries().isEmpty()) {
            final Set<ContactIndustryPreference> lContactIndustryPreferenceSet = Arrays.stream(pContactVO.getIndustries().split(","))
                    .map(lEmailPreference -> {
                        final Optional<Industry> lIndustryOpt = mIndustryRepository.findByIndustryDescIgnoreCase(lEmailPreference);
                        if (lIndustryOpt.isPresent()) {
                            final ContactIndustryPreference lContactIndustryPreference = new ContactIndustryPreference();
                            final ContactIndustryPreferenceId lContactIndustryPreferenceId = new ContactIndustryPreferenceId();
                            lContactIndustryPreferenceId.setContactEcmId(pContactVO.getContactEcmId());
                            lContactIndustryPreferenceId.setIndustryId(lIndustryOpt.get().getIndustryId());
                            lContactIndustryPreference.setId(lContactIndustryPreferenceId);
                            return lContactIndustryPreference;
                        }
                        return null;
                    }).collect(Collectors.toSet());
            mContactIndustryPreferenceRepository.saveAll(lContactIndustryPreferenceSet);
        }
    }
}
