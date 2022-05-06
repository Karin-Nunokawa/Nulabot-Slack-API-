package jp.nulab.nulab_exam.bean.dto;

import lombok.Getter;

@Getter
public class NulabIssueDto {
    // ID
    private int id;

    // projectId
    private int projectId;

    // issueKey
    private String issueKey;

    // keyId
    private String keyId;

    // issueType
    private NulabIssueTypeDto issueType;

    // summary
    private String summary;

    // description
    private String description;

    // resolution
    private String resolution;

    // priority
    private NulabPriorityDto priority;

    // status
    private NulabStatusDto status;

    // assignee
    private NulabAssigneeDto assignee;

    // category
    private String[] category;

    // versions
    private String[] versions;

    // milestone
    private NulabMilestoneDto[] milestone;

    // startDate
    private String startDate;

    // dueDate
    private String dueDate;

    // estimatedHours
    private String estimatedHours;

    // actualHours
    private String actualHours;

    // parentIssueId
    private String parentIssueId;

    // createdUser
    private NulabUserDto createdUser;

    // created
    private String created;

    // updatedUser
    private NulabUserDto updatedUser;

    // updated
    private String updated;

    // customFields
    private String[] customFields;

    // attachments
    private NulabAttachmentDto[] attachments;

    //sharedFiles
    private NulabSharedFileDto[] sharedFiles;

    // stars
    private NulabStarDto[] stars;

}
