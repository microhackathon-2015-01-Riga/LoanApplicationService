package com.ofg.loanapplication.domain

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LoanApplicationRepository extends MongoRepository<LoanApplication, String> {

}
