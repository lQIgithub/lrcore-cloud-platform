package com.lrcore.system.domain.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 头像信息
 * @ClassName: AvatarVo
 * @Author: Qi Liu
 * @Date: 2026/3/26 15:23
 * @Version: 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AvatarVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 头像地址
     */
    private String imgUrl;

}
