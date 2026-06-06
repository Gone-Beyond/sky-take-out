package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 根据条件查询地址簿。
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 新增地址。
     */
    void save(AddressBook addressBook);

    /**
     * 根据 id 查询地址。
     */
    AddressBook getById(Long id);

    /**
     * 修改地址。
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址。
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据 id 删除地址。
     */
    void deleteById(Long id);
}
