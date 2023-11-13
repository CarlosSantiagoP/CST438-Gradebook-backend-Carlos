package com.cst438.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.cst438.domain.FinalGradeDTO;

@Service
@ConditionalOnProperty(prefix="registration", name="service", havingValue = "default", matchIfMissing=true)
public class RegistrationServiceDefault implements RegistrationService {

	public RegistrationServiceDefault() {
		System.out.println("Default registration service.");
	}

	public void sendFinalGrades(int course_id , FinalGradeDTO[] grades) {

	}

}