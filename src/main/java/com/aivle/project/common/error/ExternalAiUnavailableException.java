package com.aivle.project.common.error;

/**
 * 외부 AI 서버 장애를 나타내는 예외.
 */
public class ExternalAiUnavailableException extends CommonException {

	private final String detailMessage;
	private final String reasonCode;

	public ExternalAiUnavailableException(String message, String reasonCode, Throwable cause) {
		super(CommonErrorCode.COMMON_503);
		this.detailMessage = message;
		this.reasonCode = reasonCode;
		initCause(cause);
	}

	@Override
	public String getMessage() {
		return detailMessage + " (" + reasonCode + ")";
	}

	public String getReasonCode() {
		return reasonCode;
	}
}
