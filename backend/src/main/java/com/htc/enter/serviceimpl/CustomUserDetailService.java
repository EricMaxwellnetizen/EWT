package com.htc.enter.serviceimpl;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.htc.enter.dto.UserPrincipal;
import com.htc.enter.model.User;
import com.htc.enter.repository.UserInfoRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserInfoRepository userRepository;

    public CustomUserDetailService(UserInfoRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new UserPrincipal(user);
    }
}
