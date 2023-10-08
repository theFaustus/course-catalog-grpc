package inc.evil.coursecatalog.facade.impl

import inc.evil.coursecatalog.model.Course
import inc.evil.coursecatalog.service.CourseService
import inc.evil.courses.api.dto.CourseApiResponse
import inc.evil.courses.api.dto.CourseApiResponseList
import inc.evil.courses.api.dto.FindCourseByIdRequest
import inc.evil.courses.api.dto.InstructorApiResponse
import inc.evil.courses.api.facade.CourseApiFacadeGrpc.CourseApiFacadeImplBase
import inc.evil.courses.api.facade.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class CourseApiFacadeImpl(val courseService: CourseService) : CourseApiFacadeImplBase() {

    override fun findAll(request: Empty?, responseObserver: StreamObserver<CourseApiResponseList>) {
        responseObserver.onNext(
            CourseApiResponseList.newBuilder()
                .addAllCourses(courseService.findAll().map { toCourseApiResponse(it) })
                .build()
        )
        responseObserver.onCompleted()
    }

    override fun findById(request: FindCourseByIdRequest, responseObserver: StreamObserver<CourseApiResponse>) {
        runCatching { courseService.findById(request.id) }
            .onFailure { responseObserver.onError(Status.NOT_FOUND.withCause(it).asRuntimeException()) }
            .onSuccess {
                responseObserver.onNext(toCourseApiResponse(it))
                responseObserver.onCompleted()
            }

    }

    private fun toCourseApiResponse(it: Course): CourseApiResponse? = CourseApiResponse.newBuilder()
        .setId(it.id ?: 0)
        .setName(it.name)
        .setCategory(it.category.toString())
        .setProgrammingLanguage(it.programmingLanguage)
        .setProgrammingLanguageDescription(it.programmingLanguageDescription)
        .setCreatedAt(it.createdAt.toString())
        .setUpdatedAt(it.updatedAt.toString())
        .setInstructor(
            InstructorApiResponse.newBuilder()
                .setId(it.instructor?.id ?: 0)
                .setName(it.instructor?.name)
                .setSummary(it.instructor?.summary)
                .setDescription(it.instructor?.description)
                .build()
        ).build()
}
