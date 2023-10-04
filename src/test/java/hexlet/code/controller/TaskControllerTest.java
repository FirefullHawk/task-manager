package hexlet.code.controller;

import hexlet.code.dto.TaskDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.Status;
import hexlet.code.config.TestConfig;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.StatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.NamedRoutes;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static hexlet.code.config.TestConfig.TEST_PROFILE;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestConfig.class)

public class TaskControllerTest {

    @Autowired
    private TestUtils utils;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private StatusRepository taskStatusRepository;

    @BeforeEach
    public void before() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus();
        utils.regDefaultLabel();
    }

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void createTask() throws Exception {

        final TaskDTO expectedTask = buildTaskDTO();

        final var response = utils.performAuthorizedRequest(
                        post(NamedRoutes.tasksPath())
                                .content(utils.asJson(expectedTask))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final Task task = fromJson(response.getContentAsString(), new TypeReference<>() { });

        assertThat(taskRepository.getReferenceById(task.getId())).isNotNull();
        assertThat(expectedTask.getName()).isEqualTo(task.getName());
    }

    @Test
    public void getTaskById() throws Exception {

        final TaskDTO defaultTask = buildTaskDTO();
        getTaskRequest(defaultTask);

        final Task expectedTask = taskRepository.findFirstByOrderById().get();

        final var response = utils.performAuthorizedRequest(
                        get(NamedRoutes.taskPath(expectedTask.getId())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Task task = fromJson(response.getContentAsString(), new TypeReference<>() { });

        assertThat(expectedTask.getId()).isEqualTo(task.getId());
        assertThat(expectedTask.getName()).isEqualTo(task.getName());
    }

    @Test
    public void getAllTasks() throws Exception {

        final TaskDTO defaultTask = buildTaskDTO();
        getTaskRequest(defaultTask);

        final var response = utils.performAuthorizedRequest(
                        get(NamedRoutes.tasksPath()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final List<Task> expected = taskRepository.findAll();

        int i = 0;
        for (var task : tasks) {
            assertThat(i < expected.size());
            assertEquals(task.getId(), expected.get(i).getId());
            assertEquals(task.getName(), expected.get(i).getName());
            i++;
        }
    }

    @Test
    public void updateTask() throws Exception {

        final TaskDTO taskDto = buildTaskDTO();
        getTaskRequest(taskDto);

        final Task defaultTask = taskRepository.findFirstByOrderById().get();

        final Long taskId = defaultTask.getId();
        final String oldTaskName = defaultTask.getName();

        taskDto.setName("Updated task title");
        taskDto.setDescription("Updated task description");

        var response = utils.performAuthorizedRequest(
                        put(NamedRoutes.taskPath(taskId))
                                .content(asJson(taskDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Task updatedTask = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final String updatedTaskName = updatedTask.getName();

        assertThat(taskRepository.existsById(taskId)).isTrue();
        assertThat(taskRepository.findById(taskId).get().getName()).isNotEqualTo(oldTaskName);
        assertThat(taskRepository.findById(taskId).get().getName()).isEqualTo(updatedTaskName);
        assertThat(taskRepository.findById(taskId).get().getDescription()).isEqualTo(updatedTask.getDescription());

    }

    @Test
    public void deleteTask() throws Exception {

        final TaskDTO defaultTask = buildTaskDTO();
        getTaskRequest(defaultTask);

        final Task task = taskRepository.findFirstByOrderById().get();

        final Long taskId = task.getId();

        utils.performAuthorizedRequest(
                        delete(NamedRoutes.taskPath(taskId)))
                .andExpect(status().isOk());

        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    public void deleteTaskFail() throws Exception {

        final TaskDTO defaultTask = buildTaskDTO();
        getTaskRequest(defaultTask);

        final Long defaultTaskId = taskRepository.findFirstByOrderById().get().getId();

        final String newUserUsername = "new user";

        utils.performAuthorizedRequest(
                        delete(NamedRoutes.taskPath(defaultTaskId)))
                .andExpect(status().isForbidden());
    }

    private TaskDTO buildTaskDTO() {

        User defaultUser = userRepository.findAll().stream().filter(Objects::nonNull).findFirst().get();
        Status defaultStatus = taskStatusRepository.findAll().stream().filter(Objects::nonNull).findFirst().get();
        Label defaultLabel = labelRepository.findAll().stream().filter(Objects::nonNull).findFirst().get();
        return  new TaskDTO(
                "task",
                "task_description",
                defaultUser.getId(),
                defaultUser.getId(),
                defaultStatus.getId(),
                Set.of(defaultLabel.getId())
        );
    }

    private ResultActions getTaskRequest(TaskDTO taskDto) throws Exception {
        return utils.performAuthorizedRequest(
                post(NamedRoutes.TASKS_PATH)
                        .content(asJson(taskDto))
                        .contentType(APPLICATION_JSON));
    }
}