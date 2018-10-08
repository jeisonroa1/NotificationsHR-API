package com.endava.hrapp.notifications.domain;

public enum ProcessPhase {
    SOURCING("Sourcing"),
    HR_INTERVIEW("HR Interview"),
    TECHNICAL_INTERVIEW("Technical Interview"),
    FINAL_INTERVIEW_TO_BE_SCHEDULED("Final Interview To Be Scheduled"),
    FINAL_INTERVIEW("Final Interview "),
    JO_TO_BE_MADE("JO To Be Made"),
    JO_REJECTED("JO Rejected"),
    STEP1("Step 1 -Start Screening & Medical Check-up"),
    STEP2("Step 2 - JO Made"),
    STEP3("Step 3 -JO Accepted"),
    STEP4("Step 4 -Onboard"),
    INTERNAL_CANDIDATE("Internal Candidate");

    private String phaseName;

    ProcessPhase(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseName() { return phaseName; }

}
