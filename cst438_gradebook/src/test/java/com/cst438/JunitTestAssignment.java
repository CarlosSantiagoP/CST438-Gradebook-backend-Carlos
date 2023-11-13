package com.cst438;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.domain.AssignmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class JunitTestAssignment {

    @Autowired
    private MockMvc mvc;

    @Test
    public void createAssignment() throws Exception {
        AssignmentDTO adto = new AssignmentDTO(0, "test name", "2024-01-01", null, 31045);
        MockHttpServletResponse response;
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
        int new_id = Integer.parseInt(response.getContentAsString());
        assertTrue(new_id > 0);

        // now get the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignment/"+new_id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
        AssignmentDTO adtor = fromJsonString(response.getContentAsString(), AssignmentDTO.class);
        assertEquals(adto.assignmentName(), adtor.assignmentName());
        assertEquals(adto.courseId(), adtor.courseId());
        assertEquals(adto.dueDate(), adtor.dueDate());

        // now delete the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignment/"+new_id))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());

        // delete the assignment again.  Should be silently ignored.
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignment/"+new_id))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());

    }

    @Test
    public void createAssignmentInvalidCourse() throws Exception {
        AssignmentDTO adto = new AssignmentDTO(0, "test name", "2024-01-01", null, 31000);
        MockHttpServletResponse response;
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(400, response.getStatus());
        assertTrue(response.getErrorMessage().contains("course id not found "));

    }

    @Test
    public void createAssignmentNotInstructor() throws Exception {
        AssignmentDTO adto = new AssignmentDTO(0, "test name", "2024-01-01", null, 30291);
        MockHttpServletResponse response;
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(400, response.getStatus());
        assertTrue(response.getErrorMessage().contains("not authorized"));
    }

    @Test
    public void updateAssignment() throws Exception {
        AssignmentDTO adto = new AssignmentDTO(0, "test name", "2024-01-01", null, 31045);
        MockHttpServletResponse response;
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
        int new_id = Integer.parseInt(response.getContentAsString());
        assertTrue(new_id > 0);

        // now update the assignment
        AssignmentDTO adto2 = new AssignmentDTO(new_id, "test name updated", "2024-02-02", null, 0);
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/assignment/"+new_id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto2))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());

        // now get the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignment/"+new_id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
        AssignmentDTO adtor = fromJsonString(response.getContentAsString(), AssignmentDTO.class);
        assertEquals(adto2.assignmentName(), adtor.assignmentName());
        assertEquals(adto2.dueDate(), adtor.dueDate());


        // now delete the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignment/"+new_id))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());


    }

    @Test
    public void updateAssignmentNotFound() throws Exception {
        MockHttpServletResponse response;
        // try to update assignment that does not exist
        AssignmentDTO adto2 = new AssignmentDTO(9999, "test name updated", "2024-02-02", null, 0);
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/assignment/9999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto2))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(404, response.getStatus());

    }

    @Test
    public void updateAssignmentNotInstructor() throws Exception {
        MockHttpServletResponse response;
        // try to update assignment that belongs to another instructor
        AssignmentDTO adto2 = new AssignmentDTO(3, "test name updated", "2024-02-02", null, 0);
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/assignment/3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(adto2))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(404, response.getStatus());
    }


    @Test
    public void deleteAssignmentNotInstructor() throws Exception {
        MockHttpServletResponse response;
        // try to delete assignment that belongs to another instructor
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignment/3"))
                .andReturn().getResponse();
        assertEquals(403, response.getStatus());

    }

    @Test
    public void deleteAssignmentWithGrades() throws Exception {
        MockHttpServletResponse response;
        // try to delete assignment that has grades.  should fail.
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignment/1"))
                .andReturn().getResponse();
        assertEquals(400, response.getStatus());
        assertTrue(response.getErrorMessage().contains("has grades"));

        // now delete using force=yes.  should be good.
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignment/1?force=yes"))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
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