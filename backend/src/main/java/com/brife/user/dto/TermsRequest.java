// [DTO - 요청] 약관 동의 요청 (serviceTermsAgreed, privacyTermsAgreed).
package com.brife.user.dto;

public record TermsRequest(boolean serviceTermsAgreed, boolean privacyTermsAgreed) {}
