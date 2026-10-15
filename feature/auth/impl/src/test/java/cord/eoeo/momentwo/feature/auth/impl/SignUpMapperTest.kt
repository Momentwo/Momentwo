package cord.eoeo.momentwo.feature.auth.impl

import cord.eoeo.momentwo.core.model.SignUpInput
import cord.eoeo.momentwo.feature.auth.impl.signup.*
import org.junit.Assert.assertEquals
import org.junit.Test

class SignUpMapperTest {
    @Test fun mappingCopiesAllRawInputsWithoutWireFormattingOrNormalization() {
        val state=SignUpState(
            name=SignUpFieldState(" Name "),email=SignUpFieldState("Mixed@Example.com"),
            password=SignUpFieldState(" Password1! "),nickname=SignUpFieldState(" 별명 "),
            birthday=SignUpFieldState("20000102"),phone=SignUpFieldState("01012345678"),
        )
        assertEquals(SignUpInput(" Name ","Mixed@Example.com"," Password1! "," 별명 ","20000102","01012345678"),state.toSignUpInput())
    }
}
