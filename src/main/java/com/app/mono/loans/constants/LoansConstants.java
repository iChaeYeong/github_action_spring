package com.app.mono.loans.constants;

public final class LoansConstants {

    private LoansConstants() {
        // 객체 생성 방지 (인스턴스화 제한)
    }

    public static final String HOME_LOAN = "주택담보대출";
    public static final int NEW_LOAN_LIMIT = 100_000;

    public static final String STATUS_201 = "201";
    public static final String MESSAGE_201 = "대출이 성공적으로 생성되었습니다";

    public static final String STATUS_200 = "200";
    public static final String MESSAGE_200 = "요청이 정상적으로 처리되었습니다";

    public static final String STATUS_417 = "417";
    public static final String MESSAGE_417_UPDATE = "수정 작업에 실패했습니다. 다시 시도하거나 개발팀에 문의하세요";
    public static final String MESSAGE_417_DELETE = "삭제 작업에 실패했습니다. 다시 시도하거나 개발팀에 문의하세요";

    // public static final String STATUS_500 = "500";
    // public static final String MESSAGE_500 = "오류가 발생했습니다. 다시 시도하거나 개발팀에 문의하세요";
}