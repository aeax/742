package org.darkan.core.net.web

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult

fun RequestValidationConfig.installCoreValidators() {
    validateAccountCreate()
}

data class AccountCreateRequest(val username: String, val email: String, val password: String)

private fun RequestValidationConfig.validateAccountCreate() {
    validate<AccountCreateRequest> { request ->
        when {
            !request.username.matches(Regex("^(?!\\\\s)(?!.*\\\\s{2})(?=.*[a-zA-Z0-9])[a-zA-Z0-9 ]{1,16}\$")) ->
                ValidationResult.Invalid("Username must be 1-16 alphanumeric characters, may include spaces but not consecutive spaces or spaces alone")
            !request.email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) ->
                ValidationResult.Invalid("Invalid email format")
            !request.password.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) ->
                ValidationResult.Invalid("Password must be at least 8 characters and contain at least one letter and one number")
            else -> ValidationResult.Valid
        }
    }
}