package hexlet.code.service;

import hexlet.code.dto.update.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.interfaces.TaskStatusServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class StatusService implements TaskStatusServiceInterface {
    private final TaskStatusRepository taskStatusRepository;

    @Override
    public TaskStatus getStatus(long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public List<TaskStatus> getStatuses() {
        return taskStatusRepository.findAll();
    }

    @Override
    public TaskStatus createStatus(TaskStatusUpdateDTO taskStatusDto) {
        final TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(taskStatusDto.getName());
        taskStatusRepository.save(taskStatus);
        return taskStatus;
    }

    @Override
    public TaskStatus updateStatus(TaskStatusUpdateDTO taskStatusDto, long id) {
        final TaskStatus taskStatus = getStatus(id);
        taskStatus.setName(taskStatusDto.getName());
        return taskStatusRepository.save(taskStatus);
    }

    @Override
    public void deleteStatus(long id) {
        final TaskStatus taskStatus = getStatus(id);
        taskStatusRepository.delete(taskStatus);
    }
}
