package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Resource
    private AddressBookMapper addressBookMapper;


    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }


    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }


    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }


    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }


    /**
     * 设置默认地址簿。
     * <p>
     * 本方法通过先将指定地址簿的默认状态重置为非默认（0），再将其设置为默认（1）来实现地址簿的默认设置逻辑。
     * 这种处理方式确保了数据库中仅有一个默认地址簿，符合业务逻辑的要求。
     *
     * @param addressBook 待设置为默认的地址簿对象。
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        // 将地址簿的默认状态设置为非默认（0），为即将设置其为默认做准备。
        addressBook.setIsDefault(0);
        // 设置地址簿所属的用户ID，确保操作针对正确的用户地址簿。
        addressBook.setUserId(BaseContext.getCurrentId());
        // 更新数据库中该用户ID对应的地址簿的默认状态为非默认。
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        // 将地址簿的默认状态设置为默认（1），完成默认设置。
        addressBook.setIsDefault(1);
        // 更新数据库中该地址簿的信息，将其状态设置为默认。
        addressBookMapper.update(addressBook);
    }


    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

}
