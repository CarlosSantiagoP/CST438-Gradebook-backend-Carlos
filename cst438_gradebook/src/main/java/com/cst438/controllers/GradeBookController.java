package com.cst438.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.GradeDTO;
import com.cst438.domain.FinalGradeDTO;
import com.cst438.services.RegistrationService;

@RestController
@CrossOrigin 
public class GradeBookController {
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	AssignmentGradeRepository assignmentGradeRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	RegistrationService registrationService;
	
	/*
	 * get current grades of students for an assignment
	 * if student does not have a grade, create an blank grade
	 * id - assignment id
	 */
	@GetMapping("/gradebook/{id}")
	public GradeDTO[] getGradebook(@PathVariable("id") Integer assignmentId  ) {
		String email = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
		Assignment assignment = checkAssignment(assignmentId, email);
		// get the enrollments for the course
		// for each enrollment, get the current grade for assignment, 
		// if the student does not have a current grade, create an empty grade
		List<Enrollment> students = assignment.getCourse().getEnrollments();
		GradeDTO[] grades = new GradeDTO[students.size()];
		int idx=0;
		for (Enrollment e: students) {
			// does student have a grade for this assignment
			AssignmentGrade ag = assignmentGradeRepository.findByAssignmentIdAndStudentEmail(assignmentId,  e.getStudentEmail());
			if (ag == null) {
				ag = new AssignmentGrade(assignment, e);
				assignmentGradeRepository.save(ag);
			}
			grades[idx++] = new GradeDTO(ag.getId(), e.getStudentName(), e.getStudentEmail(), ag.getScore());
		}
		return grades;
	}
	
	@GetMapping("/gradebook/all") // Should return and display all the grades not just a specific student 
	public List<GradeDTO> getAllGrades(){
		String email = "dwisneski@csumb.edu";
		Iterable<AssignmentGrade> allAssignmentGrades = assignmentGradeRepository.findAll();
		List<GradeDTO> grades = new ArrayList<>();
		
		for (AssignmentGrade AGrade : allAssignmentGrades) {
			Assignment assignment = AGrade.getAssignment();
			if (assignment != null && assignment.getCourse().getInstructor().equals(email)) {
				grades.add(new GradeDTO(AGrade.getId(), AGrade.getStudentEnrollment().getStudentName(), AGrade.getStudentEnrollment().getStudentEmail(), AGrade.getScore()));	
			}
		}
		
		return grades;
	}
	
	/* 
	 * calculate final grades.  Send final grades to registration to post to student enrollments
	 * average the student's non-null assignment grades and convert to letter grade
	 */
	@PostMapping("/course/{course_id}/finalgrades")
	@Transactional
	public void calcFinalGrades(@PathVariable int course_id) {
		System.out.println("Gradebook - calcFinalGrades for course " + course_id);
		// check that this request is from the course instructor 
		String email = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
		Course c = courseRepository.findById(course_id).orElse(null);
		if (!c.getInstructor().equals(email)) {
			throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
		}
		// for each student in the course, calculate average of all assignment grades
		// and convert to a letter grade
		ArrayList<FinalGradeDTO> grades = new ArrayList<>();
		for (Enrollment e: c.getEnrollments()) {
			double total=0.0;
			int count = 0;
			for (AssignmentGrade ag : e.getAssignmentGrades()) {
				if (ag.getScore()!=null) {
					total = total+ag.getScore();
					count++;
				}
			}
			double average = (count > 0) ? average = total/count : 0; 
			FinalGradeDTO dto = new FinalGradeDTO(e.getStudentEmail(), e.getStudentName(), letterGrade(average), course_id);
			grades.add(dto);
		}
		registrationService.sendFinalGrades(course_id, grades.toArray(new FinalGradeDTO[grades.size()]));
	}

	/*
	 * update grade-book for an assignment with grades entered
	 * id - assignment id
	 */
	@PutMapping("/gradebook/{id}")
	@Transactional
	public void updateGradebook (@RequestBody GradeDTO[] grades, @PathVariable("id") Integer assignmentId ) {
		String email = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
		checkAssignment(assignmentId, email);  // check that user name matches instructor email of the course.
		// for each grade, update the assignment grade in database 		
		for (GradeDTO g : grades) {
			System.out.printf("%s\n", g.toString());
			AssignmentGrade ag = assignmentGradeRepository.findById(g.assignmentGradeId()).orElse(null);
			if (ag == null) {
				throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Invalid grade primary key. "+g.assignmentGradeId());
			}
			ag.setScore(g.grade());
			System.out.printf("%s\n", ag.toString());
			assignmentGradeRepository.save(ag);
		}
	}

	
	private Assignment checkAssignment(int assignmentId, String email) {
		// get assignment 
		Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
		if (assignment == null) {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment not found. "+assignmentId );
		}
		// check that user is the course instructor
		if (!assignment.getCourse().getInstructor().equals(email)) {
			throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
		}
		return assignment;
	}
	
	
	private String letterGrade(double grade) {
		if (grade >= 90) return "A";
		if (grade >= 80) return "B";
		if (grade >= 70) return "C";
		if (grade >= 60) return "D";
		return "F";
	}
}
