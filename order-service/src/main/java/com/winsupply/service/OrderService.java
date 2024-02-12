package com.winsupply.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.winsupply.constants.Constants;
import com.winsupply.entity.Order;
import com.winsupply.entity.OrderLine;
import com.winsupply.globalexception.DataNotFoundException;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.model.OrderRequest;
import com.winsupply.repository.OrderLineRepository;
import com.winsupply.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * The {@code OrderService} class provides business logic for managing orders
 * 
 * @author PRADEEP
 */
@Service
public class OrderService {

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * mOrderRepository - the OrderRepository
     */
    private final OrderRepository mOrderRepository;

    /**
     * mOrderLineRepository - the OrderLineRepository
     */
    private final OrderLineRepository mOrderLineRepository;

    private final PromotionService mPromotionService;

    /**
     * Creates a new instance of the OrderService
     *
     * @param - pOrderRepository The repository for accessing orders
     * @param - pOrderLineRepository The repository for accessing order lines
     */
    public OrderService(OrderRepository pOrderRepository, OrderLineRepository pOrderLineRepository, PromotionService pPromotionService) {
        this.mOrderRepository = pOrderRepository;
        this.mOrderLineRepository = pOrderLineRepository;
        this.mPromotionService = pPromotionService;

    }

    /**
     * Creates a new order based on the provided order request
     *
     * @param pOrderRequest - The request containing order information
     * @throws Exception
     */
    public void createOrder(OrderRequest pOrderRequest) {

        mLogger.debug("pOrderRequest -> Order Name: {}, Order Amount: {}", pOrderRequest.getOrderName(), pOrderRequest.getAmount());

        Order lOrder = createOrderEntity(pOrderRequest);

        List<OrderLine> lOrderLines = createOrderlines(pOrderRequest, lOrder);

        lOrder.setOrderLines(lOrderLines);

        mOrderRepository.save(lOrder);
        mOrderLineRepository.saveAll(lOrderLines);

        mLogger.debug("Exiting createOrder method");
    }

    /**
     * Retrieves the details of an order by its ID
     *
     * @param pOrderId - The unique value of the order
     * @return - The order details or throws DataNotFoundException if the order is
     *         not found
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    // TODO- @Transactional
    public Optional<Order> getOrderDetails(int pOrderId, String pUserAgent) throws JsonMappingException, JsonProcessingException {

        mLogger.debug("pOrderId -> Order Id: {}", pOrderId);

        Optional<Order> lOrderOptional = mOrderRepository.findById(pOrderId);

        if (lOrderOptional.isPresent()) {
//            Order lOrder = lOrderOptional.get();
//            ResponseEntity<String> lResponseEntity = mPromotionService.getPromotionDetails(pUserAgent, lOrder.getAmount());
//
//            PromotionResponse lPromotionResponse = new ObjectMapper().readValue(lResponseEntity.getBody(), PromotionResponse.class);
//            Double lAmountBeforeDiscount = lOrder.getAmount();
//            Double lAmountAfterDiscount = lAmountBeforeDiscount - lPromotionResponse.getData().getFinalDiscountAmount();
//            lOrderOptional.get().setAmount(lAmountAfterDiscount);
//
//            mLogger.info("PromotionResponse: {},ResponseEntity body: {}, ResponseEntity status code: {},", lPromotionResponse,
//                    lResponseEntity.getBody(), lResponseEntity.getStatusCode());
            return lOrderOptional;
        } else {
            mLogger.debug("Exiting getOrderDetails method");
            throw new DataNotFoundException(Constants.ORDER_NOT_FOUND);
        }
    }

    /**
     * Updates the quantity of an order line
     *
     * @param pOrderId     - The unique Id of the order
     * @param pOrderLineId - The unique Id of the order line
     * @param pQuantity    - The new quantity to set for the order line
     * @throws - DataNotFoundException if the order or order line is not found
     */
    public void updateOrderLineQuantity(int pOrderId, int pOrderLineId, int pQuantity) {
        mLogger.debug("Path params -> Order Id: {} , OrderLine Id: {} , New Quantity: {}", pOrderId, pOrderLineId, pQuantity);

        OrderLine lOrderLine = mOrderLineRepository.findByOrderLineIdAndOrderOrderId(pOrderLineId, pOrderId).orElse(null);

        if (lOrderLine == null) {
            throw new DataNotFoundException(Constants.INCORRECT_ID);
        }

        lOrderLine.setQuantity(pQuantity);

        mOrderLineRepository.save(lOrderLine);

        mLogger.debug("Exiting updateOrderLineQuantity method");
    }

    /**
     * Gets the orders by page no,results per page,sort by and sorting Order
     *
     * @param pPageNo         - The page number for orders
     * @param pResultsPerPage - The number of orders per page
     * @param pSortBy         - To sort by fields of order
     * @param pSortOrder      - To sort by ascending and descending order
     * @return - Returns OrderResponsData class which contains meta and data fields
     */
    public Page<Order> getAllOrdersByPagination(Integer pPageNo, Integer pResultsPerPage, String pSortBy, String pSortOrder) {
        mLogger.debug("Query params -> page No: {} ,Results per page: {} ,Sort by: {} ,Sort order: {} ", pPageNo, pResultsPerPage, pSortBy,
                pSortOrder);

        Sort lSort = createSort(pSortBy, pSortOrder);
        Pageable lPageable = PageRequest.of(pPageNo, pResultsPerPage, lSort);
        Page<Order> lPageOrder = mOrderRepository.findAll(lPageable);

        return lPageOrder;
    }

    /**
     * Gets the orders by search term, page no,results per page,sort by and sorting
     * Order
     *
     * @param pSearchTerm     - The search term for searching the orders
     * @param pPageNo         - The page no for searched orders list
     * @param pResultsPerPage - The number of orders per page
     * @param pSortBy         - To sort on the basis of fields of order class
     * @param pSortOrder      - To sort on the basis of ascending and descending
     *                        order
     * @return - Returns OrderResponsData class which contains meta and data fields
     */
    public Page<Order> getAllOrdersBySearch(String pSearchTerm, Integer pPageNo, Integer pResultsPerPage, String pSortBy, String pSortOrder) {
        mLogger.debug("Query params ->Search Term: {} , page No: {} ,Results per page: {} ,Sort by: {} ,Sort order: {} ", pSearchTerm, pPageNo,
                pResultsPerPage, pSortBy, pSortOrder);

        Sort lSort = createSort(pSortBy, pSortOrder);
        Pageable lPageable = PageRequest.of(pPageNo, pResultsPerPage, lSort);
        Page<Order> lPageOrders = mOrderRepository.findAllOrderBySearchTerm(pSearchTerm, lPageable);

        return lPageOrders;
    }

    /**
     * Creates a list of OrderLine entities from the provided OrderRequest and order
     *
     * @param pOrderRequest - The OrderRequest object containing order lines
     * @param pOrder        - The Order object containing order
     * @return - A list of OrderLine entities representing individual items in the
     *         order
     */
    public List<OrderLine> createOrderlines(OrderRequest pOrderRequest, Order pOrder) {
        List<OrderLine> lOrderLines = new ArrayList<>();

        for (OrderLineRequest lOrderLineRequest : pOrderRequest.getOrderLines()) {

            OrderLine lOrderLine = new OrderLine();
            lOrderLine.setQuantity(lOrderLineRequest.getQuantity());
            lOrderLine.setItemName(lOrderLineRequest.getItemName());
            lOrderLine.setOrder(pOrder);

            lOrderLines.add(lOrderLine);
        }
        return lOrderLines;
    }

    /**
     * Creates an Order entity from the provided OrderRequest.
     *
     * @param - pOrderRequest The OrderRequest object containing order details.
     * @return - An Order entity representing the order.
     */
    public Order createOrderEntity(final OrderRequest pOrderRequest) {
        mLogger.debug("pOrderRequest -> Order Name: {}, Order Amount: {}", pOrderRequest.getOrderName(), pOrderRequest.getAmount());
        Order lOrder = new Order();
        lOrder.setAmount(pOrderRequest.getAmount());
        lOrder.setOrderName(pOrderRequest.getOrderName());
        mLogger.info("payload -> Order info: {}", lOrder);
        return lOrder;
    }

    /**
     * creates Sort object for sorting in various conditions
     *
     * @param pSortBy    - sort by order Id,amount and order name
     * @param pSortOrder - sorting in ascending or descending order
     * @return -returns Sort object
     */
    private Sort createSort(String pSortBy, String pSortOrder) {

        Sort lSort = null;
        if ("asc".equalsIgnoreCase(pSortOrder)) {
            lSort = Sort.by(pSortBy).ascending();
        } else {
            lSort = Sort.by(pSortBy).descending();
        }

        return lSort;
    }

}
