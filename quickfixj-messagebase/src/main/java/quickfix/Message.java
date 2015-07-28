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

import static quickfix.FieldDictionary.*;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.quickfixj.util.CharsetSupport;
import org.quickfixj.util.FixVersions;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * Represents a FIX message.
 */
public class Message extends FieldMap {
	
	public static final char FIELD_SEPARATOR = '\001';
	
    static final long serialVersionUID = -3193357271891865972L;
    protected Header header = new Header();
    protected Trailer trailer = new Trailer();

    // @GuardedBy("this")
    private FieldException exception;

    public Message() {
        // empty
    }

    protected Message(int[] fieldOrder) {
        super(fieldOrder);
    }

    public Message(String string) throws InvalidMessage {
        fromString(string, null, true);
    }

    public Message(String string, boolean validate) throws InvalidMessage {
        fromString(string, null, validate);
    }

    public Message(String string, DataDictionary dd) throws InvalidMessage {
        fromString(string, dd, true);
    }

    public Message(String string, DataDictionary dd, boolean validate) throws InvalidMessage {
        fromString(string, dd, validate);
    }

    public static boolean InitializeXML(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object clone() {
        try {
            final Message message = getClass().newInstance();
            return cloneTo(message);
        } catch (final InstantiationException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object cloneTo(Message message) {
        message.initializeFrom(this);
        message.header.initializeFrom(getHeader());
        message.trailer.initializeFrom(getTrailer());
        return message;
    }

    /**
     * Do not call this method concurrently while modifying the contents of the message.
     * This is likely to produce unexpected results or will fail with a ConcurrentModificationException
     * since FieldMap.calculateString() is iterating over the TreeMap of fields.
     */
    @Override
    public String toString() {
        final int bodyLength = bodyLength();
        header.setInt(BODYLENGTH_FIELD, bodyLength);
        trailer.setString(CHECKSUM_FIELD, checksum());

        final StringBuilder sb = new StringBuilder(bodyLength);
        header.calculateString(sb, null, null);
        calculateString(sb, null, null);
        trailer.calculateString(sb, null, null);

        return sb.toString();
    }

    public int bodyLength() {
        return header.calculateLength() + calculateLength() + trailer.calculateLength();
    }

    private static final DecimalFormat checksumFormat = new DecimalFormat("000");

    private String checksum() {
        return checksumFormat.format(
            (header.calculateChecksum() + calculateChecksum() + trailer.calculateChecksum()) & 0xFF);
    }

    public void headerAddGroup(Group group) {
        header.addGroup(group);
    }

    public void headerReplaceGroup(int num, Group group) {
        header.replaceGroup(num, group);
    }

    public Group headerGetGroup(int num, Group group) throws FieldNotFound {
        return header.getGroup(num, group);
    }

    public void headerRemoveGroup(Group group) {
        header.removeGroup(group);
    }

    public boolean headerHasGroup(int field) {
        return header.hasGroup(field);
    }

    public boolean headerHasGroup(int num, int field) {
        return header.hasGroup(num, field);
    }

    public boolean headerHasGroup(int num, Group group) {
        return headerHasGroup(num, group.getFieldTag());
    }

    public boolean headerHasGroup(Group group) {
        return headerHasGroup(group.getFieldTag());
    }

    public void trailerAddGroup(Group group) {
        trailer.addGroup(group);
    }

    public Group trailerGetGroup(int num, Group group) throws FieldNotFound {
        return trailer.getGroup(num, group);
    }

    public void trailerReplaceGroup(int num, Group group) {
        trailer.replaceGroup(num, group);
    }

    public void trailerRemoveGroup(Group group) {
        trailer.removeGroup(group);
    }

    public boolean trailerHasGroup(int field) {
        return trailer.hasGroup(field);
    }

    public boolean trailerHasGroup(int num, int field) {
        return trailer.hasGroup(num, field);
    }

    public boolean trailerHasGroup(int num, Group group) {
        return trailerHasGroup(num, group.getFieldTag());
    }

    public boolean trailerHasGroup(Group group) {
        return trailerHasGroup(group.getFieldTag());
    }

    /**
     * Converts the message into a simple XML format. This format is
     * probably not sufficient for production use, but it more intended
     * for diagnostics and debugging. THIS IS NOT FIXML.
     *
     * To get names instead of tag number, use toXML(DataDictionary)
     * instead.
     *
     * @return an XML representation of the message.
     * @see #toXML(DataDictionary)
     */
    public String toXML() {
        return toXML(null);
    }

    /**
     * Converts the message into a simple XML format. This format is
     * probably not sufficient for production use, but it more intended
     * for diagnostics and debugging. THIS IS NOT FIXML.
     *
     * @param dataDictionary
     * @return the XML representation of the message
     */
    public String toXML(DataDictionary dataDictionary) {
        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
            final Element message = document.createElement("message");
            document.appendChild(message);
            toXMLFields(message, "header", header, dataDictionary);
            toXMLFields(message, "body", this, dataDictionary);
            toXMLFields(message, "trailer", trailer, dataDictionary);
            final DOMSource domSource = new DOMSource(document);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final StreamResult streamResult = new StreamResult(out);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
            return out.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void toXMLFields(Element message, String section, FieldMap fieldMap,
            DataDictionary dataDictionary) throws FieldNotFound {
        final Document document = message.getOwnerDocument();
        final Element fields = document.createElement(section);
        message.appendChild(fields);
        final Iterator<Field<?>> fieldItr = fieldMap.iterator();
        while (fieldItr.hasNext()) {
            final Field<?> field = fieldItr.next();
            final Element fieldElement = document.createElement("field");
            if (dataDictionary != null) {
                final String name = dataDictionary.getFieldName(field.getTag());
                if (name != null) {
                    fieldElement.setAttribute("name", name);
                }
                final String enumValue = dataDictionary.getValueName(field.getTag(), field
                        .getObject().toString());
                if (enumValue != null) {
                    fieldElement.setAttribute("enum", enumValue);
                }
            }
            fieldElement.setAttribute("tag", Integer.toString(field.getTag()));
            final CDATASection value = document.createCDATASection(field.getObject().toString());
            fieldElement.appendChild(value);
            fields.appendChild(fieldElement);
        }
        final Iterator<Integer> groupKeyItr = fieldMap.groupKeyIterator();
        while (groupKeyItr.hasNext()) {
            final int groupKey = groupKeyItr.next();
            final Element groupsElement = document.createElement("groups");
            fields.appendChild(groupsElement);
            if (dataDictionary != null) {
                final String name = dataDictionary.getFieldName(groupKey);
                if (name != null) {
                    groupsElement.setAttribute("name", name);
                }
            }
            groupsElement.setAttribute("tag", Integer.toString(groupKey));
            final List<Group> groups = fieldMap.getGroups(groupKey);
            for (Group group : groups) {
                toXMLFields(groupsElement, "group", group, dataDictionary);
            }
        }
    }

    public final Header getHeader() {
        return header;
    }

    public final Trailer getTrailer() {
        return trailer;
    }

    public boolean isAdmin() {
        if (header.isSetField(MSGTYPE_FIELD)) {
            try {
                final String msgType = header.getString(MSGTYPE_FIELD);
                return isAdminMessage(msgType);
            } catch (final FieldNotFound e) {
                // shouldn't happen
            }
        }
        return false;
    }

    public boolean isApp() {
        return !isAdmin();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && header.isEmpty() && trailer.isEmpty() && position == 0;
    }

    @Override
    public void clear() {
        super.clear();
        header.clear();
        trailer.clear();
        position = 0;
    }

    public static class Header extends FieldMap {
        static final long serialVersionUID = -3193357271891865972L;
        private static final int[] EXCLUDED_HEADER_FIELDS = { BEGINSTRING_FIELD, BODYLENGTH_FIELD,
                MSGTYPE_FIELD };

        public Header() {
            super();
        }

        public Header(int[] fieldOrder) {
            super(fieldOrder);
        }

        @Override
        protected void calculateString(StringBuilder buffer, int[] excludedFields, int[] postFields) {
            super.calculateString(buffer, EXCLUDED_HEADER_FIELDS, postFields);
        }
    }

    public static class Trailer extends FieldMap {
        static final long serialVersionUID = -3193357271891865972L;
        private static final int[] TRAILER_FIELD_ORDER = { SIGNATURELENGTH_FIELD, SIGNATURE_FIELD,
                CHECKSUM_FIELD };

        public Trailer() {
            super(TRAILER_FIELD_ORDER);
        }

        public Trailer(int[] fieldOrder) {
            super(fieldOrder);
        }

        @Override
        protected void calculateString(StringBuilder buffer, int[] excludedFields, int[] postFields) {
            super.calculateString(buffer, null, new int[] { CHECKSUM_FIELD });
        }
    }

    public void reverseRoute(Header header) throws FieldNotFound {
        this.header.removeField(BEGINSTRING_FIELD);
        this.header.removeField(SENDERCOMPID_FIELD);
        this.header.removeField(SENDERSUBID_FIELD);
        this.header.removeField(SENDERLOCATIONID_FIELD);
        this.header.removeField(TARGETCOMPID_FIELD);
        this.header.removeField(TARGETSUBID_FIELD);
        this.header.removeField(TARGETLOCATIONID_FIELD);

        if (header.isSetField(BEGINSTRING_FIELD)) {
            copyField(header, BEGINSTRING_FIELD, BEGINSTRING_FIELD);

            copyField(header, SENDERCOMPID_FIELD, TARGETCOMPID_FIELD);
            copyField(header, SENDERSUBID_FIELD, TARGETSUBID_FIELD);
            copyField(header, SENDERLOCATIONID_FIELD, TARGETLOCATIONID_FIELD);

            copyField(header, TARGETCOMPID_FIELD, SENDERCOMPID_FIELD);
            copyField(header, TARGETSUBID_FIELD, SENDERSUBID_FIELD);
            copyField(header, TARGETLOCATIONID_FIELD, SENDERLOCATIONID_FIELD);

            this.header.removeField(ONBEHALFOFCOMPID_FIELD);
            this.header.removeField(ONBEHALFOFSUBID_FIELD);
            this.header.removeField(DELIVERTOCOMPID_FIELD);
            this.header.removeField(DELIVERTOSUBID_FIELD);

            copyField(header, ONBEHALFOFCOMPID_FIELD, DELIVERTOCOMPID_FIELD);
            copyField(header, ONBEHALFOFSUBID_FIELD, DELIVERTOSUBID_FIELD);
            copyField(header, DELIVERTOCOMPID_FIELD, ONBEHALFOFCOMPID_FIELD);
            copyField(header, DELIVERTOSUBID_FIELD, ONBEHALFOFSUBID_FIELD);

            this.header.removeField(ONBEHALFOFLOCATIONID_FIELD);
            this.header.removeField(DELIVERTOLOCATIONID_FIELD);

            if (header.getString(BEGINSTRING_FIELD).compareTo(FixVersions.BEGINSTRING_FIX41) >= 0) {
                copyField(header, ONBEHALFOFLOCATIONID_FIELD, DELIVERTOLOCATIONID_FIELD);
                copyField(header, DELIVERTOLOCATIONID_FIELD, ONBEHALFOFLOCATIONID_FIELD);
            }
        }
    }

    private void copyField(Header header, int fromField, int toField) throws FieldNotFound {
        if (header.isSetField(fromField)) {
            final String value = header.getString(fromField);
            if (value.length() > 0) {
                this.header.setString(toField, value);
            }
        }
    }

    void setSessionID(SessionID sessionID) {
        header.setString(BEGINSTRING_FIELD, sessionID.getBeginString());
        header.setString(SENDERCOMPID_FIELD, sessionID.getSenderCompID());
        optionallySetID(header, SENDERSUBID_FIELD, sessionID.getSenderSubID());
        optionallySetID(header, SENDERLOCATIONID_FIELD, sessionID.getSenderLocationID());
        header.setString(TARGETCOMPID_FIELD, sessionID.getTargetCompID());
        optionallySetID(header, TARGETSUBID_FIELD, sessionID.getTargetSubID());
        optionallySetID(header, TARGETLOCATIONID_FIELD, sessionID.getTargetLocationID());
    }

    private void optionallySetID(Header header, int field, String value) {
        if (!value.equals(SessionID.NOT_SET)) {
            header.setString(field, value);
        }
    }

    public void fromString(String messageData, DataDictionary dd, boolean doValidation)
            throws InvalidMessage {
        parse(messageData, dd, dd, doValidation);
    }

    public void fromString(String messageData, DataDictionary sessionDictionary,
            DataDictionary applicationDictionary, boolean doValidation) throws InvalidMessage {
        if (sessionDictionary.isAdminMessage(getMessageType(messageData))) {
            applicationDictionary = sessionDictionary;
        }
        parse(messageData, sessionDictionary, applicationDictionary, doValidation);
    }

    void parse(String messageData, DataDictionary sessionDataDictionary,
            DataDictionary applicationDataDictionary, boolean doValidation) throws InvalidMessage {
        this.messageData = messageData;

        try {
            parseHeader(sessionDataDictionary, doValidation);
            parseBody(applicationDataDictionary, doValidation);
            parseTrailer(sessionDataDictionary);
            if (doValidation) {
                validateCheckSum(messageData);
            }
        } catch (final FieldException e) {
            exception = e;
        }
    }

    private void validateCheckSum(String messageData) throws InvalidMessage {
        try {
            // Body length is checked at the protocol layer
            final int checksum = trailer.getInt(CHECKSUM_FIELD);
            if (checksum != CharsetSupport.checksum(messageData)) {
                // message will be ignored if checksum is wrong or missing
                throw new InvalidMessage("Expected CheckSum=" + CharsetSupport.checksum(messageData)
                        + ", Received CheckSum=" + checksum + " in " + messageData);
            }
        } catch (final FieldNotFound e) {
            throw new InvalidMessage("Field not found: " + e.field + " in " + messageData);
        }
    }

    private void parseHeader(DataDictionary dd, boolean doValidation) throws InvalidMessage {
        if (doValidation) {
            final boolean validHeaderFieldOrder = isNextField(dd, header, BEGINSTRING_FIELD)
                    && isNextField(dd, header, BODYLENGTH_FIELD)
                    && isNextField(dd, header, MSGTYPE_FIELD);
            if (!validHeaderFieldOrder) {
                // Invalid message preamble (first three fields) is a serious
                // condition and is handled differently from other message parsing errors.
                throw new InvalidMessage("Header fields out of order in " + messageData);
            }
        }

        StringField field = extractField(dd, header);
        while (field != null && isHeaderField(field, dd)) {
            header.setField(field);

            if (dd != null && dd.isGroup(DataDictionary.HEADER_ID, field.getField())) {
                parseGroup(DataDictionary.HEADER_ID, field, dd, header);
            }

            field = extractField(dd, header);
        }
        pushBack(field);
    }

    private boolean isNextField(DataDictionary dd, Header fields, int tag) throws InvalidMessage {
        final StringField field = extractField(dd, header);
        if (field == null || field.getTag() != tag) {
            return false;
        }
        fields.setField(field);
        return true;
    }

    private String getMsgType() throws InvalidMessage {
        try {
            return header.getString(MSGTYPE_FIELD);
        } catch (final FieldNotFound e) {
            throw new InvalidMessage(e.getMessage() + " in " + messageData);
        }
    }

    private void parseBody(DataDictionary dd, boolean doValidation) throws InvalidMessage {
        StringField field = extractField(dd, this);
        while (field != null) {
            if (isTrailerField(field.getField())) {
                pushBack(field);
                return;
            }

            if (isHeaderField(field.getField())) {
                // An acceptance test requires the sequence number to
                // be available even if the related field is out of order
                setField(header, field);
                // Group case
                if (dd != null && dd.isGroup(DataDictionary.HEADER_ID, field.getField())) {
                    parseGroup(DataDictionary.HEADER_ID, field, dd, header);
                }
                if (doValidation && dd != null && dd.isCheckFieldsOutOfOrder())
                    throw new FieldException(SessionRejectReasonText.TAG_SPECIFIED_OUT_OF_REQUIRED_ORDER,
                        field.getTag());
            } else {
                setField(this, field);
                // Group case
                if (dd != null && dd.isGroup(getMsgType(), field.getField())) {
                    parseGroup(getMsgType(), field, dd, this);
                }
            }

            field = extractField(dd, this);
        }
    }

    private void setField(FieldMap fields, StringField field) {
        if (fields.isSetField(field)) {
            throw new FieldException(SessionRejectReasonText.TAG_APPEARS_MORE_THAN_ONCE, field.getTag());
        }
        fields.setField(field);
    }

    private void parseGroup(String msgType, StringField field, DataDictionary dd, FieldMap parent)
            throws InvalidMessage {
        final DataDictionary.GroupInfo rg = dd.getGroup(msgType, field.getField());
        final DataDictionary groupDataDictionary = rg.getDataDictionary();
        final int[] fieldOrder = groupDataDictionary.getOrderedFields();
        int previousOffset = -1;
        final int groupCountTag = field.getField();
        final int declaredGroupCount = Integer.parseInt(field.getValue());
        parent.setField(groupCountTag, field);
        final int firstField = rg.getDelimiterField();
        boolean firstFieldFound = false;
        Group group = null;
        boolean inGroupParse = true;
        while (inGroupParse) {
            field = extractField(dd, group != null ? group : parent);
            if (field == null) {
                // QFJ-760: stop parsing since current position is greater than message length
                break;
            }
            int tag = field.getTag();
            if (tag == firstField) {
                if (group != null) {
                    parent.addGroupRef(group);
                }
                group = new Group(groupCountTag, firstField, groupDataDictionary.getOrderedFields());
                group.setField(field);
                firstFieldFound = true;
                previousOffset = -1;
                // QFJ-742
                if (groupDataDictionary.isGroup(msgType, tag)) {
                    parseGroup(msgType, field, groupDataDictionary, group);
                }
            } else if (groupDataDictionary.isGroup(msgType, tag)) {
                if (!firstFieldFound) {
                    throw new InvalidMessage("The group " + groupCountTag
                            + " must set the delimiter field " + firstField + " in " + messageData);
                }
                parseGroup(msgType, field, groupDataDictionary, group);
            } else if (groupDataDictionary.isField(tag)) {
                if (!firstFieldFound) {
                    throw new FieldException(
                            SessionRejectReasonText.REPEATING_GROUP_FIELDS_OUT_OF_ORDER, tag);
                }

                if (fieldOrder != null && dd.isCheckUnorderedGroupFields()) {
                    final int offset = indexOf(tag, fieldOrder);
                    if (offset > -1) {
                        if (offset <= previousOffset) {
                            throw new FieldException(
                                    SessionRejectReasonText.REPEATING_GROUP_FIELDS_OUT_OF_ORDER, tag);
                        }
                        previousOffset = offset;
                    }
                }
                group.setField(field);
            } else {
                pushBack(field);
                inGroupParse = false;
            }
        }
        // add what we've already got and leave the rest to the validation (if enabled)
        if (group != null) {
            parent.addGroupRef(group);
        }
        // For later validation that the group size matches the parsed group count
        parent.setGroupCount(groupCountTag, declaredGroupCount);
    }

    private void parseTrailer(DataDictionary dd) throws InvalidMessage {
        StringField field = extractField(dd, trailer);
        while (field != null) {
            if (!isTrailerField(field, dd)) {
                throw new FieldException(SessionRejectReasonText.TAG_SPECIFIED_OUT_OF_REQUIRED_ORDER,
                        field.getTag());
            }
            trailer.setField(field);
            field = extractField(dd, trailer);
        }
    }

    static boolean isHeaderField(Field<?> field, DataDictionary dd) {
        return isHeaderField(field.getField())
                || (dd != null && dd.isHeaderField(field.getField()));
    }

    static boolean isHeaderField(int field) {
        switch (field) {
        case BEGINSTRING_FIELD:
        case BODYLENGTH_FIELD:
        case MSGTYPE_FIELD:
        case SENDERCOMPID_FIELD:
        case TARGETCOMPID_FIELD:
        case ONBEHALFOFCOMPID_FIELD:
        case DELIVERTOCOMPID_FIELD:
        case SECUREDATALEN_FIELD:
        case MSGSEQNUM_FIELD:
        case SENDERSUBID_FIELD:
        case SENDERLOCATIONID_FIELD:
        case TARGETSUBID_FIELD:
        case TARGETLOCATIONID_FIELD:
        case ONBEHALFOFSUBID_FIELD:
        case ONBEHALFOFLOCATIONID_FIELD:
        case DELIVERTOSUBID_FIELD:
        case DELIVERTOLOCATIONID_FIELD:
        case POSSDUPFLAG_FIELD:
        case POSSRESEND_FIELD:
        case SENDINGTIME_FIELD:
        case ORIGSENDINGTIME_FIELD:
        case XMLDATALEN_FIELD:
        case XMLDATA_FIELD:
        case MESSAGEENCODING_FIELD:
        case LASTMSGSEQNUMPROCESSED_FIELD:
        case ONBEHALFOFSENDINGTIME_FIELD:
        case APPLVERID_FIELD:
        case CSTMAPPLVERID_FIELD:
        case NOHOPS_FIELD:
            return true;
        default:
            return false;
        }
    }

    static boolean isTrailerField(Field<?> field, DataDictionary dd) {
        return isTrailerField(field.getField())
                || (dd != null && dd.isTrailerField(field.getField()));
    }

    static boolean isTrailerField(int field) {
        switch (field) {
        case SIGNATURELENGTH_FIELD:
        case SIGNATURE_FIELD:
        case CHECKSUM_FIELD:
            return true;
        default:
            return false;
        }
    }

    //
    // Extract field
    //
    private String messageData;

    private int position;

    private StringField pushedBackField;

    public void pushBack(StringField field) {
        pushedBackField = field;
    }

    private StringField extractField(DataDictionary dataDictionary, FieldMap fields)
            throws InvalidMessage {
        if (pushedBackField != null) {
            final StringField f = pushedBackField;
            pushedBackField = null;
            return f;
        }

        if (position >= messageData.length()) {
            return null;
        }

        final int equalsOffset = messageData.indexOf('=', position);
        if (equalsOffset == -1) {
            throw new InvalidMessage("Equal sign not found in field" + " in " + messageData);
        }

        int tag;
        try {
            tag = Integer.parseInt(messageData.substring(position, equalsOffset));
        } catch (final NumberFormatException e) {
            position = messageData.indexOf('\001', position + 1) + 1;
            throw new InvalidMessage("Bad tag format: " + e.getMessage() + " in " + messageData);
        }

        int sohOffset = messageData.indexOf('\001', equalsOffset + 1);
        if (sohOffset == -1) {
            throw new InvalidMessage("SOH not found at end of field: " + tag + " in " + messageData);
        }

        if (dataDictionary != null && dataDictionary.isDataField(tag)) {
            /* Assume length field is 1 less. */
            int lengthField = tag - 1;
            /* Special case for Signature which violates above assumption. */
            if (tag == 89) {
                lengthField = 93;
            }
            int fieldLength;
            try {
                fieldLength = fields.getInt(lengthField);
            } catch (final FieldNotFound e) {
                throw new InvalidMessage("Tag " + e.field + " not found in " + messageData);
            }

            // since length is in bytes but data is a string, and it may also contain an SOH,
            // we find the real field-ending SOH by checking the encoded bytes length
            // (we avoid re-encoding when the chars length equals the bytes length, e.g. ASCII text,
            // by assuming the chars length is always smaller than the encoded bytes length)
            while (sohOffset - equalsOffset - 1 < fieldLength
                    && messageData.substring(equalsOffset + 1, sohOffset).getBytes(CharsetSupport.getCharsetInstance()).length < fieldLength) {
                sohOffset = messageData.indexOf('\001', sohOffset + 1);
                if (sohOffset == -1) {
                    throw new InvalidMessage("SOH not found at end of field: " + tag + " in " + messageData);
                }
            }
        }

        position = sohOffset + 1;
        return new StringField(tag, messageData.substring(equalsOffset + 1, sohOffset));
    }

    /**
     * Queries message structural validity.
     *
     * @return flag indicating whether the message has a valid structure
     */
    synchronized boolean hasValidStructure() {
        return exception == null;
    }

    public synchronized FieldException getException() {
        return exception;
    }

    /**
     * Returns the first invalid tag, which is all that can be reported
     * in the resulting FIX reject message.
     *
     * @return the first invalid tag
     */
    synchronized int getInvalidTag() {
        return exception != null ? exception.getField() : 0;
    }
    
    public static boolean isAdminMessage(String msgType) {
        return msgType.length() == 1 && "0A12345".contains(msgType);
    }
    
    public static String getMessageType(String messageString) throws InvalidMessage {
        final String value = getStringField(messageString, 35);
        if (value == null) {
            throw new InvalidMessage("Missing or garbled message type in " + messageString);
        }
        return value;
    }
    
    public static String getStringField(String messageString, int tag) {
        String value = null;
        final String tagString = Integer.toString(tag);
        int start = messageString.indexOf(tagString, 0);
        while (start != -1 && value == null) {
            if ((start == 0 || messageString.charAt(start - 1) == FIELD_SEPARATOR)) {
                int end = start + tagString.length();
                if ((end + 1) < messageString.length() && messageString.charAt(end) == '=') {
                    // found tag, get value
                    start = end = (end + 1);
                    while (end < messageString.length()
                            && messageString.charAt(end) != FIELD_SEPARATOR) {
                        end++;
                    }
                    if (end == messageString.length()) {
                        return null;
                    } else {
                        value = messageString.substring(start, end);
                    }
                }
            }
            start = messageString.indexOf(tagString, start + 1);
        }
        return value;
    }
}
