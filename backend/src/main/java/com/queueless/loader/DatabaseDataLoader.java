package com.queueless.loader;

import com.queueless.entity.*;
import com.queueless.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DatabaseDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseDataLoader.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceCounterRepository counterRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseDataLoader(UserRepository userRepository,
                              OrganizationRepository organizationRepository,
                              DepartmentRepository departmentRepository,
                              ServiceRepository serviceRepository,
                              ServiceCounterRepository counterRepository,
                              StaffAssignmentRepository staffAssignmentRepository,
                              QueueTokenRepository queueTokenRepository,
                              TokenHistoryRepository tokenHistoryRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.serviceRepository = serviceRepository;
        this.counterRepository = counterRepository;
        this.staffAssignmentRepository = staffAssignmentRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.tokenHistoryRepository = tokenHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (organizationRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data loader.");
            return;
        }

        log.info("Seeding initial demonstration organizations, departments, users, and tokens...");

        // 1. Organizations
        Organization hospital = new Organization(
                "City General Hospital",
                "CGH",
                OrganizationType.HOSPITAL,
                "Premier healthcare center with digital queue orchestration and outpatient routing.",
                "450 Healthcare Boulevard, Metro City"
        );
        hospital = organizationRepository.save(hospital);

        Organization university = new Organization(
                "Apex Metropolitan University",
                "AMU",
                OrganizationType.COLLEGE,
                "Central admissions, transcript, and registrar services for 25,000 students.",
                "100 University Parkway, Tech Park"
        );
        university = organizationRepository.save(university);

        Organization bank = new Organization(
                "Metro Commercial Bank",
                "MCB",
                OrganizationType.BANK,
                "Full-service retail banking branch with priority wealth and cash management.",
                "12 Wall Street Financial District"
        );
        bank = organizationRepository.save(bank);

        // 2. Users (Admin, Staff, Customers)
        User adminUser = new User(
                "admin@queueless.com",
                passwordEncoder.encode("admin123"),
                "Dr. Victoria Vance",
                "+1-555-0199",
                Role.ROLE_ADMIN,
                hospital
        );
        adminUser = userRepository.save(adminUser);

        User hospitalStaffSarah = new User(
                "staff.sarah@hospital.com",
                passwordEncoder.encode("staff123"),
                "Sarah Jenkins, RN",
                "+1-555-0211",
                Role.ROLE_STAFF,
                hospital
        );
        hospitalStaffSarah = userRepository.save(hospitalStaffSarah);

        User hospitalStaffAlex = new User(
                "staff.alex@hospital.com",
                passwordEncoder.encode("staff123"),
                "Alex Rivera",
                "+1-555-0212",
                Role.ROLE_STAFF,
                hospital
        );
        hospitalStaffAlex = userRepository.save(hospitalStaffAlex);

        User collegeStaffDavid = new User(
                "staff.david@college.com",
                passwordEncoder.encode("staff123"),
                "David Chen (Registrar Officer)",
                "+1-555-0213",
                Role.ROLE_STAFF,
                university
        );
        collegeStaffDavid = userRepository.save(collegeStaffDavid);

        User customerUser = new User(
                "customer@queueless.com",
                passwordEncoder.encode("customer123"),
                "Marcus Brody",
                "+1-555-0344",
                Role.ROLE_CUSTOMER,
                null
        );
        customerUser = userRepository.save(customerUser);

        User studentPriya = new User(
                "student.priya@college.com",
                passwordEncoder.encode("student123"),
                "Priya Sharma",
                "+1-555-0388",
                Role.ROLE_CUSTOMER,
                university
        );
        studentPriya = userRepository.save(studentPriya);

        // 3. Departments
        // Hospital OPD
        Department opdDept = new Department(hospital, "Outpatient Department", "OPD", "General outpatient clinical care and consultations");
        opdDept = departmentRepository.save(opdDept);

        // Hospital Radiology
        Department radDept = new Department(hospital, "Radiology & Diagnostics", "RAD", "Diagnostic imaging and lab sample collection");
        radDept = departmentRepository.save(radDept);

        // College Registrar
        Department registrarDept = new Department(university, "Registrar & Academic Records", "REG", "Student transcript verification, grade sheets, and degree records");
        registrarDept = departmentRepository.save(registrarDept);

        // Bank Retail
        Department bankDept = new Department(bank, "Retail Banking Services", "RBS", "Everyday teller operations, deposits, and account inquiries");
        bankDept = departmentRepository.save(bankDept);

        // 4. Services
        ServiceEntity gpService = new ServiceEntity(opdDept, "General Physician Consultation", "GP-CONSULT", "GP", 12, 1, "Routine illness checkup and prescription renewal");
        gpService = serviceRepository.save(gpService);

        ServiceEntity pedService = new ServiceEntity(opdDept, "Pediatric Care", "PED-CARE", "PED", 15, 2, "Specialist medical consultation for infants and children");
        pedService = serviceRepository.save(pedService);

        ServiceEntity cardService = new ServiceEntity(opdDept, "Cardiology Screening (Priority)", "CARD-SCREEN", "CRD", 20, 3, "Urgent cardiac evaluation and ECG assessment");
        cardService = serviceRepository.save(cardService);

        ServiceEntity xRayService = new ServiceEntity(radDept, "Digital X-Ray & Imaging", "RAD-XRAY", "XR", 8, 1, "Quick scan digital chest, orthopedic, and skeletal x-rays");
        xRayService = serviceRepository.save(xRayService);

        ServiceEntity transcriptService = new ServiceEntity(registrarDept, "Transcript & Degree Attestation", "TRANS-VERIF", "TR", 10, 1, "Official academic transcript generation and seal");
        transcriptService = serviceRepository.save(transcriptService);

        ServiceEntity cashService = new ServiceEntity(bankDept, "Cash Deposit & Withdrawal", "CASH-TELLER", "CSH", 5, 1, "Over the counter cash handling");
        cashService = serviceRepository.save(cashService);

        // 5. Counters
        ServiceCounter opdCounter1 = new ServiceCounter(opdDept, "Counter 1", "Room 101 - Primary Care");
        opdCounter1.setCurrentStaff(hospitalStaffSarah);
        opdCounter1 = counterRepository.save(opdCounter1);

        ServiceCounter opdCounter2 = new ServiceCounter(opdDept, "Counter 2", "Room 102 - Pediatrics");
        opdCounter2.setCurrentStaff(hospitalStaffAlex);
        opdCounter2 = counterRepository.save(opdCounter2);

        ServiceCounter regCounter1 = new ServiceCounter(registrarDept, "Window 1", "Main Student Hall - Counter 1");
        regCounter1.setCurrentStaff(collegeStaffDavid);
        regCounter1 = counterRepository.save(regCounter1);

        // 6. Staff Assignments
        staffAssignmentRepository.save(new StaffAssignment(hospitalStaffSarah, opdCounter1, gpService));
        staffAssignmentRepository.save(new StaffAssignment(hospitalStaffAlex, opdCounter2, pedService));
        staffAssignmentRepository.save(new StaffAssignment(collegeStaffDavid, regCounter1, transcriptService));

        // 7. Seed Sample Historical and Active Tokens
        Instant now = Instant.now();

        // Historical completed tokens for Hospital (simulating realistic analytics)
        for (int i = 1; i <= 8; i++) {
            Instant created = now.minus(180 - (i * 20), ChronoUnit.MINUTES);
            Instant called = created.plus(6 + (i % 4), ChronoUnit.MINUTES);
            Instant served = called.plus(1, ChronoUnit.MINUTES);
            Instant completed = served.plus(10 + (i % 5), ChronoUnit.MINUTES);

            QueueToken t = new QueueToken(
                    String.format("OPD-GP-%03d", i),
                    hospital, opdDept, gpService, null,
                    "Patient " + (char) ('A' + i) + ". Anderson",
                    "+1-555-10" + i,
                    TokenPriority.REGULAR, i, 0
            );
            t.setStatus(TokenStatus.COMPLETED);
            t.setServiceCounter(opdCounter1);
            t.setCreatedAt(created);
            t.setCalledAt(called);
            t.setServedAt(served);
            t.setCompletedAt(completed);
            t = queueTokenRepository.save(t);
            tokenHistoryRepository.save(new TokenHistory(t, TokenStatus.SERVING, TokenStatus.COMPLETED, hospitalStaffSarah, "Consultation finished"));
        }

        // Token currently SERVING at Counter 1
        QueueToken activeServingToken = new QueueToken(
                "OPD-GP-009",
                hospital, opdDept, gpService, null,
                "Robert Davis",
                "+1-555-4422",
                TokenPriority.REGULAR, 9, 0
        );
        activeServingToken.setStatus(TokenStatus.SERVING);
        activeServingToken.setServiceCounter(opdCounter1);
        activeServingToken.setCreatedAt(now.minus(25, ChronoUnit.MINUTES));
        activeServingToken.setCalledAt(now.minus(12, ChronoUnit.MINUTES));
        activeServingToken.setServedAt(now.minus(10, ChronoUnit.MINUTES));
        activeServingToken = queueTokenRepository.save(activeServingToken);
        tokenHistoryRepository.save(new TokenHistory(activeServingToken, TokenStatus.CALLED, TokenStatus.SERVING, hospitalStaffSarah, "Patient seated at counter"));

        // Token currently CALLED at Counter 2
        QueueToken activeCalledToken = new QueueToken(
                "OPD-PED-010",
                hospital, opdDept, pedService, null,
                "Emily Taylor & Baby Liam",
                "+1-555-6677",
                TokenPriority.PRIORITY, 10, 0
        );
        activeCalledToken.setStatus(TokenStatus.CALLED);
        activeCalledToken.setServiceCounter(opdCounter2);
        activeCalledToken.setCreatedAt(now.minus(15, ChronoUnit.MINUTES));
        activeCalledToken.setCalledAt(now.minus(2, ChronoUnit.MINUTES));
        activeCalledToken.setRecallCount(1);
        activeCalledToken = queueTokenRepository.save(activeCalledToken);
        tokenHistoryRepository.save(new TokenHistory(activeCalledToken, TokenStatus.WAITING, TokenStatus.CALLED, hospitalStaffAlex, "Calling to Counter 2"));

        // Customer user's active token: Marcus Brody (WAITING)
        QueueToken customerToken = new QueueToken(
                "OPD-GP-011",
                hospital, opdDept, gpService, customerUser,
                customerUser.getFullName(),
                customerUser.getPhoneNumber(),
                TokenPriority.VIP, 11, 10
        );
        customerToken.setStatus(TokenStatus.WAITING);
        customerToken.setCreatedAt(now.minus(8, ChronoUnit.MINUTES));
        customerToken = queueTokenRepository.save(customerToken);
        tokenHistoryRepository.save(new TokenHistory(customerToken, null, TokenStatus.WAITING, customerUser, "Token booked via customer portal"));

        // Other waiting customers
        QueueToken waiting1 = new QueueToken(
                "OPD-GP-012",
                hospital, opdDept, gpService, null,
                "Sophia Chen",
                "+1-555-7788",
                TokenPriority.REGULAR, 12, 18
        );
        waiting1.setCreatedAt(now.minus(5, ChronoUnit.MINUTES));
        queueTokenRepository.save(waiting1);

        QueueToken waiting2 = new QueueToken(
                "OPD-GP-013",
                hospital, opdDept, gpService, null,
                "James Wilson",
                "+1-555-8899",
                TokenPriority.REGULAR, 13, 26
        );
        waiting2.setCreatedAt(now.minus(3, ChronoUnit.MINUTES));
        queueTokenRepository.save(waiting2);

        // Student Priya's token at University
        QueueToken studentToken = new QueueToken(
                "REG-TR-001",
                university, registrarDept, transcriptService, studentPriya,
                studentPriya.getFullName(),
                studentPriya.getPhoneNumber(),
                TokenPriority.REGULAR, 1, 5
        );
        studentToken.setStatus(TokenStatus.WAITING);
        studentToken.setCreatedAt(now.minus(10, ChronoUnit.MINUTES));
        queueTokenRepository.save(studentToken);

        log.info("Database seeding complete. Seed accounts ready: admin@queueless.com / staff.sarah@hospital.com / customer@queueless.com");
    }
}
