package com.alaishat.mohammad.docdoc.di

import com.alaishat.mohammad.data.remote.APIService
import com.alaishat.mohammad.domain.model.all_doctors.AllDoctorsResponse
import com.alaishat.mohammad.domain.model.all_doctors.success.AllDoctorsSuccessResponse
import com.alaishat.mohammad.domain.model.all_specialization.response.AllSpecializationsResponse
import com.alaishat.mohammad.domain.model.all_specialization.response.success.AllSpecializationSuccessResponse
import com.alaishat.mohammad.domain.model.appointments.allappointments.AllAppointmentsResponse
import com.alaishat.mohammad.domain.model.appointments.allappointments.AllAppointmentsSuccessResponse
import com.alaishat.mohammad.domain.model.appointments.bookappointment.BookAppointmentRequest
import com.alaishat.mohammad.domain.model.appointments.bookappointment.BookAppointmentResponse
import com.alaishat.mohammad.domain.model.appointments.bookappointment.invalidtime.InvalidTimeAppointmentResponse
import com.alaishat.mohammad.domain.model.appointments.bookappointment.success.BookAppointmentSuccessResponse
import com.alaishat.mohammad.domain.model.core.LocalError
import com.alaishat.mohammad.domain.model.core.UnauthorizedResponse
import com.alaishat.mohammad.domain.model.doctor_details.DoctorDetailsResponse
import com.alaishat.mohammad.domain.model.doctor_details.DoctorDetailsSuccessResponse
import com.alaishat.mohammad.domain.model.filtered_doctors.FilteredDoctorsResponse
import com.alaishat.mohammad.domain.model.filtered_doctors.FilteredDoctorsSuccessResponse
import com.alaishat.mohammad.domain.model.home.HomeResponse
import com.alaishat.mohammad.domain.model.home.HomeSuccessResponse
import com.alaishat.mohammad.domain.model.login.UserLoginFailedResponse
import com.alaishat.mohammad.domain.model.login.UserLoginRequest
import com.alaishat.mohammad.domain.model.login.UserLoginResponse
import com.alaishat.mohammad.domain.model.login.UserLoginSuccessResponse
import com.alaishat.mohammad.domain.model.register.RegisterResponse
import com.alaishat.mohammad.domain.model.register.UserRegisterRequest
import com.alaishat.mohammad.domain.model.register.failureresponse.bothtaken.EmailAndPhoneTakenRegisterResponse
import com.alaishat.mohammad.domain.model.register.failureresponse.emailtaken.EmailTakenRegisterResponse
import com.alaishat.mohammad.domain.model.register.failureresponse.local.LocalRegisterFailureResponse
import com.alaishat.mohammad.domain.model.register.failureresponse.phonetaken.PhoneTakenRegisterResponse
import com.alaishat.mohammad.domain.model.register.success.RegisterSuccessResponse
import com.alaishat.mohammad.domain.model.search.DoctorSearchResultResponse
import com.alaishat.mohammad.domain.model.search.DoctorSearchResultSuccessResponse
import com.alaishat.mohammad.domain.model.show_profile.UserProfileResponse
import com.alaishat.mohammad.domain.model.show_profile.UserProfileSuccessResponse
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.buildHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

/**
 * Created by Mohammad Al-Aishat on Aug/20/2024.
 * DocDoc Project.
 */

const val TAKEN_EMAIL = "The email"
const val TAKEN_PHONE = "The phone"
const val ROUTE_URL = "https://vcare.integration25.com/api"
const val REGISTER_END_POINT = "/auth/register"
const val LOGIN_END_POINT = "/auth/login"
const val ALL_SPECIALIZATIONS_END_POINT = "/specialization/index" // Done: AllSpecializationsScreen
const val ALL_DOCTORS_END_POINT = "/doctor/index"
const val FILTERED_DOCTORS_BY_SPEC_END_POINT = "/doctor/doctor-filter?specialization=" // Done: AllSpecializationsScreen
const val FILTERED_DOCTORS_BY_CITY_END_POINT = "/doctor/doctor-filter?city="
const val DOCTOR_DETAILS_END_POINT = "/doctor/show/" // Done: DoctorDetailsScreen
const val BOOK_APPOINTMENT_END_POINT = "/appointment/store" // Done: BookAppointmentScreen
const val ALL_APPOINTMENT_END_POINT = "/appointment/index"
const val SEARCH_END_POINT = "/doctor/doctor-search?name=" // todo: search screen
const val USER_PROFILE_END_POINT = "/user/profile"
const val HOME_END_POINT = "/home/index"

const val UNAUTHORIZED_KEYWORD = "Unauthorized"
const val AUTHORIZATION = "Authorization"
const val BEARER = "Bearer"


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    val gson = Gson()

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }

    @Provides
    @Singleton
    fun provideAPIService(client: HttpClient): APIService {
        return object : APIService {
            override suspend fun register(userRegisterRequest: UserRegisterRequest): RegisterResponse {

                val response = client.post(urlString = "$ROUTE_URL$REGISTER_END_POINT") {
                    contentType(ContentType.Application.Json)
                    setBody(userRegisterRequest)
                }

                if (response.status.isSuccess())
                    return gson.fromJson(response.bodyAsText(), RegisterSuccessResponse::class.java)

                val text = response.bodyAsText()
                val takenEmail = text.contains(TAKEN_EMAIL)
                val takenPhone = text.contains(TAKEN_PHONE)

                if (takenEmail && takenPhone)
                    return gson.fromJson(response.bodyAsText(), EmailAndPhoneTakenRegisterResponse::class.java)
                if (takenEmail)
                    return gson.fromJson(response.bodyAsText(), EmailTakenRegisterResponse::class.java)
                if (takenPhone)
                    return gson.fromJson(response.bodyAsText(), PhoneTakenRegisterResponse::class.java)
                return gson.fromJson(response.bodyAsText(), LocalRegisterFailureResponse::class.java)
            }

            override suspend fun login(userLoginRequest: UserLoginRequest): UserLoginResponse {
                val response = client.post(urlString = "$ROUTE_URL$LOGIN_END_POINT") {
                    contentType(ContentType.Application.Json)
                    setBody(userLoginRequest)
                }

                if (response.status.isSuccess())
                    return gson.fromJson(response.bodyAsText(), UserLoginSuccessResponse::class.java)
                return gson.fromJson(response.bodyAsText(), UserLoginFailedResponse::class.java)
            }

            override suspend fun getAllSpecializations(token: String): AllSpecializationsResponse {
                val response = client.get(urlString = "$ROUTE_URL$ALL_SPECIALIZATIONS_END_POINT") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }

                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, AllSpecializationSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)

                return LocalError()
            }

            override suspend fun getAllDoctors(token: String): AllDoctorsResponse {
                val response = client.get(urlString = "$ROUTE_URL$ALL_DOCTORS_END_POINT") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }

                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, AllDoctorsSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)

                return gson.fromJson(response.bodyAsText(), LocalError::class.java)
            }

            override suspend fun getFilteredDoctorsByCity(token: String, id: Int): FilteredDoctorsResponse {

                val response = client.get(urlString = "$ROUTE_URL$FILTERED_DOCTORS_BY_CITY_END_POINT$id") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, FilteredDoctorsSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)

                return LocalError()
            }

            override suspend fun getFilteredDoctorsBySpecialization(
                token: String,
                specId: Int,
            ): FilteredDoctorsResponse {
                val response = client.get(urlString = "$ROUTE_URL$FILTERED_DOCTORS_BY_SPEC_END_POINT$specId") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, FilteredDoctorsSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                return LocalError()
            }

            override suspend fun getDoctorDetailsById(token: String, doctorId: Int): DoctorDetailsResponse {
                val response = client.get(urlString = "$ROUTE_URL$DOCTOR_DETAILS_END_POINT$doctorId") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess()) {
                    return gson.fromJson(bodyAsText, DoctorDetailsSuccessResponse::class.java)
                }
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                return LocalError()
            }

            override suspend fun bookAppointment(
                token: String,
                bookAppointmentRequest: BookAppointmentRequest,
            ): BookAppointmentResponse {
                val response = client.post(urlString = "$ROUTE_URL$BOOK_APPOINTMENT_END_POINT") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                    setBody(bookAppointmentRequest)
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, BookAppointmentSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                if (bodyAsText.contains(""))
                    return gson.fromJson(bodyAsText, InvalidTimeAppointmentResponse::class.java)
                return LocalError()
            }

            override suspend fun getAllAppointments(token: String): AllAppointmentsResponse {
                val response = client.get(urlString = "$ROUTE_URL$ALL_APPOINTMENT_END_POINT") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, AllAppointmentsSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                return LocalError()
            }

            override suspend fun searchForDoctors(token: String, query: String): DoctorSearchResultResponse {
                val response = client.get(urlString = "$ROUTE_URL$SEARCH_END_POINT$query") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess())
                    return gson.fromJson(bodyAsText, DoctorSearchResultSuccessResponse::class.java)
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                return LocalError()
            }

            override suspend fun getUserProfile(token: String): UserProfileResponse {

                val response = client.get(urlString = "$ROUTE_URL$USER_PROFILE_END_POINT")  {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess()) {
                    return gson.fromJson(bodyAsText, UserProfileSuccessResponse::class.java)
                }
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                return LocalError()
            }

            override suspend fun getHomeResponse(token: String): HomeResponse {
                val response = client.get(urlString = "$ROUTE_URL$HOME_END_POINT") {
                    contentType(ContentType.Application.Json)
                    buildHeaders {
                        headers {
                            append(AUTHORIZATION, "$BEARER $token")
                        }
                    }
                }
                val bodyAsText = response.bodyAsText()
                if (response.status.isSuccess()) {
                    return gson.fromJson(bodyAsText, HomeSuccessResponse::class.java)
                }
                if (bodyAsText.contains(UNAUTHORIZED_KEYWORD))
                    return gson.fromJson(bodyAsText, UnauthorizedResponse::class.java)
                return LocalError()
            }
        }
    }
}