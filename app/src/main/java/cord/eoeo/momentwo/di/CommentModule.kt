package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.core.data.comment.CommentDataSource
import cord.eoeo.momentwo.core.data.comment.CommentRepository
import cord.eoeo.momentwo.core.data.comment.CommentRepositoryImpl
import cord.eoeo.momentwo.core.data.comment.remote.CommentRemoteDataSource
import cord.eoeo.momentwo.core.data.comment.remote.CommentService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommentModule {
    @Provides
    @Singleton
    fun provideCommentService(retrofit: Retrofit): CommentService =
        retrofit.create(CommentService::class.java)

    @Provides
    @Singleton
    fun provideCommentRemoteDataSource(commentService: CommentService): CommentDataSource =
        CommentRemoteDataSource(commentService)

    @Provides
    @Singleton
    fun provideCommentRepository(commentRemoteDataSource: CommentDataSource): CommentRepository =
        CommentRepositoryImpl(commentRemoteDataSource)
}
