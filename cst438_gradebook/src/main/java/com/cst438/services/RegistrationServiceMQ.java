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
		System.out.println("Gradebook has received: "+message);
		EnrollmentDTO dto = fromJsonString(message, EnrollmentDTO.class);
		System.out.println(dto.toString());

		Course course = courseRepository.findById(dto.courseId()).orElse(null);
		if (course==null) {
			System.out.println("Error. Student add to course. course not found "+dto.toString());
		} else {
			Enrollment enrollment = new Enrollment();
			enrollment.setCourse(course);
			enrollment.setStudentEmail(dto.studentEmail());
			enrollment.setStudentName(dto.studentName());
			enrollmentRepository.save(enrollment);
			System.out.println("End receive enrollment.");
		}
	}

	/*
	 * Send final grades to Registration Service
	 */
	@Override
	public void sendFinalGrades(int course_id, FinalGradeDTO[] grades) {

		System.out.println("Start sendFinalGrades "+course_id);
		String message = asJsonString(grades);
		System.out.println(message);
		rabbitTemplate.convertAndSend(registrationQueue.getName(), message);
		System.out.println("End sendFinalGrades ");

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