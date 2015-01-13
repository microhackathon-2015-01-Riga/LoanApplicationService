package com.ofg.loanapplication.rest

import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient
import com.ofg.loanapplication.domain.LoanApplication
import com.ofg.loanapplication.domain.LoanApplicationRepository
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
@RequestMapping('/api')
@TypeChecked
@Api(value = "loanApplication", description = "Creates loan application")
class LoanApplicationController {

	@Autowired
	LoanApplicationRepository repository

	@Autowired
	ServiceRestClient serviceRestClient

	@RequestMapping(
			value = '/loanApplication',
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Save new loan application")
	ResponseEntity<LoanApplicationBean> create(LoanApplicationBean loanApplication) {

		repository.save(new LoanApplication(loanId: loanApplication.loanId, amount: loanApplication.amount))

		callFraudService(loanApplication)

		callReportService()
		
		new ResponseEntity<LoanApplicationBean>(loanApplication, HttpStatus.CREATED);
	}

	private String callFraudService(LoanApplicationBean loanApplication) {
		serviceRestClient.forService("fraud")
				.put()
				.onUrl("/api/loanApplication/${loanApplication.loanId}")
				.body('''{ "firstName" : "text", "lastName" : "text", "job" : "text", "amount" : 20, "age" : 20 }''')
				.withHeaders().contentTypeJson()
				.andExecuteFor()
				.anObject()
				.ofType(String)
	}

	private String callReportService() {
		serviceRestClient.forService("report")
				.put()
				.onUrl("/api/client")
				.body('''{ "firstName" : "text", "lastName" : "text", "age" : 20, "loanId" : "20" }''')
				.withHeaders().contentTypeJson()
				.andExecuteFor()
				.anObject()
				.ofType(String)
	}
}
