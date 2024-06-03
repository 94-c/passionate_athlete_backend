package com.backend.athlete.domain.branch.service;

import com.backend.athlete.domain.branch.dto.request.CreateBranchRequest;
import com.backend.athlete.domain.branch.dto.request.PageSearchBranchRequest;
import com.backend.athlete.domain.branch.dto.request.UpdateBranchRequest;
import com.backend.athlete.domain.branch.dto.response.*;
import com.backend.athlete.domain.branch.model.Branch;
import com.backend.athlete.domain.branch.repository.BranchRepository;
import com.backend.athlete.domain.user.model.User;
import com.backend.athlete.domain.user.model.type.UserRoleType;
import com.backend.athlete.domain.user.repository.UserRepository;
import com.backend.athlete.global.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public BranchService(BranchRepository branchRepository, UserRepository userRepository) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    public Page<PageSearchBranchResponse> findAllBranches(PageSearchBranchRequest request, int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        Page<Branch> branches = branchRepository.findByNameContainingAndManagerNameContaining(request.getName(), request.getManagerName(), pageable);

        return branches.map(branch -> {
            List<User> users = userRepository.findByBranchAndRole(branch, UserRoleType.USER);
            int totalMembers = users.size();
            return PageSearchBranchResponse.fromEntity(branch, totalMembers);
        });
    }

    public CreateBranchResponse createBranch(CreateBranchRequest request) {
        if (branchRepository.findByName(request.getName()).isPresent()) {
            throw new ServiceException("지점명이 이미 존재합니다.");
        }

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ServiceException("해당 유저를 찾을 수 없습니다."));

        Branch createBranch = branchRepository.save(CreateBranchRequest.toEntity(request, manager));

        return CreateBranchResponse.fromEntity(createBranch);
    }

    public GetBranchResponse getBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ServiceException("해당 지점을 찾을 수 없습니다."));

        return GetBranchResponse.fromEntity(branch);
    }

    public UpdateBranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ServiceException("해당 지점을 찾을 수 없습니다."));

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ServiceException("해당 유저를 찾을 수 없습니다."));

        branch.update(
                request.getName(),
                manager,
                request.getAddress(),
                request.getDetailedAddress(),
                request.getPostalCode(),
                request.getPhoneNumber(),
                request.getEtc()
        );

        branchRepository.save(branch);

        return UpdateBranchResponse.fromEntity(branch);
    }

    public GetBranchUsersResponse searchBranchUsersByName(String name) {
        Branch branch = branchRepository.findByNameContaining(name)
                .orElseThrow(() -> new ServiceException("해당 지점을 찾을 수 없습니다."));

        List<User> users = userRepository.findByBranchAndRole(branch, UserRoleType.USER);

        return GetBranchUsersResponse.fromEntity(branch, users);
    }



}
