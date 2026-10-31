package in.edu.ktu.buspass;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.domain.PassStatus;
import in.edu.ktu.buspass.repository.PassApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BuspassApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PassApplicationRepository passes;

    @Test
    void contextLoads() {
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("KTU register number")));
    }

    @Test
    void dashboardRequiresLogin() throws Exception {
        mvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void demoStudentCanSignInWithRegisterNumber() throws Exception {
        mvc.perform(formLogin("/login")
                        .userParameter("registerNo")
                        .user("TVE22CS001")
                        .password("Passw0rd!"))
                .andExpect(authenticated().withUsername("TVE22CS001"))
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(formLogin("/login")
                        .userParameter("registerNo")
                        .user("TVE22CS001")
                        .password("wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void studentCannotOpenAdminPages() throws Exception {
        mvc.perform(get("/admin").with(user("TVE22CS001").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanOpenApplications() throws Exception {
        mvc.perform(get("/admin").with(user("KTUADMIN").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Applications")));
    }

    @Test
    void publicVerifyPageLoads() throws Exception {
        mvc.perform(get("/verify"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Verify a bus pass")));
    }

    @Test
    void invalidRegisterNumberIsRejected() throws Exception {
        mvc.perform(post("/register")
                        .param("registerNo", "not-a-ktu-number")
                        .param("name", "Test Student")
                        .param("email", "test@example.com")
                        .param("department", "Computer Science")
                        .param("password", "Passw0rd!")
                        .param("confirmPassword", "Passw0rd!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Use the KTU format")));
    }

    @Test
    void studentPagesRender() throws Exception {
        mvc.perform(get("/dashboard").with(user("TVE22CS001").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your pass")));
        mvc.perform(get("/passes").with(user("TVE22CS001").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My passes")));
        mvc.perform(get("/pass/apply").with(user("TVE21ME007").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apply for a bus pass")));
        mvc.perform(get("/profile").with(user("TVE22CS001").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Change password")));
    }

    @Test
    void pendingApplicationShownOnDashboard() throws Exception {
        mvc.perform(get("/dashboard").with(user("TVE22EC014").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("under review")));
    }

    @Test
    void applyRedirectsWhenStudentAlreadyHasOpenPass() throws Exception {
        mvc.perform(get("/pass/apply").with(user("TVE22CS001").roles("STUDENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void passDetailAndVerificationRender() throws Exception {
        PassApplication approved = passes.findByStatusOrderByAppliedAtAsc(PassStatus.APPROVED).get(0);
        mvc.perform(get("/pass/" + approved.getId()).with(user("TVE22CS001").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pass holder")))
                .andExpect(content().string(containsString(approved.getReference())));
        mvc.perform(get("/verify/" + approved.getReference()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Valid pass")));
    }

    @Test
    void studentCannotViewAnotherStudentsPass() throws Exception {
        PassApplication approved = passes.findByStatusOrderByAppliedAtAsc(PassStatus.APPROVED).get(0);
        mvc.perform(get("/pass/" + approved.getId()).with(user("TVE22EC014").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }
}
