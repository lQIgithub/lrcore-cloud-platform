package com.lrcore.auth.social;

import com.lrcore.common.auth.social.SocialAccountBinding;
import com.lrcore.common.auth.social.SocialAccountBindingRepository;
import com.lrcore.common.auth.social.SocialPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 集成测试用的内存版社交账号绑定仓库（替代 JDBC 仓库，行为与表约束一致：
 *            (platform, openId) 唯一；userId 维度可查）。
 * @ClassName: InMemoryBindingRepository
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
public class InMemoryBindingRepository implements SocialAccountBindingRepository {

    private final Map<Long, SocialAccountBinding> byId = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();

    @Override
    public SocialAccountBinding findByPlatformAndOpenId(SocialPlatform platform, String openId) {
        return this.byId.values().stream()
                .filter(b -> b.platform() == platform && openId.equals(b.openId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<SocialAccountBinding> findByUserId(Long userId) {
        List<SocialAccountBinding> result = new ArrayList<>();
        for (SocialAccountBinding b : this.byId.values()) {
            if (b.userId().equals(userId)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public SocialAccountBinding save(SocialAccountBinding binding) {
        Long id = binding.id() == null ? this.sequence.incrementAndGet() : binding.id();
        SocialAccountBinding stored = new SocialAccountBinding(
                id, binding.userId(), binding.username(), binding.platform(),
                binding.openId(), binding.nickname(), binding.avatarUrl());
        this.byId.put(id, stored);
        return stored;
    }

    @Override
    public void updateProfile(Long id, String nickname, String avatarUrl) {
        SocialAccountBinding existing = this.byId.get(id);
        if (existing != null) {
            this.byId.put(id, new SocialAccountBinding(
                    id, existing.userId(), existing.username(), existing.platform(),
                    existing.openId(), nickname, avatarUrl));
        }
    }

    @Override
    public void deleteById(Long id) {
        this.byId.remove(id);
    }
}
