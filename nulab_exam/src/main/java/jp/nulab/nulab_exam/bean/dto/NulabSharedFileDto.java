package jp.nulab.nulab_exam.bean.dto;

import lombok.Getter;

@Getter
public class NulabSharedFileDto {
    // ID
    private int id;

    // type
    private String type;

    // dir
    private String dir;

    // name
    private String name;

    // size
    private int size;

    // createdUser
    private NulabUserDto createdUser;

    // created
    private String created;

    // updatedUser
    private NulabUserDto updatedUser;

    // updated
    private String updated;

}
