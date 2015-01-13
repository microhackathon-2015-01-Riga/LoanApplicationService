package com.ofg.loanapplication.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface LoanApplicationRepository extends MongoRepository<LoanApplication, String> {

}
