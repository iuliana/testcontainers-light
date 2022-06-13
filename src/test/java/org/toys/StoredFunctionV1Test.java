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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MariaDBContainer;
import org.toys.repo.SingerJdbcRepo;
import org.toys.repo.SingerRepo;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Created by iuliana.cosmina on 10/06/2022
 *  THIS WORKS
 */
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({ "classpath:testcontainers/drop-schema.sql", "classpath:testcontainers/create-schema.sql" })
@SpringJUnitConfig(classes = {StoredFunctionV1Test.TestContainersConfig.class, SingerJdbcRepo.class})
public class StoredFunctionV1Test {

    @Autowired
    SingerRepo singerRepo;

    @Test // method testing query
    void testFindAllQuery(){
        var singers = singerRepo.findAll();
        assertEquals(3, singers.size());
    }

    @Test // method testing stored function
    @Sql({ "classpath:testcontainers/stored-function.sql" })
    void testStoredFunction(){
        var firstName = singerRepo.findFirstNameById(2L).orElse(null);
        assertEquals("Ben", firstName);
    }

    @Configuration
    public static class TestContainersConfig {
        private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersConfig.class);

        MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.7.4-focal");

        @PostConstruct
        void initialize() {
            mariaDB.start();
        }

        @PreDestroy
        void tearDown(){
            mariaDB.stop();
        }

        @Bean
        DataSource dataSource() {
            try {
                var dataSource = new BasicDataSource();
                dataSource.setDriverClassName(mariaDB.getDriverClassName());
                dataSource.setUrl(mariaDB.getJdbcUrl());
                dataSource.setUsername(mariaDB.getUsername());
                dataSource.setPassword(mariaDB.getPassword());
                return dataSource;
            } catch (Exception e) {
                LOGGER.error("MariaDB TestContainers DataSource bean cannot be created!", e);
                return null;
            }
        }
    }

}
