package quickfix;


public class FieldDictionary {
	
	public static final int BEGINSTRING_FIELD = 8;
	public static final int BODYLENGTH_FIELD = 9;
	public static final int CHECKSUM_FIELD = 10;
	
	public static final int MSGSEQNUM_FIELD = 34;
	public static final int MSGTYPE_FIELD = 35;
	public static final int POSSDUPFLAG_FIELD = 43;
	public static final int SENDERCOMPID_FIELD = 49;
	public static final int SENDERSUBID_FIELD = 50;
	public static final int SENDINGTIME_FIELD = 52;
	public static final int TARGETCOMPID_FIELD = 56;
	public static final int TARGETSUBID_FIELD = 57;
	
	public static final int SIGNATURE_FIELD = 89;
	public static final int SECUREDATALEN_FIELD = 90;
	public static final int SIGNATURELENGTH_FIELD = 93;
	public static final int POSSRESEND_FIELD = 97;
	
	public static final int ONBEHALFOFCOMPID_FIELD = 115;
	public static final int ONBEHALFOFSUBID_FIELD = 116;
	public static final int ORIGSENDINGTIME_FIELD = 122;
	public static final int DELIVERTOCOMPID_FIELD = 128;
	public static final int DELIVERTOSUBID_FIELD = 129;
	
	public static final int SENDERLOCATIONID_FIELD = 142;
	public static final int TARGETLOCATIONID_FIELD = 143;
	public static final int ONBEHALFOFLOCATIONID_FIELD = 144;
	public static final int DELIVERTOLOCATIONID_FIELD = 145;
	
	public static final int XMLDATALEN_FIELD = 212;
	public static final int XMLDATA_FIELD = 213;
	
	public static final int MESSAGEENCODING_FIELD = 347;
	public static final int LASTMSGSEQNUMPROCESSED_FIELD = 369;
	public static final int ONBEHALFOFSENDINGTIME_FIELD = 370;
	public static final int SESSIONREJECTTREASON_FIELD = 373;
	
	public static final int NOHOPS_FIELD = 627;
	
	public static final int APPLVERID_FIELD = 1128;
	public static final int CSTMAPPLVERID_FIELD = 1129;
	
	// Possible values for SESSIONREJECTREASON_FIELD are listed in SessionRejectReasonText class
	public static final int SESSIONREJECTREASON_FIELD = 373;	
}
