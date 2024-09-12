package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.member.MemberDataSource
import cord.eoeo.momentwo.data.member.MemberRepository
import cord.eoeo.momentwo.data.member.MemberRepositoryImpl
import cord.eoeo.momentwo.data.member.remote.MemberRemoteDataSource
import cord.eoeo.momentwo.data.member.remote.MemberService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MemberModule {
    @Provides
    @Singleton
    fun provideMemberService(retrofit: Retrofit): MemberService = retrofit.create(MemberService::class.java)

    @Provides
    @Singleton
    fun provideMemberRemoteDataSource(memberService: MemberService): MemberDataSource = MemberRemoteDataSource(memberService)

    @Provides
    @Singleton
    fun provideMemberRepository(memberRemoteDataSource: MemberDataSource): MemberRepository = MemberRepositoryImpl(memberRemoteDataSource)
}
