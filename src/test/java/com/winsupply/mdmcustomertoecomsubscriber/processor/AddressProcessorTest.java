package com.winsupply.mdmcustomertoecomsubscriber.processor;

import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO.AddressVO;
import com.winsupply.mdmcustomertoecomsubscriber.repositories.AddressRepository;
import com.winsupply.readfile.PayLoadReadFile;
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
    void testImportAddressesDataWithNullAddressVo() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithNullSubAccountAddress.json");

        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
    }

    @Test
    void testImportAddressesDataWithEmptyAddressVo() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithNullSubAccountAddress.json");
        lPayLoad = lPayLoad.replace("\"subAccountAddresses\": null", "\"subAccountAddresses\": []");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
    }

    @Test
    void testImportAddressesData() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);
        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddresses.get(0));

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
    }

    @Test
    void testImportAddressesData_WithSubAccAdddressAndEmptyAddressVo() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithAddressVOAndEmptySubAccountAddress.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);

        Customer lCustomer = new Customer();
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddresses.add(lAddress);

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
    }

    @Test
    void testImportAddressesData_WithNonNullAddress() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
    void testImportAddressesData_WithTypeEmptyInAddressVo() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"type\": \"Ship to\"", "\"type\": \"\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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

    @Test
    void testImportAddressesData_WithTypeBillToInAddressVo() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"type\": \"Ship to\"", "\"type\": \"Bill To\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
    void testImportAddressesData_WithNonNullBillingAddress() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");
        lPayLoad = lPayLoad.replace("\"type\": \"Ship to\"", "\"type\": \"Bill To\"");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
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
    void testImportAddressesData_WithTypeBillToAndShipTo() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoadWithTypeShipToAndBillTo.json");
        CustomerMessageVO lCustomerMessageVO = Utility.unmarshallData(lPayLoad, CustomerMessageVO.class);
        List<Address> lAddresses = new ArrayList<>();
        Address lAddress = new Address();
        lAddress.setId(101);
        lAddresses.add(lAddress);
        Customer lCustomer = new Customer();
        lCustomer.setDefaultBillingAddress(lAddress);
        List<AddressVO> lAddressesVO = lCustomerMessageVO.getWiseAccounts().get(0).getWiseSubAccounts().get(0).getSubAccountAddresses();

        when(mAddressRepository.save(any(Address.class))).thenReturn(lAddresses.get(0));

        mAddressProcessor.importAddressesData(lCustomer, lAddressesVO);
        verify(mAddressRepository, times(2)).save(any(Address.class));
    }
}
