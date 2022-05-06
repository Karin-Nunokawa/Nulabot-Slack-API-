package jp.nulab.nulab_exam.bean.dto;

import lombok.Getter;

@Getter
public class NulabMilestoneDto {
    // ID
    private int id;

    // projectId
    private int projectId;

    // name
    private String name;

    // description
    private String description;

    // startDate
    private String startDate;

    // releaseDueDate
    private String releaseDueDate;

    // archived
    private boolean archived;

    // displayOrder
    private int displayOrder;
}
