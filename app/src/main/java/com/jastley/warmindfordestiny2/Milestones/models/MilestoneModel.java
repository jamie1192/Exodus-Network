package com.jastley.warmindfordestiny2.Milestones.models;

public class MilestoneModel {

    private String milestoneName;
    private String milestoneDescription;
    private String milestoneImageURL;
    private String milestoneRewardName;
    private String milestoneRewardImageURL;

    public MilestoneModel() {
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public String getMilestoneDescription() {
        return milestoneDescription;
    }

    public void setMilestoneDescription(String milestoneDescription) {
        this.milestoneDescription = milestoneDescription;
    }

    public String getMilestoneImageURL() {
        return milestoneImageURL;
    }

    public void setMilestoneImageURL(String milestoneImageURL) {
        this.milestoneImageURL = milestoneImageURL;
    }

    public String getMilestoneRewardName() {
        return milestoneRewardName;
    }

    public void setMilestoneRewardName(String milestoneRewardName) {
        this.milestoneRewardName = milestoneRewardName;
    }

    public String getMilestoneRewardImageURL() {
        return milestoneRewardImageURL;
    }

    public void setMilestoneRewardImageURL(String milestoneRewardImageURL) {
        this.milestoneRewardImageURL = milestoneRewardImageURL;
    }
}
