package com.winsupply.mdmcustomertoecomsubscriber.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * Base class to unmarshall customer message
 *
 * @author Amritanshu
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class CustomerMessageVO {

    private String customerEcmId;

    private String fullName;

    private String interCompanyId;

    private String proRewardsId;

    private List<String> vmiLocations;

    private String winCCA;

    private List<FederalId> federalIds;

    private List<Email> emails;

    private List<Phone> phones;

    private List<Address> addresses;

    private List<Contact> contacts;

    private List<Account> wiseAccounts;

    private List<AtgAccount> atgAccounts;

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class FederalId {

        private String federalId;

        private String taxIdType;
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class Email {

        private String emailAddress;

        private String emailType;
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class Phone {

        private String phoneNumber;

        private String phoneExtension;

        private String phoneType;
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class Address {

        private String addressLine1;

        private String addressLine2;

        private String city;

        private String state;

        private String postalCode;

        private String type;
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class Contact {

        private String contactEcmId;

        private String firstName;

        private String lastName;

        private String userId;

        private String contactECommerceStatus;

        private Set<String> communicationPreference = null;

        private String jobTitle;

        private String role;

        private String industries;

        private List<ContactEmail> contactEmails;

        private List<Phone> contactPhones;

        @Data
        @JsonInclude(Include.NON_NULL)
        public static class ContactEmail {
            private String emailAddress;
            private String emailType;
            private String optOutInd;
            private String preferenceLevel;
            private String comments;
        }
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class Account {

        private String accountNumber;

        private String companyNumber;

        private String role;

        private String interCompanyCode;

        private String accountEcommerceStatus;

        private String fullName;

        private String status;

        @JsonProperty("isFavorite")
        private String favorite;

        private AccountDetail accountDetail;

        private List<SubAccount> wiseSubAccounts;

        @JsonProperty("addresses")
        private List<Address> addresses;

        @Data
        @JsonInclude(Include.NON_NULL)
        public static class AccountDetail {

            private String creditStatusCode;

            private String proRewardsId;

            private String creditLimit;

            private String creditDiscountEligible;

            private String freightPercent;

            private String freightCost;

            private String poReqCode;

            private String billToAccount;

            private String cashSale;
        }

        @Data
        @JsonInclude(Include.NON_NULL)
        public static class SubAccount {

            private String fullName;

            private String subAccountNumber;

            private String status;

            private String role;

            private SubAccountDetail subAccountDetail;

            private List<Address> subAccountAddresses;

            @Data
            @JsonInclude(Include.NON_NULL)
            public static class SubAccountDetail {

                private String proRewardsId;

                private String creditLimit;

                private String creditStatusCode;

                private String secondarySalesPerson;

                private String poReqCode;

                private String freightPercent;

                private String freightCost;
            }
        }
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class AtgAccount {

        private String atgSystemSrcId;

        private String type;
    }
}
