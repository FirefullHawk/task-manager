package hexlet.code.service;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDTO;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final LabelService labelService;
    private final StatusService taskStatusService;
    private final UserService userService;


    public Task createTask(TaskDTO taskDto) {
        return taskRepository.save(buildTask(taskDto));
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow();
    }

    public List<Task> getAllTasks(Predicate predicate) {
        return (List<Task>) taskRepository.findAll(predicate);
    }

    public Task updateTask(TaskDTO taskDto, Long id) {
        Task temporaryTask = buildTask(taskDto);
        final Task task = taskRepository.findById(id)
                .orElseThrow();
        task.setName(temporaryTask.getName());
        task.setDescription(temporaryTask.getDescription());
        task.setExecutor(temporaryTask.getExecutor());
        task.setTaskStatus(temporaryTask.getTaskStatus());
        task.setLabels(temporaryTask.getLabels());
        return taskRepository.save(task);
    }

    public void deleteTask(long id) {
        final Task task = taskRepository.findById(id)
                .orElseThrow();
        taskRepository.delete(task);
    }

    private Task buildTask(TaskDTO taskDto) {
        final User author = userService.getCurrentUser();
        final User executor = Optional.ofNullable(taskDto.executorId())
                .map(userService::getUserById)
                .orElse(null);

        final TaskStatus status = Optional.ofNullable(taskDto.taskStatusId())
                .map(taskStatusService::getStatus)
                .orElse(null);


        final Set<Label> labels = Optional.ofNullable(taskDto.labelIds())
                .orElse(Set.of())
                .stream()
                .filter(Objects::nonNull)
                .map(labelService::getLabelById)
                .collect(Collectors.toSet());

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(status)
                .labels(labels)
                .name(taskDto.name())
                .description(taskDto.description())
                .build();
    }
}
