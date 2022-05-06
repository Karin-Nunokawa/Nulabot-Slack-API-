package jp.nulab.nulab_exam.bean.dto;

import java.util.List;

import lombok.Data;

@Data
public class BlockKitAccessoryDto {

    // type
    private String type;

    // placeholder
    private BlockKitPlaceholderDto placeholder;

    // options
    private List<BlockKitOptionDto> options;

    // action_id
    private String action_id;
}
