package inc.evil.coursecatalog.web.graphql.error

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import inc.evil.coursecatalog.common.exceptions.NotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.*
import javax.validation.ConstraintViolationException
import javax.xml.bind.ValidationException


@Component
class GraphQLExceptionHandler : DataFetcherExceptionResolverAdapter() {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun resolveToSingleError(e: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return when (e) {
            is ValidationException -> toGraphQLError(e, env)
            is NotFoundException -> toGraphQLError(e, env)
            is MissingServletRequestParameterException -> toGraphQLError(e, env)
            is MethodArgumentNotValidException -> handleMethodArgumentNotValidException(e)
            is ConstraintViolationException -> handleConstraintViolationException(e)
            is MethodArgumentTypeMismatchException -> handleMethodArgumentTypeMismatchException(e)
            is Exception -> toGraphQLError(e, env)
            else -> super.resolveToSingleError(e, env)
        }
    }

    private fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): GraphQLError? {
        val parameter = e.parameter
        val message =
            "Parameter: '${parameter.parameterName}' is not valid. Value '${e.value}' could not be bound to type: '${parameter.parameterType.simpleName.lowercase()}'"
        log.warn("Exception while handling request: $message", e)
        return GraphqlErrorBuilder.newError().message(message).errorType(ErrorType.DataFetchingException).build()
    }

    private fun toGraphQLError(e: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        log.warn("Exception while fetching data (${env.executionStepInfo.path}) at ${env.field.sourceLocation}: ${e.message}", e)
        return GraphqlErrorBuilder.newError()
            .message(e.message)
            .location(env.field.sourceLocation)
            .path(env.executionStepInfo.path)
            .errorType(ErrorType.DataFetchingException).build()
    }


    private fun handleConstraintViolationException(e: ConstraintViolationException): GraphQLError? {
        val errorMessages = mutableSetOf<String>()
        e.constraintViolations.forEach { errorMessages.add("Field '${it.propertyPath}' ${it.message}, but value was [${it.invalidValue}]") }
        val message = errorMessages.joinToString("\n")
        log.warn("Exception while handling request: $message", e)
        return GraphqlErrorBuilder.newError().message(message).errorType(ErrorType.DataFetchingException).build()
    }

    private fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): GraphQLError? {
        val errorMessages = mutableSetOf<String>()
        e.bindingResult.allErrors.forEach { error ->
            if (error is FieldError) {
                errorMessages.add("Field '${error.field}' ${error.defaultMessage}, but value was [${error.rejectedValue}]")
            } else {
                errorMessages.add(Arrays.toString(error.codes))
            }
        }
        val message = errorMessages.joinToString("\n")
        log.warn("Exception while handling request: $message", e)
        return GraphqlErrorBuilder.newError().message(message).errorType(ErrorType.DataFetchingException).build()
    }
}
