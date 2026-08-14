package com.lrcore.system.listen;

import com.lrcore.system.domain.SysUserEntity;
import com.mybatisflex.annotation.SetListener;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 该经监听器只是特殊情况下使用
 * 注： 对应mybatis-flex官网的字典回写监听器功能 https://mybatis-flex.com/zh/core/columns-dict.html
 * @ClassName: SysUserEntityOnSetListener
 * @Author: Qi Liu
 * @Date: 2026/4/13 15:13
 * @Version: 1.0
 */
public class SysUserEntityOnSetListener implements SetListener {
    @Override
    public Object onSet(Object entity, String property, Object value) {
        SysUserEntity account = (SysUserEntity) entity;
        if (property.equals("sex") && value != null) {
//            int val = (int) value;
//            switch (val) {
//                case 1 -> account.setSexLabel("女");
//                case 2 -> account.setSexLabel("男");
//                default -> account.setSexLabel("未知");
//            }
        }
        return value;
    }
}
