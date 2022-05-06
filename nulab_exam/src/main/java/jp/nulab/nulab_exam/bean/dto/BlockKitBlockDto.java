package jp.nulab.nulab_exam.bean.dto;

import lombok.Data;

@Data
public class BlockKitBlockDto {
    // type
    private String type;

    // text
    private BlockKitTextDto text;

    // accessory
    private BlockKitAccessoryDto accessory;

}
