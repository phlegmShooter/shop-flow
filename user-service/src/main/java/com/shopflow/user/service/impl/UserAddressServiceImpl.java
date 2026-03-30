package com.shopflow.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopflow.user.entity.UserAddress;
import com.shopflow.user.mapper.UserAddressMapper;
import com.shopflow.user.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址服务实现
 */
@Slf4j
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
        implements UserAddressService {

    @Override
    public List<UserAddress> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                // 默认地址排首位，然后按创建时间倒序
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreatedAt));
    }

    @Override
    @Transactional
    public UserAddress addAddress(Long userId, UserAddress address) {
        address.setUserId(userId);

        // 如果是用户第一个地址，自动设为默认
        long count = count(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId));
        if (count == 0) {
            address.setIsDefault(1);
        }

        // 如果新增时标记为默认，先清掉其他默认
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            getBaseMapper().clearDefault(userId);
        } else {
            address.setIsDefault(0);
        }

        save(address);
        return address;
    }

    @Override
    @Transactional
    public UserAddress updateAddress(Long userId, Long addressId, UserAddress address) {
        UserAddress existing = getAndCheckOwner(userId, addressId);

        // 如果要设为默认，先清掉其他
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            getBaseMapper().clearDefault(userId);
        } else {
            // 保持原默认状态
            address.setIsDefault(existing.getIsDefault());
        }

        address.setId(addressId);
        address.setUserId(userId);
        updateById(address);
        return getById(addressId);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress existing = getAndCheckOwner(userId, addressId);
        removeById(addressId);

        // 删除的是默认地址时，把剩余第一条设为默认
        if (Integer.valueOf(1).equals(existing.getIsDefault())) {
            UserAddress first = getOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getCreatedAt)
                    .last("LIMIT 1"));
            if (first != null) {
                first.setIsDefault(1);
                updateById(first);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        getAndCheckOwner(userId, addressId);
        // 先全部清零，再设当前
        getBaseMapper().clearDefault(userId);
        UserAddress addr = new UserAddress();
        addr.setId(addressId);
        addr.setIsDefault(1);
        updateById(addr);
    }

    @Override
    public UserAddress getDefault(Long userId) {
        return getOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("LIMIT 1"));
    }

    /** 查询并校验地址归属，防止越权操作 */
    private UserAddress getAndCheckOwner(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new RuntimeException("地址不存在或无权操作");
        }
        return address;
    }
}
