package com.shopflow.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopflow.user.entity.UserAddress;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService extends IService<UserAddress> {

    /** 查询用户全部地址，默认地址排首位 */
    List<UserAddress> listByUser(Long userId);

    /** 新增地址。若是第一条或 isDefault=1，自动清掉其他默认 */
    UserAddress addAddress(Long userId, UserAddress address);

    /** 修改地址（校验归属） */
    UserAddress updateAddress(Long userId, Long addressId, UserAddress address);

    /** 删除地址（若删除默认地址，转移默认到剩余第一条） */
    void deleteAddress(Long userId, Long addressId);

    /** 设为默认 */
    void setDefault(Long userId, Long addressId);

    /** 获取默认地址（结算页自动填充用） */
    UserAddress getDefault(Long userId);
}
