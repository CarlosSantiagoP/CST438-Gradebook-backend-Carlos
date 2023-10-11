package com.cst438.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.Course;
import com.cst438.domain.FinalGradeDTO;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

@Service
@ConditionalOnProperty(prefix = "registration", name = "service", havingValue = "mq")
public class RegistrationServiceMQ implements RegistrationService {

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${registration.url}")
	String registration_url;

	RestTemplate restTemplate = new RestTemplate();

	public RegistrationServiceMQ() {
		System.out.println("MQ registration service ");
	}


	Queue registrationQueue = new Queue("registration-queue", true);
	@Bean
	Queue createQueue() {
		return new Queue("gradebook-queue");
	}

	/*
	 * Receive message for student added to course
	 */
	@RabbitListener(queues = "gradebook-queue")
	@Transactional
	public void receive(String message) {
		
		System.out.println("Gradebook has received: " + message);

		//TODO  deserialize message to EnrollmentDTO and update database
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			EnrollmentDTO enrollmentDTO = objectMapper.readValue(message, EnrollmentDTO.class);

			Enrollment enrollment = new Enrollment();
			enrollment.setStudentName(enrollmentDTO.studentName());
			enrollment.setStudentEmail(enrollmentDTO.studentEmail());

			enrollmentRepository.save(enrollment);
		} catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

	/*
	 * Send final grades to Registration Service 
	 */
	@Override
	public void sendFinalGrades(int course_id, FinalGradeDTO[] grades) {
		String url = "http://localhost:8081/course/" + course_id;
		restTemplate.put(url, grades);
	}
	
	private static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T  fromJsonString(String str, Class<T> valueType ) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
