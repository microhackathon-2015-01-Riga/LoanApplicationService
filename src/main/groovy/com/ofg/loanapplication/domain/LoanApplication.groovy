package com.ofg.loanapplication.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class LoanApplication {
	@Id
	String id
	BigDecimal amount
	String loanId
}
