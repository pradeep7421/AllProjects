package com.winsupply.mdmcustomertoecomsubscriber.service;

import com.winsupply.mdmcustomertoecomsubscriber.common.Constants;
import com.winsupply.mdmcustomertoecomsubscriber.common.Utility;
import com.winsupply.mdmcustomertoecomsubscriber.models.CustomerMessageVO;
import jakarta.xml.bind.JAXBException;
import java.util.Locale;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Customer Subscriber Service
 *
 * @author Amritanshu
 */
@Service
public class CustomerSubscriberService {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final ResourceBundle mResourceBundle = ResourceBundle.getBundle(Constants.MESSAGE_BUNDLE, Locale.US);

    /**
     * CustomerSubscriberService Constructor
     *
     */
    public CustomerSubscriberService() {
    }

    /**
     * Process the Quotes Message
     *
     * @param pPayload        - the Payload
     * @param pMessageHeaders - the Message Headers
     * @throws JAXBException - the JAXBException
     */
    @Transactional
    public void processCustomerMessage(final String pPayload, final MessageHeaders pMessageHeaders) {
        final String lActionCode = (String) pMessageHeaders.get("action_code");
        try {
            final CustomerMessageVO lCustomerFeedVO = Utility.unmarshallData(pPayload, CustomerMessageVO.class);
            final String lCustomerEcmId = lCustomerFeedVO.getCustomerEcmId();

            mLogger.info("Processing customerECMId {}, Action {}...", lCustomerEcmId, lActionCode);
            if (!StringUtils.hasLength(lCustomerEcmId)) {
                mLogger.warn("SKIPING as customerECMId is missing for - {}", pPayload);
                return;
            }

        } catch (final Exception lException) {
            mLogger.error("Exception -> ", lException);
        }
    }

}
