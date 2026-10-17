package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ishaan_student_app.data.model.AcademicPeriod;
import com.example.ishaan_student_app.data.model.Assignment;
import com.example.ishaan_student_app.data.model.AttendanceEntry;
import com.example.ishaan_student_app.data.model.PersonalSubject;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.data.repository.StudentContentRepository;
import com.example.ishaan_student_app.data.repository.AcademicPeriodRepository;
import com.example.ishaan_student_app.data.repository.AssignmentRepository;
import com.example.ishaan_student_app.data.repository.AttendanceEntryRepository;
import com.example.ishaan_student_app.data.repository.LocalAccountRepository;
import com.example.ishaan_student_app.data.repository.PersonalSubjectRepository;
import com.example.ishaan_student_app.data.repository.StudentRepository;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    @Test
    public void freshAccountHasNoSeedAndPersonalAttendanceIsEditableAndAccountScoped() {
        Context context = ApplicationProvider.getApplicationContext();
        String accountA = String.valueOf(ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000));
        String accountB = "TEST-" + UUID.randomUUID();

        StudentRepository studentRepository = new StudentRepository(context);
        LocalAccountRepository localAccountRepository = new LocalAccountRepository(context);
        AcademicPeriodRepository periodRepository = new AcademicPeriodRepository(context);
        PersonalSubjectRepository subjectRepository = new PersonalSubjectRepository(context);
        AttendanceEntryRepository entryRepository = new AttendanceEntryRepository(context);

        assertNull("A new account must not read a preloaded student", studentRepository.getStudentBySapId(accountA));

        Student student = new Student(0, accountA, "Test Student", accountA.toLowerCase() + "@nmims.in", "STME", 2, "");
        assertEquals(LocalAccountRepository.RegistrationResult.CREATED,
                localAccountRepository.register(student, "TestPassword123"));
        assertNotNull(studentRepository.getStudentBySapId(accountA));
        assertTrue(localAccountRepository.authenticate(accountA, student.getEmail(), "TestPassword123"));
        assertFalse(localAccountRepository.authenticate(accountA, student.getEmail(), "WrongPassword123"));

        AcademicPeriod period = new AcademicPeriod(0, accountA, "test-year", "Test semester", "", "", true, false, 0, 0);
        long periodId = periodRepository.saveOrUpdatePeriod(period);
        assertTrue(periodId > 0);

        PersonalSubject subject = new PersonalSubject(0, accountA, periodId, "Test subject", "", false, 0, 0);
        long subjectId = subjectRepository.saveOrUpdateSubject(subject);
        assertTrue(subjectId > 0);
        assertEquals(1, subjectRepository.getSubjectsForPeriod(accountA, periodId).size());

        AttendanceEntry present = new AttendanceEntry(0, accountA, subjectId, "16-10-2026", "", "Lecture 1", "PRESENT", "", 0, 0);
        AttendanceEntry absent = new AttendanceEntry(0, accountA, subjectId, "16-10-2026", "", "Lecture 2", "ABSENT", "", 0, 0);
        long presentId = entryRepository.saveOrUpdateEntry(present);
        long absentId = entryRepository.saveOrUpdateEntry(absent);

        assertTrue(presentId > 0);
        assertTrue(absentId > 0);
        assertEquals(1, entryRepository.getPresentCountForSubject(accountA, subjectId));
        assertEquals(1, entryRepository.getAbsentCountForSubject(accountA, subjectId));
        assertEquals("Multiple sessions on one date must remain separate records", 2, entryRepository.getEntriesForSubject(accountA, subjectId).size());

        AttendanceEntry crossAccountUpdate = new AttendanceEntry(presentId, accountB, subjectId, "17-10-2026", "", "", "ABSENT", "", 0, 0);
        assertEquals("A different account cannot update this entry", -1, entryRepository.saveOrUpdateEntry(crossAccountUpdate));
        assertFalse("A different account cannot delete this entry", entryRepository.deleteEntry(accountB, absentId));
        assertTrue(entryRepository.deleteEntry(accountA, absentId));
        assertEquals(0, entryRepository.getAbsentCountForSubject(accountA, subjectId));
        assertEquals(1, entryRepository.getPresentCountForSubject(accountA, subjectId));
    }

    @Test
    public void assignmentsAreStoredAndUpdatedWithinTheOwningAccount() {
        Context context = ApplicationProvider.getApplicationContext();
        String accountA = "TEST-" + UUID.randomUUID();
        String accountB = "TEST-" + UUID.randomUUID();
        AssignmentRepository repository = new AssignmentRepository(context);

        long assignmentId = repository.saveAssignment(accountA,
                new Assignment("Test task", "Test subject", "25-10-2026", false));
        assertTrue(assignmentId > 0);
        assertEquals(1, repository.getAssignmentsForAccount(accountA).size());
        assertTrue(repository.getAssignmentsForAccount(accountB).isEmpty());

        assertFalse(repository.updateAssignmentCompletion(accountB, assignmentId, true));
        assertTrue(repository.updateAssignmentCompletion(accountA, assignmentId, true));
        List<Assignment> rows = repository.getAssignmentsForAccount(accountA);
        assertEquals(1, rows.size());
        assertTrue(rows.get(0).isCompleted());
        assertFalse(repository.deleteAssignment(accountB, assignmentId));
        assertTrue(repository.deleteAssignment(accountA, assignmentId));
        assertTrue(repository.getAssignmentsForAccount(accountA).isEmpty());
    }

    @Test
    public void assignmentsEventsAndResourcesStayInTheirSemesterAndAccount() {
        Context context = ApplicationProvider.getApplicationContext();
        String account = "TEST-" + UUID.randomUUID();
        String otherAccount = "TEST-" + UUID.randomUUID();
        AcademicPeriodRepository periods = new AcademicPeriodRepository(context);
        AssignmentRepository assignments = new AssignmentRepository(context);
        StudentContentRepository content = new StudentContentRepository(context);

        long semesterFive = periods.saveOrUpdatePeriod(new AcademicPeriod(
                0, account, "2026-27", "Semester 5", "", "", true, false, 0, 0));
        long semesterSix = periods.saveOrUpdatePeriod(new AcademicPeriod(
                0, account, "2026-27", "Semester 6", "", "", false, false, 0, 0));
        assertTrue(semesterFive > 0);
        assertTrue(semesterSix > 0);

        assertTrue(assignments.saveAssignment(account,
                new Assignment("Semester 5 task", "Subject 5", "25-10-2026", false), semesterFive) > 0);
        assertTrue(assignments.saveAssignment(account,
                new Assignment("Semester 6 task", "Subject 6", "25-11-2026", false), semesterSix) > 0);
        assertEquals("Semester 5 must not show semester 6 work", "Semester 5 task",
                assignments.getAssignmentsForPeriod(account, semesterFive).get(0).getTitle());
        assertEquals("Semester 6 must not show semester 5 work", "Semester 6 task",
                assignments.getAssignmentsForPeriod(account, semesterSix).get(0).getTitle());

        assertTrue(content.addEvent(account, "Test event", "01-11-2026", "Campus", "", "") > 0);
        assertEquals(1, content.getEvents(account).size());
        assertTrue(content.getEvents(otherAccount).isEmpty());
        assertTrue(content.addResource(account, "notes.pdf", "application/pdf", "content://test/notes") > 0);
        assertEquals(1, content.getResources(account).size());
        assertTrue(content.getResources(otherAccount).isEmpty());
    }

    @Test
    public void updatingAnExistingSemesterDoesNotDeleteItsSubjectsOrSessions() {
        Context context = ApplicationProvider.getApplicationContext();
        String account = "TEST-" + UUID.randomUUID();
        AcademicPeriodRepository periodRepository = new AcademicPeriodRepository(context);
        PersonalSubjectRepository subjectRepository = new PersonalSubjectRepository(context);
        AttendanceEntryRepository entryRepository = new AttendanceEntryRepository(context);

        long periodId = periodRepository.saveOrUpdatePeriod(
                new AcademicPeriod(0, account, "test-year", "Semester 1", "", "", true, false, 0, 0));
        long subjectId = subjectRepository.saveOrUpdateSubject(
                new PersonalSubject(0, account, periodId, "Test subject", "", false, 0, 0));
        long entryId = entryRepository.saveOrUpdateEntry(
                new AttendanceEntry(0, account, subjectId, "16-10-2026", "", "Lecture", "PRESENT", "", 0, 0));

        long savedAgain = periodRepository.saveOrUpdatePeriod(
                new AcademicPeriod(0, account, "test-year", "Semester 1", "01-08-2026", "31-12-2026", true, false, 0, 0));

        assertEquals(periodId, savedAgain);
        assertEquals(1, subjectRepository.getSubjectsForPeriod(account, periodId).size());
        assertEquals(1, entryRepository.getEntriesForSubject(account, subjectId).size());
        assertFalse(periodRepository.setActivePeriod("TEST-other-account", periodId));
        assertFalse(periodRepository.deletePeriod("TEST-other-account", periodId));
        assertTrue(entryId > 0);
    }

    @Test
    public void attendanceEntriesAreOrderedByWhenTheyWereLogged() {
        Context context = ApplicationProvider.getApplicationContext();
        String account = "TEST-" + UUID.randomUUID();
        long periodId = new AcademicPeriodRepository(context).saveOrUpdatePeriod(
                new AcademicPeriod(0, account, "test-year", "Semester 1", "", "", true, false, 0, 0));
        long subjectId = new PersonalSubjectRepository(context).saveOrUpdateSubject(
                new PersonalSubject(0, account, periodId, "Test subject", "", false, 0, 0));
        AttendanceEntryRepository repository = new AttendanceEntryRepository(context);

        repository.saveOrUpdateEntry(new AttendanceEntry(0, account, subjectId, "16-10-2026", "", "", "PRESENT", "", 100, 100));
        repository.saveOrUpdateEntry(new AttendanceEntry(0, account, subjectId, "15-10-2026", "", "", "ABSENT", "", 200, 200));

        assertEquals("The minus control should remove the latest logged match even when an older date was entered later",
                "15-10-2026", repository.getEntriesForSubject(account, subjectId).get(0).getClassDate());
    }
}
