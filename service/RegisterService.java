package com.fresco.codelab.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fresco.codelab.model.CodeLabUser;
import com.fresco.codelab.repo.CodeLabUserRepository;

@Service
public class RegisterService {

    @Autowired
    private CodeLabUserRepository userRepo;

    @Transactional
    public Long registerUser(String fullname, String username, String password) {
        CodeLabUser user = new CodeLabUser();
        user.setFullname(fullname);
        user.setUsername(username);
        user.setPassword(password);
        CodeLabUser saved = userRepo.save(user);
        return saved.getUserAutoGenId();
    }
}
