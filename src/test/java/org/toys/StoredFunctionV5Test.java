/*
Freeware License, some rights reserved

Copyright (c) 2022 Iuliana Cosmina

Permission is hereby granted, free of charge, to anyone obtaining a copy 
of this software and associated documentation files (the "Software"), 
to work with the Software within the limits of freeware distribution and fair use. 
This includes the rights to use, copy, and modify the Software for personal use. 
Users are also allowed and encouraged to submit corrections and modifications 
to the Software for the benefit of other users.

It is not allowed to reuse,  modify, or redistribute the Software for 
commercial use in any way, or for a user's educational materials such as books 
or blog articles without prior permission from the copyright holder. 

The above copyright notice and this permission notice need to be included 
in all copies or substantial portions of the software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS OR APRESS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.toys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import org.toys.config.BasicDataSourceCfg;
import org.toys.repo.SingerJdbcRepo;
import org.toys.repo.SingerRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Created by iuliana.cosmina on 10/06/2022
 *  TODO: THIS DOES NOT WORK!
 */
@Testcontainers
@SpringJUnitConfig(classes = {BasicDataSourceCfg.class, SingerJdbcRepo.class})
public class StoredFunctionV5Test {

    @Container
    static MariaDBContainer<?> mariaDB =
            new MariaDBContainer<>("mariadb:10.7.4")
                    .withCopyFileToContainer(MountableFile.forClasspathResource("testcontainers/create-schema.sql"), " /docker-entrypoint-initdb.d/")
                 .withCopyFileToContainer(MountableFile.forClasspathResource("testcontainers/stored-function.sql"), " /docker-entrypoint-initdb.d/")
            ;

    @DynamicPropertySource // this does the magic
    static void setUp(DynamicPropertyRegistry registry) {
        registry.add("jdbc.driverClassName", mariaDB::getDriverClassName);
        registry.add("jdbc.url", mariaDB::getJdbcUrl);
        registry.add("jdbc.username", mariaDB::getUsername);
        registry.add("jdbc.password", mariaDB::getPassword);
    }

    @Autowired
    SingerRepo singerRepo;

    @Test // method testing query
    void testFindAllQuery(){
        var singers = singerRepo.findAll();
        assertEquals(3, singers.size());
    }

    @Test // method testing stored function
    void testStoredFunction(){
        var firstName = singerRepo.findFirstNameById(2L).orElse(null);
        assertEquals("Ben", firstName);
    }
}
