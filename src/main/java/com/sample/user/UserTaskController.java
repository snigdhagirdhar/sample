package com.sample.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserTaskController {

    private UserTaskService userTaskService;

    public UserTaskController(UserTaskService userTaskService) {
        this.userTaskService = userTaskService;
    }

    /**
     * Accepts the requested the countries
     * @param countries
     * @return the list of all open user tasks which the user can access filtered by the requested countries.
     */
    @RequestMapping(value = "/userTasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserTask> getUserTasksByCountries(@RequestParam(required = false, value = "countries")
                                                  @Countries List<String> countries) {

        return userTaskService.getTasks(countries);
    }
}
