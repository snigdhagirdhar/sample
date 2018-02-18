package com.sample.user;

import com.google.common.collect.Lists;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class UserTaskControllerTest {

    private UserTaskController uut;
    @Mock
    private UserTaskService userTaskServiceMock;
    private List<String> countries = Lists.newArrayList("someValue");

    @Before
    public void setUp() throws Exception {
        uut = new UserTaskController(userTaskServiceMock);
    }

    @Test
    public void getUserTasksByCountries() throws Exception {
        givenUserTasksForCountries(countries);

        List<UserTask> userTasksByCountries = uut.getUserTasksByCountries(countries);
        assertThat(userTasksByCountries, IsNull.notNullValue());
        assertThat(userTasksByCountries.size(), equalTo(1));

    }

    private void givenUserTasksForCountries(List<String> countries) {
        given(userTaskServiceMock.getTasks(eq(countries)))
                .willAnswer(invocationOnMock -> {
                   return countries
                           .stream()
                           .map(this::createUserTask)
                           .collect(Collectors.toList());
                });
    }

    private UserTask createUserTask(String country) {
        return new UserTask("testId", country, "testDesc", LocalDateTime.now(), "testProcessName");
    }

}