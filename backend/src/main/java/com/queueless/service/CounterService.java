package com.queueless.service;

import com.queueless.dto.CounterDto;
import com.queueless.dto.StaffAssignmentRequest;
import com.queueless.entity.Department;
import com.queueless.entity.Role;
import com.queueless.entity.ServiceCounter;
import com.queueless.entity.StaffAssignment;
import com.queueless.entity.TokenStatus;
import com.queueless.entity.User;
import com.queueless.exception.BadRequestException;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.DepartmentRepository;
import com.queueless.repository.QueueTokenRepository;
import com.queueless.repository.ServiceCounterRepository;
import com.queueless.repository.StaffAssignmentRepository;
import com.queueless.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounterService {

    private final ServiceCounterRepository counterRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final QueueTokenRepository queueTokenRepository;

    public CounterService(ServiceCounterRepository counterRepository,
                          DepartmentRepository departmentRepository,
                          UserRepository userRepository,
                          StaffAssignmentRepository staffAssignmentRepository,
                          QueueTokenRepository queueTokenRepository) {
        this.counterRepository = counterRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.staffAssignmentRepository = staffAssignmentRepository;
        this.queueTokenRepository = queueTokenRepository;
    }

    @Transactional
    public CounterDto createCounter(CounterDto dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        ServiceCounter counter = new ServiceCounter(
                dept,
                dto.getCounterNumber().trim(),
                dto.getName().trim()
        );

        if (dto.getCurrentStaffId() != null) {
            User staff = userRepository.findById(dto.getCurrentStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
            counter.setCurrentStaff(staff);
        }

        counter = counterRepository.save(counter);
        return CounterDto.fromEntity(counter);
    }

    @Transactional(readOnly = true)
    public List<CounterDto> getCountersByDepartment(Long departmentId) {
        return counterRepository.findByDepartmentIdAndActiveTrue(departmentId).stream()
                .map(this::enrichCounterDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CounterDto> getCountersByOrganization(Long organizationId) {
        return counterRepository.findByDepartmentOrganizationIdAndActiveTrue(organizationId).stream()
                .map(this::enrichCounterDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CounterDto getCounterById(Long id) {
        ServiceCounter counter = counterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Counter not found with ID: " + id));
        return enrichCounterDto(counter);
    }

    @Transactional(readOnly = true)
    public CounterDto getCounterByStaffUserId(Long staffUserId) {
        ServiceCounter counter = counterRepository.findByCurrentStaffIdAndActiveTrue(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No active counter currently assigned to this staff member"));
        return enrichCounterDto(counter);
    }

    @Transactional
    public CounterDto assignStaffToCounter(StaffAssignmentRequest req) {
        ServiceCounter counter = counterRepository.findById(req.getCounterId())
                .orElseThrow(() -> new ResourceNotFoundException("Counter not found with ID: " + req.getCounterId()));

        User staff = userRepository.findById(req.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with ID: " + req.getStaffId()));

        if (staff.getRole() != Role.ROLE_STAFF && staff.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("User must have STAFF or ADMIN role to be assigned to a counter");
        }

        // Deactivate previous assignment for this staff if any
        staffAssignmentRepository.findByStaffIdAndActiveTrue(staff.getId()).ifPresent(assignment -> {
            assignment.setActive(false);
            staffAssignmentRepository.save(assignment);
        });

        // Set staff on counter
        counter.setCurrentStaff(staff);
        counter = counterRepository.save(counter);

        StaffAssignment newAssignment = new StaffAssignment(staff, counter, null);
        staffAssignmentRepository.save(newAssignment);

        return enrichCounterDto(counter);
    }

    @Transactional
    public CounterDto unassignStaffFromCounter(Long counterId) {
        ServiceCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter not found with ID: " + counterId));

        if (counter.getCurrentStaff() != null) {
            staffAssignmentRepository.findByStaffIdAndActiveTrue(counter.getCurrentStaff().getId()).ifPresent(a -> {
                a.setActive(false);
                staffAssignmentRepository.save(a);
            });
            counter.setCurrentStaff(null);
            counter = counterRepository.save(counter);
        }

        return enrichCounterDto(counter);
    }

    private CounterDto enrichCounterDto(ServiceCounter counter) {
        CounterDto dto = CounterDto.fromEntity(counter);
        // Find current serving or called token
        var activeTokens = queueTokenRepository.findByServiceCounterIdAndStatusInOrderByCreatedAtDesc(
                counter.getId(), List.of(TokenStatus.CALLED, TokenStatus.SERVING)
        );
        if (!activeTokens.isEmpty()) {
            dto.setCurrentServingToken(activeTokens.get(0).getTokenNumber());
        }
        return dto;
    }
}
