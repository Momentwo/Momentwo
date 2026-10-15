package cord.eoeo.momentwo.core.data.signup

import cord.eoeo.momentwo.core.data.model.User
import cord.eoeo.momentwo.core.model.SignUpInput
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

internal val signUpFixture = SignUpInput(" Private Name ", "MixedCase@Example.com", " PrivatePass1! ", " 별명 ", "20000102", "01012345678")

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpRepositoryTest {
    private class Source : SignUpDataSource {
        val requests = mutableListOf<User>()
        var request: suspend () -> Result<Unit> = { Result.success(Unit) }
        val checks = mutableListOf<Pair<String,String>>()
        var checked: Result<Unit> = Result.success(Unit)
        override suspend fun requestSignUp(user: User): Result<Unit> { requests += user; return request() }
        override suspend fun checkEmail(email: String): Result<Unit> { checks += "email" to email; return checked }
        override suspend fun checkNickname(nickname: String): Result<Unit> { checks += "nickname" to nickname; return checked }
    }
    @Test fun mappingPreservesOriginalValuesAndAddsOnlyWireSeparators() = runTest {
        val source=Source(); val result=SignUpRepositoryImpl(source).requestSignUp(signUpFixture)
        assertTrue(result.isSuccess)
        val dto=source.requests.single()
        assertEquals(signUpFixture.name,dto.name);assertEquals(signUpFixture.email,dto.username)
        assertEquals(signUpFixture.password,dto.password);assertEquals(signUpFixture.nickname,dto.nickname)
        assertEquals("2000-01-02",dto.birthday);assertEquals("010-1234-5678",dto.phone)
    }
    @Test fun mapperDoesNotAddCalendarValidationOrNormalizeOtherFields() {
        val dto=signUpFixture.copy(name="",email="",password="",nickname="",birthdayDigits="20000231").toUserDto()
        assertEquals("2000-02-31",dto.birthday)
        assertEquals("",dto.name);assertEquals("",dto.username);assertEquals("",dto.password);assertEquals("",dto.nickname)
    }
    @Test fun invalidDigitsReturnFailureWithoutCallingSource() = runTest {
        val source=Source();val repository=SignUpRepositoryImpl(source)
        val invalid=listOf(
            signUpFixture.copy(birthdayDigits=""),signUpFixture.copy(birthdayDigits="2000010"),
            signUpFixture.copy(birthdayDigits="200001021"),signUpFixture.copy(birthdayDigits="2000-01-02"),
            signUpFixture.copy(birthdayDigits="２００００１０２"),signUpFixture.copy(birthdayDigits="2000010a"),
            signUpFixture.copy(phoneDigits=""),signUpFixture.copy(phoneDigits="0101234567"),
            signUpFixture.copy(phoneDigits="010123456789"),signUpFixture.copy(phoneDigits="010-1234-5678"),
            signUpFixture.copy(phoneDigits="０１０１２３４５６７８"),signUpFixture.copy(phoneDigits="0101234567a"))
        invalid.forEach { assertTrue(repository.requestSignUp(it).exceptionOrNull() is IllegalArgumentException) }
        assertTrue(source.requests.isEmpty())
    }
    @Test fun ordinaryReturnedAndThrownFailuresKeepTheirOriginalCause() = runTest {
        val source=Source();val repository=SignUpRepositoryImpl(source);val failure=IOException("offline")
        source.request={Result.failure(failure)};assertSame(failure,repository.requestSignUp(signUpFixture).exceptionOrNull())
        source.request={throw failure};assertSame(failure,repository.requestSignUp(signUpFixture).exceptionOrNull())
    }
    @Test fun thrownAndReturnedCancellationAreRethrown() = runTest {
        val source=Source();val repository=SignUpRepositoryImpl(source)
        val cancelled=CancellationException("cancelled")
        for(returnResult in listOf(true,false)) {
            source.request={if(returnResult)Result.failure(cancelled)else throw cancelled}
            try { repository.requestSignUp(signUpFixture); fail("Cancellation must escape") }
            catch(actual:CancellationException) { assertSame(cancelled,actual) }
        }
    }
    @Test fun fatalErrorIsNeverConvertedToFailure() = runTest {
        val source=Source();val failure=AssertionError("fatal");source.request={Result.failure(failure)}
        try { SignUpRepositoryImpl(source).requestSignUp(signUpFixture);fail("Error must escape") }
        catch(actual:AssertionError) { assertSame(failure,actual) }
    }
    @Test fun cancelledContextCannotMapOrCallSource() = runTest {
        val source=Source();var returned=false
        val job=launch(start=CoroutineStart.LAZY) { currentCoroutineContext().cancel();SignUpRepositoryImpl(source).requestSignUp(signUpFixture);returned=true }
        job.start();job.join();assertTrue(job.isCancelled);assertFalse(returned);assertTrue(source.requests.isEmpty())
    }
    @Test fun cancellationDuringSuspendCannotReturnSuccess() = runTest {
        val source=Source();val started=CompletableDeferred<Unit>();var returned=false
        source.request={started.complete(Unit);awaitCancellation()}
        val job=launch{SignUpRepositoryImpl(source).requestSignUp(signUpFixture);returned=true}
        started.await();job.cancelAndJoin();assertTrue(job.isCancelled);assertFalse(returned)
    }
    @Test fun swallowedCancellationCannotReturnSuccess() = runTest {
        val source=Source();val started=CompletableDeferred<Unit>();val complete=CompletableDeferred<Unit>();var returned=false
        source.request={withContext(NonCancellable){started.complete(Unit);complete.await()};Result.success(Unit)}
        val job=launch{SignUpRepositoryImpl(source).requestSignUp(signUpFixture);returned=true}
        started.await();job.cancel();complete.complete(Unit);job.join()
        assertTrue(job.isCancelled);assertFalse(returned)
    }
    @Test fun duplicateChecksKeepArgumentsAndOriginalResult() = runTest {
        val source=Source();val repository=SignUpRepositoryImpl(source);val failure=IOException("existing check")
        source.checked=Result.failure(failure)
        assertSame(failure,repository.checkEmail(" Mixed@Example.com ").exceptionOrNull())
        assertSame(failure,repository.checkNickname(" 별명 ").exceptionOrNull())
        assertEquals(listOf("email" to " Mixed@Example.com ","nickname" to " 별명 "),source.checks)
    }
    @Test fun dtoStringRepresentationDoesNotExposeAnyField() {
        val dto=signUpFixture.toUserDto()
        listOf(dto.name,dto.username,dto.password,dto.nickname,dto.birthday,dto.phone).forEach{assertFalse(dto.toString().contains(it))}
    }
}
