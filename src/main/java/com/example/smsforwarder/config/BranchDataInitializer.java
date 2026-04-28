package com.example.smsforwarder.config;

import com.example.smsforwarder.entity.Branch;
import com.example.smsforwarder.repository.BranchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BranchDataInitializer implements CommandLineRunner {

    private final BranchRepository branchRepository;

    public BranchDataInitializer(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Branch> branches = List.of(
                new Branch(1L, "GLOBAL FARMA PR", "Prado"),
                new Branch(2L, "GLOBAL FARMA QU", "Quintanares"),
                new Branch(3L, "GLOBAL FARMA LA", "Fragua"),
                new Branch(4L, "RED FARMA", "Redfarma"),
                new Branch(5L, "PORTAL DE SAN", "San Ignacio")
        );

        for (Branch branch : branches) {
            Branch saved = branchRepository.findById(branch.getId()).orElse(new Branch());
            saved.setId(branch.getId());
            saved.setName(branch.getName());
            saved.setFullName(branch.getFullName());
            branchRepository.save(saved);
        }
    }
}
