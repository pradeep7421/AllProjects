package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.common.utils.UtilityFile;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AddressVO;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddressProcessorTest {
    @InjectMocks
    AddressProcessor mAddressProcessor;
    @Mock
    AddressRepository mAddressRepository;

    @Test
    void testImportAddressesData_WithNullAddressesVo() throws IOException {
        String lListenerMessege = UtilityFile.readFile("customerPayLoadWithNullAddressesVo.json");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(0)).save(any(Address.class));
    }

    @Test
    void testImportAddressesData_WithEmptyAddressesVo() throws IOException {
        String lListenerMessege = UtilityFile.readFile("customerPayLoadWithNullAddressesVo.json");
        lListenerMessege = lListenerMessege.replace("\"subAccountAddresses\": null", "\"subAccountAddresses\": []");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(0)).save(any(Address.class));
    }

    @Test
    void testImportAddressesData_WithNull_CustDefault_Shipping_And_BillingAddress() throws IOException {
        String lListenerMessege = UtilityFile.readFile("customerPayload.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);
        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddresses.get(0));

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void testImportAddressesData_WithNonNull_CustDefault_Shipping_And_BillingAddress() throws IOException {
        String lListenerMessege = UtilityFile.readFile("customerPayload.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);

        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();

        List<Address> lAddresses = new ArrayList<>();

        Customer lCustomer = new Customer();
        Address lShippingAddress = new Address();
        lShippingAddress.setId(101);
        Address lBillingAddress = new Address();
        lShippingAddress.setId(102);
        lCustomer.setDefaultShippingAddress(lShippingAddress);
        lCustomer.setDefaultBillingAddress(lBillingAddress);
        lAddresses.add(lShippingAddress);
        lAddresses.add(lBillingAddress);

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddresses.get(0));

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void testImportAddressesData_WithEmpty_ShipTo_ForSetBillingAddress() throws IOException {
        String lListenerMessege = UtilityFile.readFile("customerPayLoad.json");
        lListenerMessege = lListenerMessege.replace("\"type\": \"Ship to\"", "\"type\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddress.setId(101);
        lAddresses.add(lAddress);
        Customer lCustomer = new Customer();
        lCustomer.setDefaultShippingAddress(lAddress);
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddresses.get(0));

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void testImportAddressesData_WithTypeEmptyAddressVo_InAddressesVoList() throws IOException {
        String lListenerMessege = UtilityFile.readFile("customerPayloadWithEmptyAddressVoObjectWithNullValues.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lListenerMessege, CustomerMessageVO.class);
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddress.setId(101);
        lAddresses.add(lAddress);
        Customer lCustomer = new Customer();
        lCustomer.setDefaultShippingAddress(lAddress);
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(0)).save(any(Address.class));
    }
}
