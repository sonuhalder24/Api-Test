package com.fresco.codelab.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fresco.codelab.model.CodeLabRepo;
import com.fresco.codelab.model.CodeLabRepoVersion;
import com.fresco.codelab.model.CodeLabUser;
import com.fresco.codelab.repo.CodeLabRepoRepository;
import com.fresco.codelab.repo.CodeLabRepoVersionRepository;
import com.fresco.codelab.repo.CodeLabUserRepository;
import com.fresco.codelab.service.DashboardService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private CodeLabUserRepository userRepo;

    @Autowired
    private CodeLabRepoRepository repoRepo;

    @Autowired
    private CodeLabRepoVersionRepository versionRepo;

    @Autowired
    private DashboardService dashboardService;

    @PostMapping("/createnewrepo")
    public String createNewRepo(@RequestParam("repo_name") String repoName) {
        List<CodeLabUser> users = userRepo.findAll();
        if (!users.isEmpty()) {
            CodeLabRepo repo = new CodeLabRepo();
            repo.setRepoName(repoName);
            repo.setRepoOwnerId(users.get(0).getUserAutoGenId());
            repoRepo.save(repo);
        }
        return "redirect:/dashboard/";
    }

    @GetMapping("/")
    public String getDashboard(Model model) {
        List<CodeLabUser> users = userRepo.findAll();
        List<CodeLabRepo> myrepos = new ArrayList<>();
        List<CodeLabRepo> allrepos = new ArrayList<>();
        if (!users.isEmpty()) {
            Long userId = users.get(0).getUserAutoGenId();
            myrepos = repoRepo.findByRepoOwnerId(userId);
            if (users.get(0).getRepos() != null) {
                allrepos = new ArrayList<>(users.get(0).getRepos());
            }
        }
        model.addAttribute("myrepos", myrepos);
        model.addAttribute("allrepos", allrepos);
        return "homepage.jsp";
    }

    @GetMapping("/openrepo/{repoId}")
    public String openRepo(@PathVariable Long repoId, Model model) {
        CodeLabRepo repo = repoRepo.findById(repoId).orElse(null);
        CodeLabUser repoOwner = null;
        if (repo != null && repo.getRepoOwnerId() != null) {
            repoOwner = userRepo.findById(repo.getRepoOwnerId()).orElse(null);
        }
        List<CodeLabUser> allUsers = userRepo.findAll();
        Long loggedInUser = allUsers.isEmpty() ? null : allUsers.get(0).getUserAutoGenId();
        model.addAttribute("repo", repo);
        model.addAttribute("repoOwner", repoOwner);
        model.addAttribute("developers", allUsers);
        model.addAttribute("loggedInUser", loggedInUser);
        return "repodashboardpage.jsp";
    }

    @PostMapping("/uploadcode/{repoId}")
    public String uploadCode(@PathVariable Long repoId,
                             @RequestParam("file") MultipartFile file) throws Exception {
        List<CodeLabRepoVersion> existing = versionRepo.findByRepo_RepoAutoGenId(repoId);
        int version = existing.size() + 1;

        String folderPath = "uploads/" + repoId + "-" + version + "code";
        new File(folderPath).mkdirs();

        ZipInputStream zis = new ZipInputStream(file.getInputStream());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                File outFile = new File(folderPath + "/" + entry.getName());
                outFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();

        CodeLabRepo repo = repoRepo.findById(repoId).orElse(null);
        if (repo != null) {
            CodeLabRepoVersion repoVersion = new CodeLabRepoVersion(version, repo, null);
            versionRepo.save(repoVersion);
        }

        return "redirect:/dashboard/openrepo/" + repoId;
    }

    @GetMapping("/openrepo/{repoId}/version/{version}")
    public String openRepoVersion(@PathVariable Long repoId,
                                  @PathVariable Integer version,
                                  Model model) throws Exception {
        CodeLabRepo repo = repoRepo.findById(repoId).orElse(null);

        String folderName = repoId + "-" + version + "code";
        String basePath = "uploads/" + folderName;

        TreeMap<String, List<String>> repoCode = new TreeMap<>();
        File folder = new File(basePath);
        if (folder.exists() && folder.isDirectory()) {
            addFilesToMap(folder, folderName, repoCode);
        }

        model.addAttribute("repo", repo);
        model.addAttribute("repoCode", repoCode);
        model.addAttribute("version", version);
        return "repocodepage.jsp";
    }

    private void addFilesToMap(File dir, String relPath, TreeMap<String, List<String>> map) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String childRelPath = relPath + "/" + f.getName();
            if (f.isFile()) {
                String content = new String(Files.readAllBytes(f.toPath()));
                List<String> value = new ArrayList<>();
                value.add(String.valueOf(content.length()));
                value.add(content);
                map.put(childRelPath, value);
            } else if (f.isDirectory()) {
                addFilesToMap(f, childRelPath, map);
            }
        }
    }

    @PostMapping("/savecode/{repoId}/{version}")
    public String saveCode(@PathVariable Long repoId,
                           @PathVariable Integer version,
                           @RequestParam("filename") String filename,
                           @RequestParam("code") String code) throws Exception {
        File f = new File("uploads/" + filename);
        f.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(f, false);
        fw.write(code);
        fw.close();
        return "redirect:/dashboard/openrepo/" + repoId + "/version/" + version;
    }

    @PostMapping("/adddeveloper/{repoId}")
    public String addDeveloper(@PathVariable Long repoId,
                               @RequestParam("developer") String username) {
        dashboardService.addDeveloper(repoId, username);
        return "redirect:/dashboard/openrepo/" + repoId;
    }
}
