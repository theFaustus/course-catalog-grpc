package inc.evil.reviews.service.impl

import inc.evil.courses.api.dto.CourseApiResponse
import inc.evil.courses.api.dto.FindCourseByIdRequest
import inc.evil.courses.api.facade.CourseApiFacadeGrpc.CourseApiFacadeBlockingStub
import inc.evil.reviews.common.exceptions.NotFoundException
import inc.evil.reviews.model.Review
import inc.evil.reviews.repo.ReviewRepository
import inc.evil.reviews.service.ReviewService
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class ReviewServiceImpl(val reviewRepository: ReviewRepository) : ReviewService {

    @GrpcClient("courses-service")
    private lateinit var courseApiFacade: CourseApiFacadeBlockingStub

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun findAll(): List<Review> {
        return reviewRepository.findAll().collectList().awaitFirst()
    }

    override suspend fun findById(id: Int): Review {
        return reviewRepository.findById(id).awaitFirstOrNull() ?: throw NotFoundException(Review::class, "id", id.toString())
    }

    override suspend fun save(review: Review): Review {
        runCatching {
            courseApiFacade.findById(FindCourseByIdRequest.newBuilder().setId(review.courseId!!).build()).also { log.info("gRPC call ended with $it") }
        }.getOrNull() ?: throw NotFoundException(CourseApiResponse::class, "course_id", review.courseId.toString())
        return reviewRepository.save(review).awaitFirst()
    }

    override suspend fun deleteById(id: Int): Void? {
        return reviewRepository.deleteById(id).awaitFirstOrNull()
    }

    override suspend fun findAllByCreatedAt(date: LocalDate): List<Review> {
        return reviewRepository.findAllByCreatedAt(date).collectList().awaitFirst()
    }
}
