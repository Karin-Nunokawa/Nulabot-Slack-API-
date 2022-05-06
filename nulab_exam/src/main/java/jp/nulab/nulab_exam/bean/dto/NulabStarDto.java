package jp.nulab.nulab_exam.bean.dto;

import lombok.Getter;

@Getter
public class NulabStarDto {
    // id
    private int id;

    // comment
    private String comment;

    // url
    private String url;

    // title
    private String title;

    // presenter
    private NulabUserDto presenter;

    // created
    private String created;

}
