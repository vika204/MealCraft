package org.l5g7.mealcraft.springboottest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.l5g7.mealcraft.app.auth.dto.LoginUserDto;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.app.notification.NotificationRepository;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.recipes.RecipeRepository;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItemRepository;
import org.l5g7.mealcraft.app.user.PasswordHasher;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.app.user.UserRequestDto;
import org.l5g7.mealcraft.app.user.UserResponseDto;
import org.l5g7.mealcraft.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    MealPlanRepository mealPlanRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    ShoppingItemRepository shoppingItemRepository;

    @Autowired
    PasswordHasher passwordHasher;

    @Value("${jwt.cookie-name}")
    String authCookieName;

    String usersBase;
    String cookieHeader;

    @BeforeEach
    void setUp() {
        usersBase = "http://localhost:" + port + "/users";

        // Clean up in the correct order to avoid foreign key constraint violations
        mealPlanRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        notificationRepository.deleteAll();
        recipeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
                .username("admin")
                .email("admin@mealcraft.org")
                .password(passwordHasher.hashPassword("admin123"))
                .role(Role.ADMIN)
                .avatarUrl(null)
                .createdAt(new Date())
                .build();
        userRepository.save(admin);

        String loginUrl = "http://localhost:" + port + "/auth/login";
        LoginUserDto creds = new LoginUserDto("admin@mealcraft.org", "admin123");

        ResponseEntity<String> loginResp = rest.postForEntity(loginUrl, creds, String.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<String> setCookies = loginResp.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).isNotNull().hasSize(1);

        String only = setCookies.get(0);
        cookieHeader = only.split(";", 2)[0];
        assertThat(cookieHeader).startsWith(authCookieName + "=");
    }

    @Test
    void getUserById_returns200_andBody() {
        User u = User.builder()
                .username("vika")
                .email("vika@mealcraft.org")
                .password(passwordHasher.hashPassword("vika123"))
                .role(Role.ADMIN)
                .createdAt(new Date())
                .avatarUrl(null)
                .build();
        User saved = userRepository.save(u);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<UserResponseDto> resp = rest.exchange(
                usersBase + "/{id}",
                HttpMethod.GET,
                req,
                UserResponseDto.class,
                saved.getId()
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponseDto body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(saved.getId());
        assertThat(body.username()).isEqualTo("vika");
        assertThat(body.email()).isEqualTo("vika@mealcraft.org");
        assertThat(body.role()).isEqualTo(Role.ADMIN);
        assertThat(body.avatarUrl()).isNull();
    }

    @Test
    void getAllUsers_returns200_andArray() {
        User u1 = User.builder()
                .username("vika")
                .email("vika@mealcraft.org")
                .password(passwordHasher.hashPassword("vika123"))
                .role(Role.USER)
                .avatarUrl(null)
                .createdAt(new Date())
                .build();
        User u2 = User.builder()
                .username("ira")
                .email("ira@mealcraft.org")
                .password(passwordHasher.hashPassword("ira123"))
                .role(Role.USER)
                .avatarUrl(null)
                .createdAt(new Date())
                .build();
        userRepository.saveAll(List.of(u1, u2));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<List<UserResponseDto>> resp = rest.exchange(
                usersBase,
                HttpMethod.GET,
                req,
                new ParameterizedTypeReference<List<UserResponseDto>>() {}
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<UserResponseDto> body = resp.getBody();
        assertThat(body).isNotNull()
                .hasSizeGreaterThanOrEqualTo(2)
                .anyMatch(dto -> dto.username().equals("vika"))
                .anyMatch(dto -> dto.username().equals("ira"));
    }

    @Test
    void createUser_returns200_andPersists() {
        UserRequestDto body = UserRequestDto.builder()
                .username("ira")
                .email("ira@mealcraft.org")
                .password("ira123")
                .role(Role.USER)
                .avatarUrl(null)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRequestDto> req = new HttpEntity<>(body, headers);

        ResponseEntity<Void> resp = rest.postForEntity(usersBase, req, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        java.util.Optional<User> saved = userRepository.findByEmail("ira@mealcraft.org");
        assertThat(saved).isPresent();
        assertThat(saved.get().getUsername()).isEqualTo("ira");
        assertThat(saved.get().getRole()).isEqualTo(Role.USER);
        assertThat(passwordHasher.matches("ira123", saved.get().getPassword())).isTrue();
    }

    @Test
    void updateUser_returns200_andUpdates() {
        User u = User.builder()
                .username("vika")
                .email("vika@mealcraft.org")
                .password(passwordHasher.hashPassword("vika123"))
                .role(Role.USER)
                .avatarUrl(null)
                .createdAt(new Date())
                .build();
        userRepository.save(u);

        UserRequestDto body = UserRequestDto.builder()
                .username("vika.updated")
                .email("vika.updated@mealcraft.org")
                .password("vikaNewPass1")
                .role(Role.ADMIN)
                .avatarUrl("vika.png")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRequestDto> req = new HttpEntity<>(body, headers);

        ResponseEntity<Void> resp = rest.exchange(
                usersBase + "/{id}",
                HttpMethod.PUT,
                req,
                Void.class,
                u.getId()
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        User updated = userRepository.findById(u.getId()).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("vika.updated");
        assertThat(updated.getEmail()).isEqualTo("vika.updated@mealcraft.org");
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        assertThat(updated.getAvatarUrl()).isEqualTo("vika.png");
        assertThat(passwordHasher.matches("vikaNewPass1", updated.getPassword())).isTrue();
    }

    @Test
    void patchUser_returns200_andPartiallyUpdates() {
        User u = User.builder()
                .username("vika")
                .email("vika@mealcraft.org")
                .password(passwordHasher.hashPassword("vika123"))
                .role(Role.USER)
                .avatarUrl(null)
                .createdAt(new Date())
                .build();
        userRepository.save(u);

        UserRequestDto body = UserRequestDto.builder()
                .username("vika")
                .email("vika@mealcraft.org")
                .password("vikaNewPass2")
                .role(Role.ADMIN)
                .avatarUrl("vika-avatar.png")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRequestDto> req = new HttpEntity<>(body, headers);

        ResponseEntity<Void> resp = rest.exchange(
                usersBase + "/{id}",
                HttpMethod.PATCH,
                req,
                Void.class,
                u.getId()
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        User patched = userRepository.findById(u.getId()).orElseThrow();
        assertThat(patched.getUsername()).isEqualTo("vika");
        assertThat(patched.getEmail()).isEqualTo("vika@mealcraft.org");
        assertThat(patched.getRole()).isEqualTo(Role.ADMIN);
        assertThat(patched.getAvatarUrl()).isEqualTo("vika-avatar.png");
        assertThat(passwordHasher.matches("vikaNewPass2", patched.getPassword())).isTrue();
    }

    @Test
    void deleteUser_returns200_andRemovesFromDb() {
        User u = User.builder()
                .username("vika")
                .email("vika@mealcraft.org")
                .password(passwordHasher.hashPassword("vika123"))
                .role(Role.USER)
                .avatarUrl(null)
                .createdAt(new Date())
                .build();
        userRepository.save(u);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<Void> resp = rest.exchange(
                usersBase + "/{id}",
                HttpMethod.DELETE,
                req,
                Void.class,
                u.getId()
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(userRepository.findById(u.getId())).isEmpty();
        assertThat(userRepository.findByEmail("vika@mealcraft.org")).isEmpty();
    }
}