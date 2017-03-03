package com.airent.db;

import com.airent.config.OyoSpringTest;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@OyoSpringTest
public class UserTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdvertMapper advertMapper;

    @Test
    public void testFindByStartingSixNumbers() {
        Advert advert = TestUtil.createAdvert(advertMapper);
        User user1 = TestUtil.createUser(userMapper, 987234_8751L);
        User user2 = TestUtil.createUser(userMapper, 987234_8752L);
        User user3 = TestUtil.createUser(userMapper, 987235_8751L);
        User user4 = TestUtil.createUser(userMapper, 987236_8751L);

        advertMapper.bindToUser(advert.getId(), user1.getId());
        advertMapper.bindToUser(advert.getId(), user2.getId());
        advertMapper.bindToUser(advert.getId(), user3.getId());
        advertMapper.bindToUser(advert.getId(), user4.getId());

        List<User> group1 = userMapper.findByStartingSixNumbers(advert.getId(), 987234L);
        assertEquals(group1.size(), 2);
        assertTrue(group1.stream().map(User::getPhone).collect(Collectors.toList()).contains(user1.getPhone()));
        assertTrue(group1.stream().map(User::getPhone).collect(Collectors.toList()).contains(user2.getPhone()));

        List<User> group2 = userMapper.findByStartingSixNumbers(advert.getId(), 987235L);
        assertEquals(group2.size(), 1);
        assertTrue(group2.stream().map(User::getPhone).collect(Collectors.toList()).contains(user3.getPhone()));

        List<User> group3 = userMapper.findByStartingSixNumbers(advert.getId(), 987239L);
        assertTrue(group3.isEmpty());
    }

    @Test
    public void testArrangeRate() {
        Advert advert = TestUtil.createAdvert(advertMapper);
        User user1 = TestUtil.createUser(userMapper, 9000000001L);
        User user2 = TestUtil.createUser(userMapper, 9000000002L);
        User user3 = TestUtil.createUser(userMapper, 9000000003L);

        advertMapper.bindToUser(advert.getId(), user1.getId());
        advertMapper.bindToUser(advert.getId(), user2.getId());
        advertMapper.bindToUser(advert.getId(), user3.getId());

        userMapper.arrangeRate(advert.getId(), user1.getId(), 15_000, 0.25d);

        assertEquals(userMapper.findById(user1.getId()).getTrustRate(), 15_000);
        assertEquals(userMapper.findById(user2.getId()).getTrustRate(), 2500);
        assertEquals(userMapper.findById(user3.getId()).getTrustRate(), 2500);
    }

}