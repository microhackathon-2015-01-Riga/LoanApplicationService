package com.ofg.loanapplication.rest
import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.nurkiewicz.asyncretry.AsyncRetryExecutor
import com.ofg.infrastructure.hystrix.CorrelatedCommand
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

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey

@Slf4j
@RestController
@RequestMapping('/api')
@TypeChecked
@Api(value = "loanApplication", description = "Creates loan application")
class LoanApplicationController {


	private final LoanApplicationRepository repository
	private final ServiceRestClient serviceRestClient
	private final Meter meter


	@Autowired
	LoanApplicationController(MetricRegistry metricRegistry,
	                          LoanApplicationRepository repository,
	                          ServiceRestClient serviceRestClient) {
		meter = metricRegistry.meter('loanAmount')
		this.repository = repository
		this.serviceRestClient = serviceRestClient
	}

	@RequestMapping(
			value = '/loanApplication',
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Save new loan application")
	ResponseEntity<LoanApplicationBean> create(LoanApplicationBean loanApplication) {
		log.info("Trying to create application with id: ${loanApplication.loanId} and amount ${loanApplication.amount}")
		repository.save(new LoanApplication(loanId: loanApplication.loanId, amount: loanApplication.amount))
		log.info("Application saved")
		meter.mark(loanApplication.amount as Long)
		log.info("Metrics added")
		callFraudService(loanApplication)
		log.info("Fraud service called")
		callReportService(loanApplication)
		log.info("Report service called")
		new ResponseEntity<LoanApplicationBean>(loanApplication, HttpStatus.CREATED);
	}

	private String callFraudService(LoanApplicationBean loanApplication) {
		def asyncRetryExecutor = new AsyncRetryExecutor(new ScheduledThreadPoolExecutor(1)).withMaxRetries(3)

		def fraudRequest = new FraudRequest(firstName: loanApplication.firstName, lastName: loanApplication.lastName, job: loanApplication.job, amount: loanApplication.amount, age: loanApplication.age)
		
		new CorrelatedCommand<String>(withGroupKey(asKey("KEY"))) {
			String doRun() {
				serviceRestClient.forService("fraud").retryUsing(asyncRetryExecutor)
						.put()
						.onUrl("/api/loanApplication/${loanApplication.loanId}")
						.body(fraudRequest)
						.withHeaders().contentTypeJson()
						.andExecuteFor()
						.anObject()
						.ofType(String)
				
			}
			@Override
			protected String getFallback() {
				log.info("Hystrix fallback on Fraud Service call")
				return super.getFallback()
			}
		}.execute()
		
	}

	private String callReportService(LoanApplicationBean loanApplication) {
		def asyncRetryExecutor = new AsyncRetryExecutor(new ScheduledThreadPoolExecutor(1)).withMaxRetries(3)
		new CorrelatedCommand<String>(withGroupKey(asKey("KEY"))) {
			String doRun() {
				serviceRestClient.forService("report").retryUsing(asyncRetryExecutor)
						.post()
						.onUrl("/api/client")
						.body(new ReportRequest(firstName: loanApplication.firstName, lastName: loanApplication.lastName, loanId: loanApplication.loanId, age: loanApplication.age))
						.withHeaders().contentTypeJson()
						.andExecuteFor()
						.anObject()
						.ofType(String)
			}

			@Override
			protected String getFallback() {
				log.info("Hystrix fallback on Report Service call")
				return super.getFallback()
			}
		}.execute()
		
	}

}
