package com.palatophil.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.palatophil.module.user.entity.SysUser;
import com.palatophil.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;

    public SysUser findByOpenid(String openid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getOpenid, openid));
    }

    public SysUser findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public SysUser findById(Long id) {
        return userMapper.selectById(id);
    }

    public SysUser createWxUser(String openid, String nickname) {
        SysUser u = new SysUser();
        u.setOpenid(openid);
        u.setNickname(nickname == null || nickname.isBlank() ? "微信用户" : nickname);
        u.setRole("USER");
        u.setStatus(1);
        userMapper.insert(u);
        return u;
    }

    public void touchLastLogin(Long id) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setLastLoginAt(java.time.LocalDateTime.now());
        userMapper.updateById(u);
    }
}
