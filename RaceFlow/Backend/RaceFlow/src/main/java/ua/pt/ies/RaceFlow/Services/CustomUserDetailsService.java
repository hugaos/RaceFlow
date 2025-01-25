package ua.pt.ies.RaceFlow.Services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Map<String, String> USERS = new HashMap<>();
    private static final Map<String, String> ROLES = new HashMap<>();

    public CustomUserDetailsService() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String username = "admin";
        String rawPassword = "admin";
        String encodedPassword = encoder.encode(rawPassword);

        USERS.put(username, encodedPassword);
        ROLES.put(username, "ROLE_ADMIN");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!USERS.containsKey(username)) {
            throw new UsernameNotFoundException("User not found");
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(USERS.get(username))
                .roles(ROLES.get(username).replace("ROLE_", ""))
                .build();
    }
}
