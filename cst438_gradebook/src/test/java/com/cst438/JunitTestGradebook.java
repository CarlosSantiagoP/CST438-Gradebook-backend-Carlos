package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Example of using Junit
 * Mockmvc is used to test a simulated REST call to the RestController
 * This test assumes that students test4@csumb.edu, test@csumb.edu are enrolled in course
 * with assignment with id=1
 */
@SpringBootTest
@AutoConfigureMockMvc
public class JunitTestGradebook {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AssignmentGradeRepository assignmentGradeRepository;

	/*
	 * Enter a new grade for student test4@csumb.edu for assignment id=1
	 */
	@Test
	public void gradeAssignment() throws Exception {

		MockHttpServletResponse response;

		// do an http get request for assignment 1 and test4
		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());

		// verify that returned data has non zero primary key
		GradeDTO[] result = fromJsonString(response.getContentAsString(), GradeDTO[].class);

		for (int i=0; i<result.length; i++) {
			GradeDTO g = result[i];
			if (g.email().equals("test4@csumb.edu")) {
				// change grade from null to 80.
				assertNull(g.grade());
				result[i] = new GradeDTO(g.assignmentGradeId(), g.name(), g.email(), 80);

			}
		}

		// send updates to server
		response = mvc
				.perform(MockMvcRequestBuilders.put("/gradebook/1").accept(MediaType.APPLICATION_JSON)
						.content(asJsonString(result)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify that return status = OK (value 200)
		assertEquals(200, response.getStatus());
		// verify that database assignmentGrade table was correctly updated
		AssignmentGrade ag = assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1,  "test4@csumb.edu");
		assertEquals(80, ag.getScore());

	}

	/*
	 * Update existing grade of test@csumb.edu for assignment id=1 from 90 to 88.
	 */
	@Test
	public void updateAssignmentGrade() throws Exception {

		MockHttpServletResponse response;

		// do an http get request for assignment 1
		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());

		// verify that returned data has non zero primary key
		GradeDTO[] result = fromJsonString(response.getContentAsString(), GradeDTO[].class);
		// change grade of student test@csumb.edu from 90 to 88
		for (int i=0; i<result.length; i++) {
			GradeDTO g = result[i];
			if (g.email().equals("test@csumb.edu")) {
				assertEquals(90, g.grade());
				result[i] = new GradeDTO(g.assignmentGradeId(), g.name(), g.email(), 88);

			}
		}

		// send updates to server
		response = mvc
				.perform(MockMvcRequestBuilders.put("/gradebook/1").accept(MediaType.APPLICATION_JSON)
						.content(asJsonString(result)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify that return status = OK (value 200)
		assertEquals(200, response.getStatus());

		AssignmentGrade ag = assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1,  "test@csumb.edu");
		assertEquals(88, ag.getScore());


	}

	private static String asJsonString(final Object obj) {
		try {

			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T fromJsonString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}