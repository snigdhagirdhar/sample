package com.sample.user;

import java.util.List;

public interface UserTaskService {

    List<UserTask> getTasks(List<String> countries);
}
