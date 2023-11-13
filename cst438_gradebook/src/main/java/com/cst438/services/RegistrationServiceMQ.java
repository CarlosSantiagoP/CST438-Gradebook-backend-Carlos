package com.cst438.services;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.Course;
import com.cst438.domain.FinalGradeDTO;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "registration", name = "service", havingValue = "mq")
public class RegistrationServiceMQ implements RegistrationService {

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

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

		// deserialize message to EnrollmentDTO and update database
		EnrollmentDTO e = fromJsonString(message, EnrollmentDTO.class);

		// retrieve
		Enrollment studentEnrollment = enrollmentRepository.findById(e.id()).orElse(null);

		// update
		if (studentEnrollment != null) {
			studentEnrollment.setId(e.id());
			studentEnrollment.setStudentName(e.studentName());
			studentEnrollment.setStudentEmail(e.studentEmail());
		}

		// Save to database
		enrollmentRepository.save(studentEnrollment);
	}

	/*
	 * Send final grades to Registration Service
	 */
	@Override
	public void sendFinalGrades(int course_id, FinalGradeDTO[] grades) {

		System.out.println("Start sendFinalGrades " + course_id);

		// convert grades to JSON string and send to registration service
		String data = asJsonString(grades);
		rabbitTemplate.convertAndSend(registrationQueue.getName(), data);
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