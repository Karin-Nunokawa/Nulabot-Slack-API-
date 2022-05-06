package jp.nulab.nulab_exam.bean.dto;

import lombok.Getter;

@Getter
public class NulabProjectDto {
    // ID
    private int id;

    // projectKey
    private String projectKey;

    // name
    private String name;

    // chartEnabled
    private boolean chartEnabled;

    // useResolvedForChart
    private boolean useResolvedForChart;

    // subtaskingEnabled
    private boolean subtaskingEnabled;

    // projectLeaderCanEditProjectLeader
    private boolean projectLeaderCanEditProjectLeader;

    // useWiki
    private boolean useWiki;

    // useFileSharing
    private boolean useFileSharing;

    // useWikiTreeView
    private boolean useWikiTreeView;

    // useOriginalImageSizeAtWiki
    private boolean useOriginalImageSizeAtWiki;

    // textFormattingRule
    private String textFormattingRule;

    // archived
    private boolean archived;

    // displayOrder
    private int displayOrder;

    // useDevAttributes
    private boolean useDevAttributes;
}
