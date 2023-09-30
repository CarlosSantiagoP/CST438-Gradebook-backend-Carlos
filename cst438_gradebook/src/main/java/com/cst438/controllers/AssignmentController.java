package com.cst438.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

@RestController
@CrossOrigin 
public class AssignmentController {
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	@GetMapping("/assignment")
	public AssignmentDTO[] getAllAssignmentsForInstructor() {
		// get all assignments for this instructor
		String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
		List<Assignment> assignments = assignmentRepository.findByEmail(instructorEmail);
		AssignmentDTO[] result = new AssignmentDTO[assignments.size()];
		for (int i=0; i<assignments.size(); i++) {
			Assignment as = assignments.get(i);
			AssignmentDTO dto = new AssignmentDTO(
					as.getId(), 
					as.getName(), 
					as.getDueDate().toString(), 
					as.getCourse().getTitle(), 
					as.getCourse().getCourse_id());
			result[i]=dto;
		}
		return result;
	}
	
	@GetMapping("/assignment/{id}")
	public AssignmentDTO getAssignment(@PathVariable("id") int id)  {
		String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email)
		Assignment a = assignmentRepository.findById(id).orElse(null);
		if (a==null) {
			throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "assignment not found "+id);
		}
		// check that assignment is for a course of this instructor
		if (! a.getCourse().getInstructor().equals(instructorEmail)) {
			throw  new ResponseStatusException( HttpStatus.FORBIDDEN, "not authorized "+id);
		}
		AssignmentDTO adto = new AssignmentDTO(a.getId(), a.getName(), a.getDueDate().toString(), a.getCourse().getTitle(), a.getCourse().getCourse_id());
		return adto;

	}
	
	@PostMapping("/assignment")
	public int createAssignment(@RequestBody AssignmentDTO adto) {
		// check that course exists and belongs to this instructor
		String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email)
		Course c = courseRepository.findById(adto.courseId()).orElse(null);
		if (c==null || ! c.getInstructor().equals(instructorEmail)) {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "course id not found or not authorized "+adto.courseId());
		}
		// create and save assignment.  Return generated id to client.
		Assignment a = new Assignment();
		a.setCourse(c);
		a.setDueDate( java.sql.Date.valueOf(adto.dueDate()));
		a.setName(adto.assignmentName());
		assignmentRepository.save(a);
		return a.getId();
	}
	
	@PutMapping("/assignment/{id}")
	public void updateAssignment(@PathVariable("id") int id, @RequestBody AssignmentDTO adto) {
		// check assignment belongs to a course for this instructor
	    String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email)
	    Assignment a = assignmentRepository.findById(id).orElse(null);
	    if (a==null || ! a.getCourse().getInstructor().equals(instructorEmail)) {
	    	throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "assignment not found or not authorized "+id);
	    }
	    a.setDueDate( java.sql.Date.valueOf(adto.dueDate()));
	    a.setName(adto.assignmentName());
	    assignmentRepository.save(a);
	}
	
	@DeleteMapping("/assignment/{id}")
	public void deleteAssignment(@PathVariable("id") int id, @RequestParam("force") Optional<String> force) {
		// check assignment belongs to a course for this instructor
	    String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email)
	    Assignment a = assignmentRepository.findById(id).orElse(null);
	    if (a==null) {
	    	return;
	    }
	    if (! a.getCourse().getInstructor().equals(instructorEmail)) {
	    	throw  new ResponseStatusException( HttpStatus.FORBIDDEN, "not authorized "+id);
	    }
	    // does assignment have grades?  if yes, don't delete unless force is specified 
	    if (a.getAssignmentGrades().size()==0 || force.isPresent()) {
	    	assignmentRepository.deleteById(id);
	    } else {
	    	throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "assignment has grades ");
	    }
	}
}