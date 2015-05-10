/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix;

import java.util.HashMap;


class SessionRejectReasonText  {
    private static final HashMap<Integer, String> rejectReasonText = new HashMap<Integer, String>();
    
    public static final int FIELD = 373;
    public static final int INVALID_TAG_NUMBER = 0;
    public static final int REQUIRED_TAG_MISSING = 1;
    public static final int TAG_NOT_DEFINED_FOR_THIS_MESSAGE_TYPE = 2;
    public static final int UNDEFINED_TAG = 3;
    public static final int TAG_SPECIFIED_WITHOUT_A_VALUE = 4;
    public static final int VALUE_IS_INCORRECT = 5;
    public static final int INCORRECT_DATA_FORMAT_FOR_VALUE = 6;
    public static final int DECRYPTION_PROBLEM = 7;
    public static final int SIGNATURE_PROBLEM = 8;
    public static final int COMPID_PROBLEM = 9;
    public static final int SENDINGTIME_ACCURACY_PROBLEM = 10;
    public static final int INVALID_MSGTYPE = 11;
    public static final int XML_VALIDATION_ERROR = 12;
    public static final int TAG_APPEARS_MORE_THAN_ONCE = 13;
    public static final int TAG_SPECIFIED_OUT_OF_REQUIRED_ORDER = 14;
    public static final int REPEATING_GROUP_FIELDS_OUT_OF_ORDER = 15;
    public static final int INCORRECT_NUMINGROUP_COUNT_FOR_REPEATING_GROUP = 16;
    public static final int NON_DATA_VALUE_INCLUDES_FIELD_DELIMITER = 17;
    public static final int INVALID_UNSUPPORTED_APPLICATION_VERSION = 18;
    public static final int OTHER = 99;

    static {
        rejectReasonText.put(INVALID_TAG_NUMBER, "Invalid tag number");
        rejectReasonText.put(REQUIRED_TAG_MISSING, "Required tag missing");
        rejectReasonText.put(TAG_NOT_DEFINED_FOR_THIS_MESSAGE_TYPE, "Tag not defined for this message type");
        rejectReasonText.put(UNDEFINED_TAG, "Undefined Tag");
        rejectReasonText.put(TAG_SPECIFIED_WITHOUT_A_VALUE, "Tag specified without a value");
        rejectReasonText.put(VALUE_IS_INCORRECT, "Value is incorrect (out of range) for this tag");
        rejectReasonText.put(INCORRECT_DATA_FORMAT_FOR_VALUE, "Incorrect data format for value");
        rejectReasonText.put(DECRYPTION_PROBLEM, "Decryption problem");
        rejectReasonText.put(SIGNATURE_PROBLEM, "Signature problem");
        rejectReasonText.put(COMPID_PROBLEM, "CompID problem");
        rejectReasonText.put(SENDINGTIME_ACCURACY_PROBLEM, "SendingTime accuracy problem");
        rejectReasonText.put(INVALID_MSGTYPE, "Invalid MsgType");
        rejectReasonText.put(TAG_APPEARS_MORE_THAN_ONCE, "Tag appears more than once");
        rejectReasonText.put(TAG_SPECIFIED_OUT_OF_REQUIRED_ORDER, "Tag specified out of required order");
        rejectReasonText.put(INCORRECT_NUMINGROUP_COUNT_FOR_REPEATING_GROUP, "Incorrect NumInGroup count for repeating group");
        rejectReasonText.put(REPEATING_GROUP_FIELDS_OUT_OF_ORDER, "Out of order repeating group members");
    }

    public static String getMessage(int sessionRejectReason) {
        return rejectReasonText.get(sessionRejectReason);
    }

}
