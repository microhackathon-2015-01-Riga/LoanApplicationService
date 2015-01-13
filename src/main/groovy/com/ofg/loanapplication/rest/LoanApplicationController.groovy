package com.ofg.loanapplication.rest

import com.nurkiewicz.asyncretry.AsyncRetryExecutor
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

import java.util.concurrent.ScheduledThreadPoolExecutor

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

		callReportService(loanApplication)

		new ResponseEntity<LoanApplicationBean>(loanApplication, HttpStatus.CREATED);
	}

	private String callFraudService(LoanApplicationBean loanApplication) {
		def asyncRetryExecutor = new AsyncRetryExecutor(new ScheduledThreadPoolExecutor(1))
		asyncRetryExecutor.withMaxRetries(3)
		serviceRestClient.forService("fraud").retryUsing(asyncRetryExecutor)
				.put()
				.onUrl("/api/loanApplication/${loanApplication.loanId}")
				.body("{ 'firstName' : '${loanApplication.firstName}', 'lastName' : ${loanApplication.lastName}, 'job' : ${loanApplication.job}, 'amount' : ${loanApplication.amount}, 'age' : ${loanApplication.amount} }")
				.withHeaders().contentTypeJson()
				.andExecuteFor()
				.anObject()
				.ofType(String)
	}

	private String callReportService(LoanApplicationBean loanApplication) {
		def asyncRetryExecutor = new AsyncRetryExecutor(new ScheduledThreadPoolExecutor(1))
		asyncRetryExecutor.withMaxRetries(3)
		serviceRestClient.forService("report").retryUsing(asyncRetryExecutor)
				.post()
				.onUrl("/api/client")
				.body(" { 'firstName' : '${loanApplication.firstName}', 'lastName' : ${loanApplication.lastName}, 'age' : ${loanApplication.amount}, 'loanId' : ${loanApplication.loanId} }")
				.withHeaders().contentTypeJson()
				.andExecuteFor()
				.anObject()
				.ofType(String)
	}
}
