package com.eventshare.app.service.impl;

import com.eventshare.app.entity.User;
import com.eventshare.app.repository.UserRepository;
import com.eventshare.app.security.JwtTokenProvider;
import com.eventshare.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
}