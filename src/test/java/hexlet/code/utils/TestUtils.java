package hexlet.code.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.JWTHelper;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.required.UserRequiredDTO;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public final class TestUtils {
    public static final String TEST_EMAIL_1 = "arar@rar.com";
    public static final String TEST_EMAIL_2 = "ududu@dud.com";
    public static final String LOGIN = "/login";

    private final UserRequiredDTO defaultUser = new UserRequiredDTO(
        TEST_EMAIL_1,
            "John",
            "Dou",
        "12345");

    private final TaskStatusDTO defaultStatus = new TaskStatusDTO(null, "Test Status", null);

    private final LabelDTO defaultLabel = new LabelDTO(null, "Test label", null);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private JWTHelper jwtHelper;

    public void tearDown() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
    }

    public ResultActions regDefaultUser() throws Exception {
        return regNewInstance(NamedRoutes.usersPath(), defaultUser);
    }

    public ResultActions regDefaultStatus() throws Exception {
        return regNewInstance(NamedRoutes.statusesPath(), defaultStatus);
    }

    public void regDefaultLabel() throws Exception {
        regNewInstance(NamedRoutes.labelsPath(), defaultLabel);
    }

    public ResultActions regNewInstance(String path, Object dto) throws Exception {
        return performAuthorizedRequest(post(path)
                .content(asJson(dto))
                .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performAuthorizedRequest(final MockHttpServletRequestBuilder request) throws Exception {
        final String token = jwtHelper.expiring(Map.of(SPRING_SECURITY_FORM_USERNAME_KEY, TEST_EMAIL_1));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    public ResultActions performAuthorizedRequest(
        final MockHttpServletRequestBuilder request, String newUser) throws Exception {
        final String token = jwtHelper.expiring(Map.of(SPRING_SECURITY_FORM_USERNAME_KEY, newUser));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }
}
