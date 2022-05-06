package jp.nulab.nulab_exam.bean.dto;

import lombok.Getter;

@Getter
public class NulabUserDto {

    // ID
    private int id;

    // ユーザID
    private String userId;

    // 名前
    private String name;

    // 権限種別
    private int roleType;

    // 言語
    private String lang;

    // メールアドレス
    private String mailAddress;
}
