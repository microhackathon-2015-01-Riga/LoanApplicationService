package com.ofg.loanapplication.mongo

import com.github.fakemongo.Fongo
import com.mongodb.Mongo
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories
class MongoConfiguration extends AbstractMongoConfiguration {

	@Override
	Mongo mongo() {
		new Fongo("applications").getMongo()
	}

	@Override
	String getDatabaseName() {
		"applications"
	}
}
