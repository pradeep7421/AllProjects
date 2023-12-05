package com.winsupply.mdmcustomertoecomsubscriber.common;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility
 *
 * @author Amritanshu
 *
 */
public class Utility {

    private Utility() {
    }

    private static final ObjectMapper mObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static Map<String, String> lPhoneNumberTypesMap = new HashMap<>();
    private static Map<String, Integer> lRoleMap = new HashMap<>();

    static {
        lPhoneNumberTypesMap.put("LB", "Not Specified");
        lPhoneNumberTypesMap.put("MB", "Mobile");
        lPhoneNumberTypesMap.put("ALT", "Alternate");
        lPhoneNumberTypesMap.put("ON-SMS", "SMS");

        lRoleMap.put("admin", 0);
        lRoleMap.put("procurementmanager", 1);
        lRoleMap.put("trustedtech", 2);
        lRoleMap.put("lcadmin", 4);
        lRoleMap.put("personal", 5);
        lRoleMap.put("punchout", 6);
        lRoleMap.put("supportadmin", 7);
    }

    /**
     * <b>unmarshallData</b> - Unmarshall data
     *
     * @param pData  - String data
     * @param pClazz Class
     * @return an object of the give class
     * @throws IOException
     * @throws DatabindException
     */
    public static <T> T unmarshallData(final String pData, final Class<T> pClazz) throws IOException {
        return mObjectMapper.readValue(pData, pClazz);
    }

    /**
     * It checks if email is valid
     *
     * @param pEmailAddress - the email address
     * @return- boolean
     */
    public static boolean isValidEmail(final String pEmailAddress) {
        return pEmailAddress.matches(Constants.EMAIL_PATTERN);
    }

    /**
     * It returns the phone type in database based on phone type in received message
     *
     * @param pPhoneType - the phone type
     * @return - String
     */
    public static String getPhoneNumberType(final String pPhoneType) {
        return lPhoneNumberTypesMap.get(pPhoneType);
    }

    /**
     * <b>getContactRole</b> - it returns the Role id based on role type
     *
     * @param pType - the Type
     * @return - Short
     */
    public static Integer getContactRole(String pType) {
        return lRoleMap.get(pType);
    }
}
