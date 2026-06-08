package com.fresco.codelab.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fresco.codelab.model.CodeLabRepo;
import com.fresco.codelab.model.CodeLabUser;
import com.fresco.codelab.repo.CodeLabRepoRepository;
import com.fresco.codelab.repo.CodeLabUserRepository;

@Service
public class DashboardService {

    @Autowired
    private CodeLabUserRepository userRepo;

    @Autowired
    private CodeLabRepoRepository repoRepo;

    @Transactional
    public void addDeveloper(Long repoId, String username) {
        CodeLabUser user = userRepo.findByUsername(username);
        if (user == null) return;
        CodeLabRepo repo = repoRepo.findById(repoId).orElse(null);
        if (repo == null) return;
        Set<CodeLabRepo> repos = user.getRepos();
        if (repos == null) {
            repos = new HashSet<>();
            user.setRepos(repos);
        }
        repos.add(repo);
        userRepo.save(user);
    }
}
