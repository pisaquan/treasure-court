package com.sancai.oa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
public class BadgerDruidApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
    public void contextLoads() throws SQLException {
        Connection connection = dataSource.getConnection();


        for(int i=1;i<50;i++){
            PreparedStatement prepareStatement = connection
                    .prepareStatement("select * from t_company");
            ResultSet resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                String cityName = resultSet.getString("name");
                System.out.println(cityName);
            }
            System.out.println(dataSource);
        }


    }
}