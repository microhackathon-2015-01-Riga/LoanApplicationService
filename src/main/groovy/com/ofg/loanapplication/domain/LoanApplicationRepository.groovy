package com.ofg.loanapplication.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class LoanApplicationRepository {

	@Autowired
	MongoTemplate mongoTemplate


	def create(LoanApplication loanApplication) {
		mongoTemplate.insert(loanApplication)
	}

	def list() {
		mongoTemplate.findAll(LoanApplication)
	}
}
