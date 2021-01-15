package com.atguigu.gulimall.member.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class MemberLoginVo {
    @TableField
    private String loginacct;
    private String password;
}
