package com.ofg.loanapplication.rest
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
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

    @RequestMapping(
            value = '/loanApplication',
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Save new loan application")
    ResponseEntity<?> create(LoanApplicationBean loanApplication) {
        println loanApplication // save to db
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }
}
